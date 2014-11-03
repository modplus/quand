(ns quand.pages.landing
  (:require [hiccup.core :as html]
            [quand.pages.template :as t]))

(defn page [request]
  (t/page
   [:div
    [:h2 "Welcome to quand."]
    [:form {:action "/create"}
     [:label "Name your room:"]
     [:input {:type "text"}]
     [:br]
     [:input {:type "submit" :value "Create Room."}]]]))
