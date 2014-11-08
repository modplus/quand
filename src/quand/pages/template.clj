(ns quand.pages.template
  (:require [hiccup.core :as html]
            [hiccup.page :as page]
            [quand.db :as db]
            [clojure.pprint :as pp]))


(def debug false)

(defn page [content]
  (page/html5
   (page/include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.3.0/css/bootstrap.min.css"
                     "/freelancer.css"
                     "/style.css")
   (page/include-js "//code.jquery.com/jquery-2.1.1.min.js"
                    "//maxcdn.bootstrapcdn.com/bootstrap/3.3.0/js/bootstrap.min.js")
   [:div.container content]
   (when debug
     [:div
      [:h2 "State:"]
      [:pre
       (with-out-str
         (pp/pprint @db/state))]])))
