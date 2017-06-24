(ns drill.training.phrase-constructor
  (:require [clojure.string :as s]
            [clojure.set :refer [difference]]
            [rum.core :as rum :refer [defc
                                      defcs
                                      reactive
                                      react
                                      cursor-in]]
            [cljs-react-material-ui.core :refer [color]]
            [cljs-react-material-ui.rum :as ui]
            [cljs-react-material-ui.icons :as ic]))


(defc source-text [text]
  [:.source-text text])

(defc construction [target-words construct delete-word!]
  (let [word-pairs (map vector target-words construct)
        butlast* (butlast word-pairs)
        last* (last word-pairs)
        ok-color (color :light-green-300)
        err-color (color :red-300)
        clr #(if (= %1 %2) ok-color err-color)]
    [:.blocks
     (when (seq butlast*)
       (for [[target-word word] butlast*]
         (ui/chip {:key word
                   :background-color (clr target-word word)}
                  word)))
     (when last*
       (let [[target-word word] last*]
         (ui/chip {:on-touch-tap delete-word!
                   :background-color (clr target-word word)}
                  word)))]))

(defc blocks [target-words construct add-word!]
  ;TODO: fix the case when the same word appears multiple times
  (let [words (difference (set target-words) (set construct))]
    [:.blocks
     (for [word (sort-by (comp s/join reverse) words)]
       (ui/chip {:key word
                 :on-touch-tap (partial add-word! word)}
                word))]))


(defcs phrase-constructor < reactive
  [state *phrase *ui activate-speech!]
  (let [p (react *phrase)
        target-words (s/split (:targetText p) #" ")
        *construct (cursor-in *ui [:construct])
        construct (react *construct)
        *can-complete? (cursor-in *ui [:can-complete?])
        update-status! #(reset! *can-complete?
                                (= @*construct target-words))
        add-word! #(do (swap! *construct conj %)
                       (update-status!))
        delete-word! #(do (swap! *construct pop)
                          (update-status!))]
    (ui/paper {:z-depth 2
               :class-name "constructor"}
              (source-text (:sourceText p))
              (construction target-words construct delete-word!)
              (blocks target-words construct add-word!))))
