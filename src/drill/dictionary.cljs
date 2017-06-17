(ns drill.dictionary
  (:require [clojure.core.async :refer [<!]]
            [rum.core :as rum :refer [defc defcs local reactive react]]
            [cljs-react-material-ui.rum :as ui]
            [drill.common.processes :refer [api-get api-post api-delete]]
            [drill.common.mixins :refer [wrap-load loader-mx tab-mx]]
            [drill.utils :refer [log]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(declare dictionary table filters)

(defn fetch-table-data!
  ([state]
   (fetch-table-data! state false))
  ([state more]
   (go (let [params @(::filters state)
             *table-data (::table-data state)
             params (if more
                      (assoc params :offset (count (:list @*table-data)))
                      params)
             data (<! (api-get "dictionary/list" params))]
         (if more
           (swap! *table-data update :list into (:list data))
           (reset! *table-data data))))))

(defn set-in-my-dict! [*table-data idx value]
  (let [id (get-in @*table-data [:list idx :id])]
    (if value
      (api-post "my-dictionary/add-phrase" {:id id})
      (api-delete (str "my-dictionary/phrase/" id) {}))
    (swap! *table-data assoc-in [:list idx :isInMyDict] value)))

(def initial-filters {:target-language "en"})

(def load! (wrap-load fetch-table-data!))

;;TODO: listen to filters' changes and handle them
(defcs dictionary < (loader-mx)
                    (local [] ::table-data)
                    (local initial-filters ::filters)
                    (tab-mx load!)
  [state]
  (let [*table-data (::table-data state)]
    (ui/card (filters (::filters state))
             (table *table-data)
             (ui/raised-button
               {:label "Load More"
                :disabled (= (count (:list @*table-data)) (:total @*table-data))
                :on-touch-tap #(fetch-table-data! state true)}))))

(defc filters [filters] [:div])

;TODO: fix text overflow and column width
(defc table [*table-data]
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
      (for [[idx r] (->> @*table-data :list (map-indexed vector))]
        (ui/table-row
          {:key (:id r)}
          (ui/table-row-column (:sourceText r))
          (ui/table-row-column (:targetText r))
          (ui/table-row-column (:addedBy r))
          (ui/table-row-column
           (ui/toggle {:toggled (:isInMyDict r)
                       :onToggle #(set-in-my-dict! *table-data idx %2)})))))))

