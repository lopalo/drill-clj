(ns drill.common.mixins
  (:require [rum.core :refer [local]]
            [cljs-react-material-ui.rum :as ui]))

(def loading ::loading)

(defn loader
  ([] (loader true {}))
  ([initial] (loader initial {}))
  ([initial props]
   (let [default-props {:status "loading"
                        :style {:position "relative"
                                :margin "auto"
                                :margin-top "30%"}
                        :left 0
                        :top 0}
         indicator (ui/refresh-indicator (merge default-props props))
         wrap-render
         (fn [render-fn]
           (fn [state]
             (if @(loading state)
               [indicator state]
               (render-fn state))))]
     (assoc (local initial loading) :wrap-render wrap-render))))

