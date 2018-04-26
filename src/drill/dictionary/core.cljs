(ns drill.dictionary.core
  (:require [clojure.core.async :refer [<!]]
            [rum.core :as rum :refer [defc defcs local reactive react]]
            [cljs-react-material-ui.rum :as ui]
            [drill.common.processes :refer [api-get api-post api-delete]]
            [drill.common.mixins :refer [wrap-load
                                         loader-mx
                                         tab-mx
                                         cleanup-mx]]
            [drill.app-state :refer [languages]]
            [drill.dictionary.filters :refer [filters]]
            [drill.utils :refer [log]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(declare dictionary table)

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

(def initial-filters
  {:target-language (first languages)
   :grammar-section nil
   :theme nil
   :text ""})

(def load! (wrap-load fetch-table-data!))

(defn sync-filters! [state old-filters new-filters]
  (when (not= old-filters new-filters)
    (fetch-table-data! state)))

(def dictionary-mx
  {:will-mount
   (fn [state]
     (-> state ::filters (add-watch ::sync #(sync-filters! state %3 %4)))
     state)})

(defc load-more-btn [*table-data load-more!]
  [:.load-more
   (ui/raised-button
    {:label "Load More"
     :disabled (= (count (:list @*table-data)) (:total @*table-data))
     :on-touch-tap load-more!})])

(defcs dictionary
  < (loader-mx)
  < cleanup-mx
  < (local [] ::table-data)
  < (local initial-filters ::filters)
  < (tab-mx load!)
  < dictionary-mx
  [state]
  (let [*table-data (::table-data state)
        load-more! #(fetch-table-data! state true)]
    (ui/card (filters (::filters state))
             (table *table-data)
             (load-more-btn *table-data load-more!))))

(defn text-header-col [text]
  (ui/table-header-column {:style {:white-space "normal" :width "35%"}} text))

(defn text-row-col [text]
  (ui/table-row-column {:style {:white-space "normal" :width "35%"}} text))

(defc table [*table-data]
  (ui/table
   {:selectable false}
   (ui/table-header
    {:display-select-all false
     :adjust-for-checkbox false}
    (ui/table-row
     (text-header-col "Source Text")
     (text-header-col "Target Text")
     (ui/table-header-column "Added By")
     (ui/table-header-column {:style {:white-space "normal"}}
                             "In My Dictionary")))
   (ui/table-body
    {:display-row-checkbox false}
    (for [[idx r] (->> @*table-data :list (map-indexed vector))]
      (ui/table-row
       {:key (:id r)}
       (text-row-col (:sourceText r))
       (text-row-col (:targetText r))
       (ui/table-row-column (:addedBy r))
       (ui/table-row-column
        (ui/toggle {:toggled (:isInMyDict r)
                    :onToggle #(set-in-my-dict! *table-data idx %2)})))))))
