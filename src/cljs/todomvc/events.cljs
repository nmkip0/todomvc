(ns todomvc.events
  (:require
   [re-frame.core :as re-frame]
   [todomvc.db :as db]))

;; TODO: Interceptors (spec)
;; TODO: path para evitar tener db en todos lados.
;; TODO: Local storage

;; TODO: Review this.
(def id (atom 0))
(defn next-id! []
  (let [current-id @id]
    (swap! id inc)
    current-id))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
  :add-task
  (fn [db [_ new-task]]
    (let [id (next-id!)]
      (assoc-in db
                [:tasks id]
                {:id id :description new-task :done false}))))

(re-frame/reg-event-db
  :toggle-done
  (fn [db [_ id]]
    (update-in db [:tasks id :done] not)))

(re-frame/reg-event-db
  :delete-task
  (fn [db [_ id]]
    (update-in db [:tasks] dissoc id)))

(re-frame/reg-event-db
  :update-showing
  (fn [db [_ showing]]
    (assoc db :showing showing)))

(re-frame/reg-event-db
  :clear-completed
  (fn [db [_ _]]
    (let [completed-tasks (into {} (filter #(false? (:done (val %))) (:tasks db)))]
      (assoc db :tasks completed-tasks))))