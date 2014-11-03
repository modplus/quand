(ns quand.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [quand.pages.landing :as landing]
            [cider.nrepl :as nrepl]
            [ring.adapter.jetty :as jetty]))

(defn v [req]
  (def *r req)
  (str
   "session token: " 
   (-> req :cookies (get "ring-session") :value)))

(defroutes app-routes
  (GET "/" [] landing/page)
  (GET "/create" [] v)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn -main [& args]
  (nrepl/start-server )
  (jetty/run-jetty app {:port 10501}))
