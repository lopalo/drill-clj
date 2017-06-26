(ns drill.training.core
  (:require [clojure.set :refer [difference]]
            [clojure.core.async :refer [<!]]
            [rum.core :as rum :refer [defc
                                      defcs
                                      local
                                      react
                                      cursor-in]]
            [cljs-react-material-ui.rum :as ui]
            [cljs-react-material-ui.icons :as ic]
            [drill.common.processes :refer [api-get api-post]]
            [drill.common.mixins :refer [wrap-load
                                         loader-mx
                                         tab-mx
                                         cleanup-mx]]
            [drill.training.flashcard :refer [flashcard]]
            [drill.training.phrase-constructor :refer [phrase-constructor]]
            [drill.training.speech
             :refer [activate! cancel!]
             :rename {activate! activate-speech!
                      cancel! cancel-speech!}])
  (:require-macros [cljs.core.async.macros :refer [go]]))


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

(def types #{:flashcard :constructor})

(def initial-ui {:type :constructor
                 :speech-active? false
                 :flashcard {:flipped false}
                 :constructor {:can-complete? false
                               :construct []}})

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
        *constructor (cursor-in *ui [:constructor])
        pid (first @*queue)
        *phrase (cursor-in *working-set [pid])
        tp (:type @*ui)
        can-complete? (or (= tp :flashcard) (:can-complete? @*constructor))
        *speech-active? (cursor-in *ui [:speech-active?])
        speech-active? @*speech-active?
        cancel-speech! (partial cancel-speech! *speech-active?)
        reset-ui! #(do (cancel-speech!)
                       (swap! *ui merge (select-keys initial-ui types)))
        pass! #(do (swap! *queue rotate)
                   (reset-ui!))
        complete! #(do (complete-phrase! state)
                       (reset-ui!))
        activate-speech! (partial activate-speech! *speech-active?)]
    [:.training
     (ui/card
      (ui/card-text
       (case tp
         :flashcard (flashcard *phrase
                               (cursor-in *ui [:flashcard])
                               activate-speech!)
         :constructor (phrase-constructor *phrase
                                          *constructor
                                          activate-speech!)))
      (ui/card-actions
       (ui/select-field {:floating-label-text "Training"
                         :class-name "type-selector"
                         :value tp
                         :on-change #(swap! *ui assoc :type (keyword %3))}
                        (ui/menu-item {:value :flashcard
                                       :primary-text "Flashcard"})
                        (ui/menu-item {:value :constructor
                                       :primary-text "Constructor"}))
       (ui/raised-button {:label "Pass"
                          :on-touch-tap pass!})
       (ui/raised-button {:label "Complete"
                          :disabled (not can-complete?)
                          :on-touch-tap complete!})
       (ui/floating-action-button
        {:class-name "speech"
         :mini true
         :on-touch-tap #(if speech-active?
                          (cancel-speech!)
                          (activate-speech! (:targetText @*phrase)))
         :secondary speech-active?}
        (ic/av-hearing))))]))
