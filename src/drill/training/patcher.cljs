
(ns drill.training.patcher
  (:require [clojure.string :as s]
            [clojure.core.async :refer [<!]]
            [sablono.core :refer [html]]
            [rum.core :as rum :refer [defc
                                      defcs
                                      reactive
                                      local
                                      react
                                      cursor-in]]
            [cljs-react-material-ui.rum :as ui]
            [cljs-react-material-ui.icons :as ic]
            [drill.common.processes :refer [api-patch]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defc buttons [*flipped save!]
  [:div
   (ui/floating-action-button
    {:class-name "icon-button" :mini true
     :on-touch-tap #(swap! *flipped not)}
    (ic/action-autorenew))
   (ui/floating-action-button
    {:class-name "icon-button"
     :mini true
     :on-touch-tap save!}
    (ic/content-save))])

(defcs patcher
  < reactive
  < (local false ::flipped)
  < (local "" ::message)
  [state *phrase]
  (let [p (react *phrase)
        *flipped (::flipped state)
        *message (::message state)
        flipped @*flipped
        field (if flipped :targetText :sourceText)
        save! #(go (let [r (<! (api-patch (str "dictionary/phrase/" (:id p))
                                          {field (field p)}))
                         msg (if (:error r) "Failure" "Saved")]
                     (reset! *message msg)))]
    (ui/paper {:z-depth 5
               :class-name "flashcard"
               :id (name field)}
              (html
               [:textarea.patcher-text
                {:id "text"
                 :rows 3
                 :value (field p)
                 :on-change #(swap! *phrase assoc field
                                    (.. % -target -value))}])
              (buttons *flipped save!)
              (ui/snackbar {:message @*message
                            :open (not (s/blank? @*message))
                            :auto-hide-duration 2000
                            :on-request-close #(reset! *message "")}))))
