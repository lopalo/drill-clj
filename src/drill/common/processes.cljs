(ns drill.common.processes
  (:require [clojure.string :refer [blank?]]
            [clojure.core.async :refer [<!]]
            [cljs-http.client :as http])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(def URL (let [host (. js/location -hostname)]
           (str "http://" (if (= host "localhost") "vladio" host))))

(defn- response [r]
  (let [status (:status r)
        body (:body r)
        description (:description body)
        error (if (not (blank? description)) description (:title body))]
    (if (and (>= status 200) (< status 300))
      body
      {:error (or error (:error-code r))})))

(defn request [method url params-key params]
  (go (let [resp (<! (method (str URL "/" url) {:with-credentials? true
                                                params-key params}))]
        (response resp))))

(defn api-get [url params] (request http/get url :query-params params))

(defn api-post [url params] (request http/post url :json-params params))

(defn api-patch [url params] (request http/patch url :json-params params))

(defn api-delete [url params] (request http/delete url :query-params params))

