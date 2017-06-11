(ns drill.common.processes
  (:require [clojure.string :refer [blank?]]
            [clojure.core.async :refer [<!]]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def URL "http://vladio")

(defn- response [r]
  (let [status (:status r)
        body (:body r)
        description (:description body)
        error (if (not (blank? description)) description (:title body))]
    (if (and (>= status 200) (< status 300))
      body
      {:error error})))

(defn api-get [url params]
  (go (let [resp (<! (http/get (str URL "/" url) {:with-credentials? true
                                                  :query-params params}))]

        (response resp))))

(defn api-post [url params]
  (go (let [resp (<! (http/post (str URL "/" url) {:with-credentials? true
                                                   :json-params params}))]
        (response resp))))
