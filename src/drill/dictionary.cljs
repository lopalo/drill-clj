(ns drill.dictionary
  (:require [clojure.core.async :refer []]
            [rum.core :as rum :refer [defc defcs local reactive react]]
            [cljs-react-material-ui.rum :as ui]
            [drill.common.processes :refer [api-get api-post]]
            [drill.common.mixins :refer [loading loader]]
            [drill.utils :refer [log]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(declare dictionary table filters)

(defn fetch-rows!
  ([rows filters]
   (fetch-rows! rows filters false))
  ([rows filters more]
   (go (let [data (<! (api-get "dictionary/list" @filters))]
         (reset! rows data)))))

(def initial-filters {:target-language "en"})

(def init
  {:did-mount
   (fn [state]
     (go (<! (fetch-rows! (::rows state) (::filters state)))
         (reset! (loading state) false))
     state)})

;;TODO: listen to filters' changes and handle them
(defcs dictionary < (loader)
                    (local [] ::rows)
                    (local initial-filters ::filters)
                    init
  [state]
  (ui/card (filters (::filters state))
           (table (::rows state))))

(defc filters [filters] [:div])

(defc table [rows]
  (ui/table
    {:selectable false}
    (ui/table-header
      {:display-select-all false
       :adjust-for-checkbox false}
      (ui/table-row
        (ui/table-header-column "Source Text")
        (ui/table-header-column "Target Text")
        (ui/table-header-column "Added By")
        (ui/table-header-column "In My Dictionary")))
    (ui/table-body
      {:display-row-checkbox false}
      (for [r (:list @rows)]
        (ui/table-row
          {:key (:id r)}
          (ui/table-row-column (:sourceText r))
          (ui/table-row-column (:targetText r))
          (ui/table-row-column (:addedBy r))
          (ui/table-row-column (ui/toggle {:toggled (:isInMyDict r)})))))))

;;TODO: "load more" button
