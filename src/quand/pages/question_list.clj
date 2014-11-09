(ns quand.pages.question-list
  (:require [quand.db :as db]
            [ring.util.response :as resp]
            [quand.pages.template :as t]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))

(defn render-question [{:keys [message score]}]
  [:div.question (str score "|" "message: " message)])

(defn page [req room-id]
  (if-not ((db/rooms) room-id)
    (resp/not-found (str room-id " doesn't appear to exist."))
    (t/page
     [:div.jumbotron
      [:h3 (db/room-id->title room-id)]
      (for [q (-> @db/state (get room-id) :questions)]
        (render-question q))
      [:form {:action (str "/say") :method "GET"}
       (anti-forgery-field)
       [:input.chat {:type "text" :name "chat_message" :placeholder "Ask a thoughtful question"}]
       [:input {:type "submit" :value "Ask Carefully."}]]])))
