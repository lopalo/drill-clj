(ns drill.common.mixins
  (:require [clojure.core.async :refer [chan mult tap put! close!]]
            [rum.core :refer [local]]
            [cljs-react-material-ui.rum :as ui]
            [drill.utils :refer [log]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(def ^:private loading ::loading)

(defn loader-mx
  [& {:keys [initial props] :or {initial true props {}}}]
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
    (assoc (local initial loading) :wrap-render wrap-render)))

(defn wrap-load [load-fn]
  (fn [state]
    (go (reset! (loading state) true)
        (<! (load-fn state))
        (reset! (loading state) false))
    state))

(def ^:private ^:dynamic *tab-channels*)

(defn tab
  [value label component]
  (let [c (@*tab-channels* value)
        c (if c c (chan 1))]
    (when (not (@*tab-channels* value))
      (swap! *tab-channels* assoc value c))
    (ui/tab {:label label :value value :on-active #(put! c true)}
            (component c))))

(def tabs-mx
  {:init #(assoc % ::tab-channels (atom {}))
   :wrap-render
   (fn [render-fn]
     (fn [state]
       (binding [*tab-channels* (::tab-channels state)]
         (render-fn state))))})

(def cleanup-mx
  {:init
   (fn [state]
     (let [ch (chan 1)
           mult (mult ch)]
       (-> state
           (assoc ::cleanup-channel ch)
           (assoc ::cleanup-mult mult))))
   :will-unmount
   (fn [state]
     (close! (::cleanup-channel state))
     state)})

(defn cleanup-chan [state]
  (let [ch (chan 1)]
    (tap (::cleanup-mult state) ch)
    ch))

(defn tab-mx [load!]
  {:did-mount
   (fn [{[on-active] :rum/args :as state}]
     (let [cleanup (cleanup-chan state)]
       (go-loop [[ok] (alts! [on-active cleanup])]
         (when ok
           (load! state)
           (recur (alts! [on-active cleanup])))))
     (load! state)
     state)})
