(ns quand.pages.landing
  (:require [hiccup.core :as html]
            [quand.pages.template :as t]))

(defn page [request]
  (t/page
   [:div.jumbotron
    [:h2 "Welcome to quand."]
    [:form {:action "/create" :method "GET"}
     [:label "Name your room:"]
     [:input {:type "text"
              :name "title"
              :placeholder "pick a name"}]
     [:br]
     [:input {:type "submit" :value "Create Room."}]]]))
