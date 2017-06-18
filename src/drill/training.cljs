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
            [drill.common.mixins :refer [wrap-load
                                         loader-mx
                                         tab-mx
                                         cleanup-mx]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(declare phrase-card flip-button)

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

(def initial-ui {:flipped false})

(defcs training < (loader-mx)
                  cleanup-mx
                  (local #queue [] ::ring-queue)
                  (local {} ::working-set)
                  (local initial-ui ::ui)
                  (tab-mx load!)
  [state]
  (let [*queue (::ring-queue state)
        *working-set (::working-set state)
        *ui (::ui state)
        pid (first @*queue)
        *phrase (cursor-in *working-set [pid])
        reset-ui! #(reset! *ui initial-ui)
        pass! #(do (swap! *queue rotate)
                   (reset-ui!))
        complete! #(do (complete-phrase! state)
                       (reset-ui!))]
    [:.training
     (ui/card
      (ui/card-text
       (phrase-card *phrase *ui))
      (ui/card-actions
       (ui/raised-button {:label "Pass"
                          :on-touch-tap pass!})
       (ui/raised-button {:label "Complete"
                          :on-touch-tap complete!})))]))

(defc flip-button [*flipped]
  [:.flip-button
   (ui/floating-action-button
    {:on-touch-tap #(swap! *flipped not)}
    (ic/action-autorenew))])

(defc card-text [text]
  [:.card-text text])

(defcs phrase-card < reactive
  [state *phrase *ui]
  (let [p (react *phrase)
        *flipped (cursor-in *ui [:flipped])
        text ((if @*flipped :targetText :sourceText) p)]
    (ui/paper {:z-depth 5
               :class-name "phrase-card"}
              (card-text text)
              (flip-button *flipped))))
