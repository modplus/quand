(ns quand.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [quand.pages.landing :as landing]))

(defn checkout-req [req]
  (def *r req)
  "hello")

(defroutes app-routes
  (GET "/" [] landing/page)
  (GET "/create" [] checkout-req)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
