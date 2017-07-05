(ns drill.utils
  (:require [sablono.interpreter :refer [interpret]]
            [camel-snake-kebab.core :refer [->camelCase]]
            [camel-snake-kebab.extras :refer [transform-keys]]))

(defn wrap-react-class [cls]
  (fn [& args]
    (let [[props children] (if (map? (first args))
                             [(first args) (rest args)]
                             [{} args])
          convert #(if (vector? %) (interpret %) %)
          convert-all (partial map #(if (seq? %)
                                      (map convert %)
                                      (convert %)))]

      (apply js/React.createElement
             cls
             (->> props (transform-keys ->camelCase) clj->js)
             (->> children convert-all clj->js)))))

(defn log [& args] (apply (.-log js/console) args))
