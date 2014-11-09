(ns quand.pages.question-list
  (:require [quand.db :as db]
            [ring.util.response :as resp]
            [quand.pages.template :as t]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn render-question-aud [{:keys [message score]}]
  [:div.question (str score " | " message " v ^")])

(defn audience-view [room-id]
  (t/page
   [:div.jumbotron
    [:h3 room-id]
    (for [q (-> @db/state (get room-id) :questions)]
      (render-question-aud q))
    [:form {:action (str "/say") :method "GET"}
     (anti-forgery-field)
     [:input.chat {:type "text" :name "chat_message" :placeholder "Ask a thoughtful question"}]
     [:input {:type "submit" :value "Ask Carefully."}]]]))

(defn render-question-owner [{:keys [message score]}]
  [:div.question (str score " | " message " X")])

(defn owner-view [room-id]
  (t/page
   [:div.jumbotron
    [:h3 room-id]
    (for [q (:questions (get @db/state room-id))]
      (render-question-owner q))]))

(defn page [req room-id]
  (def *in [req room-id])
  (if-not ((db/rooms) room-id)
    (resp/not-found (str room-id " doesn't appear to exist."))
    (let [owner-from-db (db/room-id->owner room-id)
          owner-from-req (-> req :cookies (get "ring-session") :value)
          owner? (= owner-from-db owner-from-req)]
      (if owner? (owner-view room-id)
          (audience-view room-id)))))
