(ns quand.db
  (:require [clojure.string :as str]))


;; room-id is a key that maps top-level to a room.
(defonce state (atom {}))

(def room-names
  (memoize (fn []
             (read-string (slurp "resources/room_names.txt")))))

(defn init-room [{:keys [room-id title] :as room-map}]
  {:title title
   :room-id room-id
   :questions []})

(defn create-room
  "creates a room."
  [{:keys [room-id title] :as room-map}]
  (let [name-taken? (boolean (get @state (keyword room-id)))]
    (if name-taken?
      (throw (Exception. "room ids must be unique"))
      (swap! state (fn [st] (assoc st
                              (keyword room-id)
                              (init-room room-map)))))))

(create-room {:room-id "apple"
              :title "Apples and other stuff."})

(comment
  ;; reset rooms
  (swap! state (fn [st] {}))

  )
