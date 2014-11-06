(ns quand.core.handler
  (:require [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [quand.db :as db]
            [quand.pages.landing :as landing]
            [quand.pages.template :as t]
            [ring.middleware.defaults :refer [wrap-defaults
                                              site-defaults]]
            [ring.util.response :as resp]))

(defn req->room-id [req]
  (-> req :query-params (get "title")))

(defn req->session [req]
  (-> req :cookies (get "ring-session") :value))

(defn create-room [req]
  (db/create-room (req->session req) (req->room-id req))
  (resp/redirect (str "/listing")))

(defn show-rooms [_]
  (t/page
   [:pre (with-out-str
           (clojure.pprint/pprint @db/state))]))

(defroutes app-routes
  (GET "/" [] landing/page)
  (GET "/create" [] create-room)
  (GET "/listing" [] show-rooms)
  (GET "/room/:room-id" [room-id] (str "this is room " room-id))
  (route/not-found "Not Found"))

(defn cookie->owner [req]
  (-> req :headers (get "cookie") (str/split #"=") second))

(defn owner-check-middleware
  "if our visitor owns a room,
  he should be directed immediately to their room."
  [handler]
  (fn [req]
    (if (and (not (re-matches #"/room.*" (:uri req)))
             (get (db/room-owners) (cookie->owner req)))
      ;; recognize user ^
      (let [room-id (db/session->room-id (cookie->owner req))]
        (resp/redirect (str "/room/" room-id)))
      (handler req))))

(def app
  (owner-check-middleware (wrap-defaults app-routes site-defaults)))
