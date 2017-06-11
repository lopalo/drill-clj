
(ns drill.my-dictionary
  (:require [clojure.core.async :refer []]
            [rum.core :as rum :refer [defc defcs local reactive react]]
            [cljs-react-material-ui.rum :as ui]
            [cljs-react-material-ui.icons :as ic]
            [drill.common.processes :refer [api-get api-post]]
            [drill.common.mixins :refer [loading loader]]
            [drill.utils :refer [log]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(declare my-dictionary table)

(defn fetch-rows!
  [rows]
  (go (let [data (<! (api-get "my-dictionary/list" {}))]
        (reset! rows data))))

(def init
  {:did-mount
   (fn [state]
     (go (<! (fetch-rows! (::rows state)))
         (reset! (loading state) false))
     state)})

(defcs my-dictionary < (loader)
                       (local [] ::rows)
                       init
  [state]
  (table (::rows state)))


(defc table [rows]
  (ui/table
    {:selectable false}
    (ui/table-header
      {:display-select-all false
       :adjust-for-checkbox false}
      (ui/table-row
        (ui/table-header-column "Source Text")
        (ui/table-header-column "Target Text")
        (ui/table-header-column "Reset")
        (ui/table-header-column "Completion Time")
        (ui/table-header-column "Delete")))
    (ui/table-body
      {:display-row-checkbox false}
      (for [r @rows]
        (ui/table-row
          {:key (:id r)}
          (ui/table-row-column (:sourceText r))
          (ui/table-row-column (:targetText r))
          (ui/table-row-column
            (ui/floating-action-button
              {:mini true}
              (ic/av-replay)))
          (ui/table-row-column (:completionTime r))
          (ui/table-row-column
            (ui/floating-action-button
              {:mini true :secondary true}
              (ic/action-delete))))))))



