(ns quand.db
  (:require [clojure.string :as str]
            [cheshire.core :as json]))

;; room-id is a key that maps top-level to a room.
(defonce state (atom {}))

(defn ->json-state []
  (json/encode @state))

(defn q->score [{:keys [upvotes downvotes]}]
  (- (count upvotes) (count downvotes)))

(defn map-vals [f m]
  (into {} (map (fn [[k v]] [k (f v)]) m)))

(defn ->json-room [room-id]
  ;; adds the scores
  (json/encode
   (let [questions (vals (:questions (get @state room-id)))]
     (map (fn [q] (assoc q :score (q->score q))) questions))))

(defn init-room [owner room-id]
  {:owner owner
   :room-id room-id
   :questions {}})

(defn new-question [message id]
  {:message message
   :id id
   :upvotes #{}
   :downvotes #{}})

(defn room-owners []
  (->> @state vals (map :owner) set))

(defn rooms []
  (->> @state keys set))

(defn room-id->owner [room-id]
  (-> @state (get room-id) :owner))

(defn kill-in-room [room-id message-id]
  (swap! state
         #(update-in % [room-id :questions]
                     (fn [qid->q] (dissoc qid->q message-id)))))

(defn kill [message-id]
  (doseq [room (rooms)]
    (kill-in-room room message-id)))

(defn vote [room-id message-id user-id up-or-down]
  (swap! state
         #(update-in % [room-id :questions message-id up-or-down]
                     (fn [votes] (conj votes user-id)))))

(defn downvote [room-id message-id user-id]
  (vote room-id message-id user-id :downvotes))

(defn upvote [room-id message-id user-id]
  (vote room-id message-id user-id :upvotes))

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


(defn create-user []
  (str (java.util.UUID/randomUUID)))

(defn create-message [room-id message]
  (let [message-id (str (java.util.UUID/randomUUID))]
    (swap! state
           #(update-in % [room-id :questions]
                       (fn [question-list]
                         (assoc question-list
                           message-id (new-question message message-id)))))))

(defn session->room-id [session]
  (:room-id (first (filter #(= (-> :owner %) session) (vals @state)))))

(defn assoc-cookie [resp session-id]
  (merge resp {:cookies {"value" {:value session-id}}}))

(comment
  ;; reset rooms
  (swap! state (fn [st] {}))
  (room-owners))


(create-user)
