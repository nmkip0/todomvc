(ns todomvc.views
  (:require
    [re-frame.core :refer [subscribe dispatch]]
    [todomvc.subs :as subs]
    [garden.core :refer [css]]
    [reagent.core :as reagent]
    [clojure.string :as str]))

(defn footer []
  [:footer.footer
   [:p "Double-click to edit a todo"]
   [:p "Created by nmkip"]
   [:p "Part of TodoMVC"]])

(defn title []
  [:header.header
   [:h1 "todos"]])

(defn task-entry [{:keys [id on-save on-stop]}]
  (let [val (reagent/atom "")
        stop #(do (reset! val "")
                  (when on-stop (on-stop)))
        save #(let [v (-> @val str str/trim)]
                (on-save v)
                (stop))]
    (fn [props]
      [:input.task-entry (merge (dissoc props :on-save :on-stop)
                                {:type        "text"
                                 :value       @val
                                 :auto-focus  true
                                 :on-blur     save
                                 :on-change   #(reset! val (-> % .-target .-value))
                                 :on-key-down #(case (.-which %)
                                                 13 (save)
                                                 27 (stop)
                                                 nil)})])))

(defn filter-task [key txt]
  (let [showing @(subscribe [:showing])]
    [:a {:href     (str "#/" (name key))
         :class    (when (= showing key) "selected")
         :on-click #(dispatch [:update-showing key])} txt]))

(defn task-controls []
  (let [[active-count done-count] @(subscribe [:task-count])]
    [:div.controls
     [:label (str active-count " " (if (= active-count 1) "task" "tasks") " left")]
     [:ul.filters
      [:li (filter-task :all "All")]
      [:li (filter-task :active "Active")]
      [:li (filter-task :completed "Completed")]]
     [:div.clear-completed
      (when (pos? done-count)
        [:button.delete-all-btn {:on-click #(dispatch [:clear-completed])} "Clear completed"])]]))

(defn display [done? showing]
  (when
    (or (and (= showing :completed) (not done?))
        (and (= showing :active) done?))
    "none"))

(defn task-style [done]
  (str "task" (when done " completed")))

(defn todo-item []
  (let [editing (reagent/atom false)]
    (fn [{:keys [id description done]} showing]
      [:li {:key id :style {:display (display done showing)}}
       [:div {:class           (task-style done)
              :on-double-click #(reset! editing true)}
        [:input.toggle {:type "checkbox" :checked done :on-change #(dispatch [:toggle-done id])}]
        [:div.task-content
         (if @editing
           [task-entry {:on-save #(when (seq %)
                                    (dispatch [:edit-task id %]))
                        :on-stop #(reset! editing false)}]
           [:label description])]
        [:button.delete-btn {:on-click #(dispatch [:delete-task id])} "×"]]])))

(defn task-list [tasks showing]
  [:ul.task-list
   (for [[_ task] tasks]
     ^{:key (:id task)} [todo-item task showing])])

(defn tasks-container []
  [:div.content
   [task-entry {:placeholder "What needs to be done?"
                :on-save     #(when (seq %)
                                (dispatch [:add-task %]))}]
   (when-let [tasks (seq @(subscribe [:tasks]))]
     [:div
      [task-list tasks @(subscribe [:showing])]
      [task-controls]])])

(defn app []
  [:div.container
   [title]
   [tasks-container]
   [footer]])