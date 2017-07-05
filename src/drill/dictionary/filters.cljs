(ns drill.dictionary.filters
  (:require [clojure.core.async :refer [<!]]
            [rum.core :as rum :refer [defcs local reactive react]]
            [cljs-react-material-ui.rum :as ui]
            [drill.utils :refer [log]]
            [drill.common.processes :refer [api-get]]
            [drill.app-state :refer [languages]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def filters-mx
  {:did-mount
   (fn [state]
     (go (let [sections-c (api-get "dictionary/grammar-sections" {})
               themes-c (api-get "dictionary/themes" {})
               sections (<! sections-c)
               themes (<! themes-c)]
           (reset! (::grammar-sections state) (vals sections))
           (reset! (::themes state) (vals themes))))
     state)})

(defcs filters < reactive
                 (local [] ::grammar-sections)
                 (local [] ::themes)
                 filters-mx
  [state *filters]
  (let [f (react *filters)
        grammar-sections @(::grammar-sections state)
        themes @(::themes state)
        set-filter! #(swap! *filters assoc %1 %2)]
    [:.filters
     (ui/select-field
      {:floating-label-text "Target language"
       :value (:target-language f)
       :on-change #(set-filter! :target-language %3)}
      (for [lang languages]
        (ui/menu-item {:key lang
                       :value lang
                       :primary-text lang})))
     (ui/select-field
      {:floating-label-text "Grammar section"
       :value (:grammar-section f)
       :on-change #(set-filter! :grammar-section %3)}
      (ui/menu-item {:key "all"
                     :value nil})
      (for [gs grammar-sections :let [id (:id gs)]]
        (ui/menu-item {:key id
                       :value id
                       :primary-text (:title gs)})))
     (ui/select-field
      {:floating-label-text "Theme"
       :value (:theme f)
       :on-change #(set-filter! :theme %3)}
      (ui/menu-item {:key "all"
                     :value nil})
      (for [th themes :let [id (:id th)]]
        (ui/menu-item {:key id
                       :value id
                       :primary-text (:title th)})))
     (ui/text-field {:floating-label-text "Text"
                     :input-style {:top "-14px" :marginTop "29px"}
                     :type "text"
                     :value (:text f)
                     :on-change #(set-filter! :text %2)})]))
