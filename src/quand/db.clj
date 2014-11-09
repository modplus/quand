(ns quand.db
  (:require [clojure.string :as str]))

;; room-id is a key that maps top-level to a room.
(defonce state (atom {}))

(def room-names
  (memoize
   (fn []
     (read-string (slurp "resources/room_names.txt")))))

(defn init-room [owner room-id]
  {:owner owner
   :room-id room-id
   :title (rand-nth (room-names))
   :questions []})

(defn new-question [message]
  {:message message
   :score 0})

(defn room-owners []
  (->> @state vals (map :owner) set))

(defn rooms []
  (->> @state keys set))

(defn room-id->title [room-id]
  (-> @state (get room-id) :title))

(defn create-room
  "creates a room."
  [owner room-id]
  (let [name-taken? (boolean (get @state room-id))]
    (cond
     name-taken? (throw (Exception. "room ids must be unique."))
     (not owner) (throw (Exception. "rooms must have an owner."))
     ;;(get (room-owners) owner) (throw (Exception. "one room per customer."))
     :else (swap! state
                  (fn [st] (assoc st
                             room-id
                             (init-room owner room-id)))))))

(defn create-message [room-id message]
  (swap! state
         #(update-in % [room-id :questions]
                     (fn [question-list] (conj question-list
                                               (new-question message))))))

(defn session->room-id [session]
  (:room-id (first (filter #(= (-> :owner %) session) (vals @state)))))

(comment
  ;; reset rooms
  (swap! state (fn [st] {}))
  (room-owners))
