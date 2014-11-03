(ns quand.pages.template
  (:require [hiccup.core :as html]
            [hiccup.page :as page]))


(defn page [content]
  (page/html5
   (page/include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.3.0/css/bootstrap.min.css")
   (page/include-js "//maxcdn.bootstrapcdn.com/bootstrap/3.3.0/js/bootstrap.min.js")
   [:div.container
    content]))


