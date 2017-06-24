(ns drill.training.flashcard
  (:require [clojure.string :as s]
            [rum.core :as rum :refer [defc
                                      defcs
                                      reactive
                                      react
                                      cursor-in]]
            [cljs-react-material-ui.rum :as ui]
            [cljs-react-material-ui.icons :as ic]))


(defc flip-button [*flipped]
  [:.flip-button
   (ui/floating-action-button
    {:on-touch-tap #(swap! *flipped not)}
    (ic/action-autorenew))])

(defc card-text [text activate-speech!]
  [:.card-text
   (for [[idx word] (map-indexed vector (s/split text #" "))
         :let [speech! #(when activate-speech! (activate-speech! word))]]
     (ui/flat-button {:key (str idx word)
                      :label word
                      :on-touch-tap speech!}))])

(defcs flashcard < reactive
  [state *phrase *ui activate-speech!]
  (let [p (react *phrase)
        *flipped (cursor-in *ui [:flipped])
        flipped @*flipped
        text ((if flipped :targetText :sourceText) p)]
    (ui/paper {:z-depth 5
               :class-name "flashcard"}
              (card-text text (when flipped activate-speech!))
              (flip-button *flipped))))
