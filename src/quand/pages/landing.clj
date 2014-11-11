(ns quand.pages.landing
  (:require [hiccup.core :as html]
            [quand.pages.template :as t]
            [ring.util.anti-forgery :refer [anti-forgery-field]]))


(defn page [request]
  (def *req request)
    (slurp "resources/index.html"))

#_(defn page [request]
  (def *r request)
  (t/page
   [:div.jumbotron
    [:h2 "Welcome to quand."]
    [:form {:action "/create" :method "GET"}
     [:label "Name your room:"]
     [:input {:type "text"
              :name "title"
              :placeholder "pick a name"}]
     [:br]
     [:input {:type "submit" :value "Create Room."}]]
    [:span "test:"]
    [:form {:action "/what" :method "POST"}
     (anti-forgery-field)
     [:input {:type "text" :name "title"}]
     [:input {:type "submit" :value "hitting why"}]]]))
