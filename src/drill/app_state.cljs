
(ns drill.app-state
  (:require [clojure.string :as s]
            [rum.core :refer [cursor-in]]
            [drill.utils :refer [log]]
            [drill.common.processes :refer [api-patch]]))

(def languages ["en" "ru"])

(defonce *app-state
  (atom {:route ["training"] :user nil}))

(defonce *route (cursor-in *app-state [:route]))
(defonce *user (cursor-in *app-state [:user]))
(defonce *profile (cursor-in *app-state [:user :profile]))

(defn set-route! [route]
  (set! (. js/location -hash) route))

(defn setup-router! []
  (let [parse-hash #(-> % (s/replace-first "#" "") (s/split #"/"))
        set-route! #(reset! *route (-> js/location .-hash parse-hash))]
    (set! (. js/window -onhashchange) set-route!)
    (set-route!)))

(defn set-profile-field! [field value]
  (swap! *profile assoc field value)
  (api-patch "profile" {:fieldName field :value value}))

(comment
  (log "App state:" @*app-state)
  (set-route! "dictionary")
  (reset! *user {:name "Adolf"})
  (reset! *user nil))
