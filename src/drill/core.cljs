(ns drill.core
  (:require [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme]]
            [cljs-react-material-ui.rum :as ui]
            [cljs-react-material-ui.icons :as ic]
            [rum.core :as rum :refer [defc reactive react]]
            [drill.app-state :refer [user setup-router!]]
            [drill.app :refer [app]]
            [drill.auth :refer [auth]]))

(enable-console-print!)
(setup-router!)

(defc root-component
  < reactive
  []
  (ui/mui-theme-provider
   {:mui-them (get-mui-theme)}
   (if (react user) (app) (auth))))

(defn mount []
  (rum/mount (root-component)
             (. js/document (getElementById "app"))))

(defn start-processes []) ;TODO: session checker

(defonce _
  (do (mount)
      (start-processes)))
(defn on-js-reload [] (mount))
