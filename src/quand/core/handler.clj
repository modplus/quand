(ns quand.core.handler
  (:require [clojure.string :as str]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [quand.db :as db]
            [quand.pages.landing :as landing]
            [quand.pages.template :as t]
            [ring.middleware.defaults :refer [wrap-defaults
                                              site-defaults]]
            [ring.util.response :as resp]
            [ring.util.anti-forgery :refer [anti-forgery-field]]
            [org.httpkit.server :as server]
            [clojure.pprint :as pp]
            [clojure.tools.nrepl.server :as nrepl]
            [prone.middleware :as prone]
            [cider.nrepl :refer (cider-nrepl-handler)]))

(defn req->room-id [req]
  (-> req :query-params (get "title")))

(defn req->session [req]
  (-> req :cookies (get "ring-session") :value))

(defn create-room [req]
  (let [room-id (req->room-id req)]
    (db/create-room (req->session req) room-id)
    (resp/redirect (str "/room/" room-id))))

(defn show-rooms [_]
  (t/page
   [:pre (with-out-str
           (pp/pprint @db/state))]))

(defn render-question [{:keys [user message score]}]
  [:div.question
   [:div score]
   [:div message]
   [:div (str "user:" user)]])

(defn chat-room [req room-id]
  (if-not ((db/rooms) room-id)
    (resp/not-found (str room-id " doesn't appear to exist."))
    (t/page
     [:div.jumbotron
      (for [q-map (-> @db/state (get room-id) :questions)]
        (render-question q-map))
      [:form {:action (str "/say/" room-id) :method "POST"}
       (anti-forgery-field)
       [:input.chat {:type "text" :name "chat_message" :placeholder "Ask a thoughtful question"}]
       [:input {:type "submit" :value "neat"}] [:i.fa.fa-camera-retro.fa-3x]]])))

(defn new-question [user message]
  {:user user
   :message message
   :score 0})

(defn add-message [room-id user message]
  (swap! db/state
         #(update-in % [room-id :questions]
                     (fn [question-list] (conj question-list
                                               (new-question user message))))))

(defn say-something [req room-id]
  (println "say something hit")
  (let [_ (def *r req)
        message "user123"
        user "user123"]
    (if (get @db/state room-id)
      (add-message room-id user message)
      {:body "error" :status 200})))


(defroutes app-routes
  (GET  "/" [] landing/page)
  (GET  "/create" [] create-room)
  (GET  "/listing" [] show-rooms)
  (POST "/say/:room-id" [room-id] ;#(say-something % room-id)
        (str room-id)
        )
  (GET  "/room/:room-id" [room-id] #(chat-room % room-id))
  (route/not-found "Not Found"))

(defn cookie->owner [req]
  (try (-> req :headers (get "cookie") (str/split #"=") second)
       (catch Exception e nil)))

(defn owner-check-middleware
  "if our visitor owns a room,
  he should be directed immediately to their room."
  [handler]
  (fn [req]
    (if (and (= (req :request-method) :get)
             (not (re-matches #"/room.*" (:uri req)))
             (get (db/room-owners) (cookie->owner req)))
      ;; recognize user ^
      (let [room-id (db/session->room-id (cookie->owner req))]
        (resp/redirect (str "/room/" room-id)))
      (handler req))))

(def app
  (-> (wrap-defaults app-routes site-defaults)
   ;; owner-check-middleware
      prone/wrap-exceptions ;; last, i think
      ))

(defn -main [& args]
  (nrepl/start-server :port 10101
                      :handler cider-nrepl-handler)
  (def stop-server
    (server/run-server app {:port 8080})))


(comment
  (stop-server)
  )
