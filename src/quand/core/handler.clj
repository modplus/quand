(ns quand.core.handler
  (:require [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [quand.db :as db]
            [quand.config :as config]
            [quand.pages.landing :as landing]
            [quand.pages.template :as t]
            [ring.middleware.defaults :refer [wrap-defaults
                                              site-defaults]]
            [ring.util.response :as resp]
            [org.httpkit.server :as server]
            [clojure.pprint :as pp]
            [clojure.tools.nrepl.server :as nrepl]
            [prone.middleware :as prone]
            [cider.nrepl :refer (cider-nrepl-handler)]
            [quand.pages.question-list :as q-list]))

(defn debug [] (false))

(defonce server (atom nil))

(defn req->room-id [req]
  (-> req :query-params (get "title")))

(defn req->session [req]
  (-> req :cookies (get "ring-session") :value))

(defn cookie->owner [req]
  (try (-> req :headers (get "cookie") (str/split #"=") second)
       (catch Exception e nil)))

(defn create-room [req]
  (let [room-id (req->room-id req)
        session (or (req->session req)
                    (db/create-user))]
    (db/create-room session room-id)
    (merge 
     {:cookies {:value session}}
     (resp/redirect (str "/r/" room-id)))))

(defn create-message [req]
  (let [room-id (-> req :headers (get "referer") (str/split #"/") last)
        message (-> req :query-params (get "chat_message"))]
    (db/create-message room-id message)
    (resp/redirect (-> req :headers (get "referer")))))

(defroutes app-routes
  (GET "/" [] landing/page)
  (GET "/create" [] create-room)
  (GET "/say" [] create-message)
  (GET "/r/json/:room-id" [room-id] (db/->json-room room-id))
  (GET "/r/:room-id" [room-id] #(q-list/page % room-id))
  (POST "/delete/:message-id" [message-id] (db/kill message-id))
  (POST "/upvote/:message-id/:user-id" [message-id] (db/kill message-id))
  (POST "/downvote/:message-id/:user-id" [message-id] (db/kill message-id))  
  (GET "/json" [] (db/->json-state))
  (route/resources "/public")
  (route/not-found "Not Found"))

(defn owner-redirect-middleware
  "if our visitor owns a room,
  he should be directed immediately to their room."
  [handler]
  (fn [req]
    (if (and (= (req :request-method) :get)
             (not (re-matches #"/r.*" (:uri req)))
             (get (db/room-owners) (cookie->owner req)))
      ;; we recognize this user ^
      (let [room-id (db/session->room-id (cookie->owner req))]
        (resp/redirect (str "/r/" room-id)))
      (handler req))))

(def app
  (-> (wrap-defaults app-routes config/defaults)
      ;; owner-redirect-middleware
      prone/wrap-exceptions
      ))

(defn stop-server []
  (when-not (nil? @server)
    ;; graceful shutdown: wait 100ms for existing requests to be finished
    ;; :timeout is optional, when no timeout, stop immediately
    (@server :timeout 100)
    (reset! server nil)))

(defn -main [& args]
  ;; The #' is useful when you want to hot-reload code
  ;; You may want to take a look: https://github.com/clojure/tools.namespace
  ;; and http://http-kit.org/migration.html#reload
  (nrepl/start-server :port 10101 :handler cider-nrepl-handler)
  (println "NREPL Server on localhost:10101")
  (reset! server (server/run-server #'app {:port 8080}))
  (println "Started server on localhost:8080"))
