(ns drill.training
  (:require [clojure.set :refer [difference]]
            [clojure.core.async :refer [<!]]
            [rum.core :as rum :refer [defc
                                      defcs
                                      local
                                      reactive
                                      react
                                      cursor-in]]
            [cljs-react-material-ui.rum :as ui]
            [cljs-react-material-ui.icons :as ic]
            [drill.common.processes :refer [api-get api-post]]
            [drill.common.mixins :refer [wrap-load loader-mx tab-mx]])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(declare phrase-card)

(defn rotate [queue] (-> queue pop (conj (peek queue))))

(defn fetch-working-set!
  [state]
  (go (let [data (<! (api-get "training/working-set" {}))
            ids (map :id data)]
        (reset! (::ring-queue state) (into #queue [] ids))
        (reset! (::working-set state) (zipmap ids data)))))

(def load! (wrap-load fetch-working-set!))

(defn complete-phrase! [state]
  (let [*queue (::ring-queue state)
        id (first @*queue)
        *working-set (::working-set state)]
    (swap! *queue pop)
    (swap! *working-set dissoc id)
    (go (let [data (<! (api-post "training/working-set" {:id id}))
              ids (map :id data)
              new-ids (difference (set ids) (set @*queue))
              new-set (select-keys (zipmap ids data) new-ids)]
          (swap! *queue into new-ids)
          (swap! *working-set merge new-set)))))


(defcs training < (loader-mx)
                  (local #queue [] ::ring-queue)
                  (local {} ::working-set)
                  (tab-mx load!)
  [state]
  (let [*queue (::ring-queue state)
        *working-set (::working-set state)
        pid (first @*queue)
        *phrase (cursor-in *working-set [pid])
        pass! #(swap! *queue rotate)
        complete! #(complete-phrase! state)]
    (ui/card
      (ui/card-text
        (phrase-card *phrase pass! complete!))
      (ui/card-actions
        (ui/raised-button {:label "Pass"
                           :on-touch-tap pass!})
        (ui/raised-button {:label "Complete"
                           :on-touch-tap complete!})))))

(defcs phrase-card < reactive
                     (local {} ::flipped)
  [state phrase]
  (let [p (react phrase)
        *flipped (::flipped state)
        text ((if @*flipped :targetText :sourceText) p)
        flip-btn (ui/floating-action-button
                  {:on-touch-tap #(swap! *flipped not)}
                  (ic/av-replay))]
    (ui/paper {:z-depth 5} text flip-btn)))
