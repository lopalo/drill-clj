(ns drill.utils
  (:require [sablono.interpreter :refer [interpret]]
            [camel-snake-kebab.core :refer [->camelCase]]
            [camel-snake-kebab.extras :refer [transform-keys]]))

(defn map-vals [f coll]
  (reduce-kv (fn [m k v] (assoc m k (f v)))
             (empty coll) coll))

(defn wrap-react-class [cls]
  (fn [& args]
    (let [[props children] (if (map? (first args))
                             [(first args) (rest args)]
                             [{} args])
          transform-key #(if (symbol? %) % (->camelCase %))]
      (apply js/React.createElement
             cls
             (->> props
                  (map-vals interpret)
                  (transform-keys transform-key)
                  clj->js)
             (->> children interpret clj->js)))))

(defn log [& args] (apply (.-log js/console) args))
