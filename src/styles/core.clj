(ns styles.core
  (:require [garden.def :refer [defstyles]]
            [garden.units :refer [px em]]))


(def type-selector
  [:.type-selector {:position "absolute !important"
                    :left (px 16)
                    :width "9em !important"
                    :bottom 0
                    :text-align 'initial}])


(def icon-button
  [:.icon-button {:margin-left (em 1)
                  :position 'relative
                  :bottom (px -10)}])

(def flashcard
  [:.flashcard
   {:padding (em 1)
    :max-width (px 800)
    :margin 'auto}
   [:.card-text
    {:margin-bottom (em 1)
     :font-size (px 20)}
    [:button
     {:min-width "0 !important"}
     [:span
      {:padding-left "0.5em !important"
       :padding-right "0.5em !important"}]]]])


(def constructor
  [:.constructor
   {:padding (em 1)}
   [:.source-text {:font-size (px 18)}]
   [:.blocks
    {:display 'flex
     :flex-wrap 'wrap
     :justify-content 'center
     :margin (em 1)}
    [:div {:margin "2px !important"}]]])

(defstyles training
  [:.training
   {:text-align 'center}
   type-selector
   icon-button
   flashcard
   constructor])
