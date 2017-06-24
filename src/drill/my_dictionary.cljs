(ns drill.my-dictionary
  (:require [clojure.core.async :refer [<! put!]]
            [rum.core :as rum :refer [defc defcs local reactive react]]
            [cljs-react-material-ui.rum :as ui]
            [cljs-react-material-ui.icons :as ic]
            [drill.common.processes :refer [api-get
                                            api-post
                                            api-patch
                                            api-delete]]
            [drill.common.mixins :refer [wrap-load
                                         loader-mx
                                         tab-mx
                                         cleanup-mx]]
            [drill.utils :refer [log]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(declare my-dictionary table)

(defn fetch-rows!
  [*rows]
  (go (let [data (<! (api-get "my-dictionary/list" {}))]
        (reset! *rows data))))

(defn do-action! [*rows idx action]
  (let [id (get-in @*rows [idx :id])
        reset {:progress 0 :completionTime nil}]
    (case action
      :reset
      (do (api-patch (str "my-dictionary/phrase/" id) reset)
          (swap! *rows update idx #(merge % reset)))
      :delete
      (go (<! (api-delete (str "my-dictionary/phrase/" id) {}))
          (fetch-rows! *rows)))))

(def load! (wrap-load #(fetch-rows! (::rows %))))

(defcs my-dictionary < (loader-mx)
                       cleanup-mx
                       (local [] ::rows)
                       (tab-mx load!)
  [state]
  (table (::rows state)))

(defn text-header-col [text]
  (ui/table-header-column {:style {:white-space "normal" :width "32%"}} text))

(defn text-row-col [text]
  (ui/table-row-column {:style {:white-space "normal" :width "32%"}} text))

(defn btn-header-col [text]
  (ui/table-header-column {:style {:white-space "normal" :width "10%"}} text))

(defn btn-row-col [btn]
  (ui/table-row-column {:style {:white-space "normal" :width "10%"}} btn))

(defc table [*rows]
  (ui/table
   {:selectable false}
   (ui/table-header
    {:display-select-all false
     :adjust-for-checkbox false}
    (ui/table-row
     (text-header-col "Source Text")
     (text-header-col "Target Text")
     (btn-header-col "Reset")
     (ui/table-header-column "Completion Time")
     (btn-header-col "Delete")))
   (ui/table-body
    {:display-row-checkbox false}
    (for [[idx r] (map-indexed vector @*rows)
          :let [compl-time (:completionTime r)]]
      (ui/table-row
       {:key (:id r)}
       (text-row-col (:sourceText r))
       (text-row-col (:targetText r))
       (btn-row-col
        (ui/floating-action-button
         {:mini true
          :disabled (not compl-time)
          :on-touch-tap #(do-action! *rows idx :reset)}
         (ic/av-replay)))
       (ui/table-row-column {:style {:text-overflow "initial"
                                     :white-space "normal"}}
                            compl-time)
       (btn-row-col
        (ui/floating-action-button
         {:mini true
          :secondary true
          :on-touch-tap #(do-action! *rows idx :delete)}
         (ic/action-delete))))))))

