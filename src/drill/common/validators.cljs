(ns drill.common.validators
  (:require [clojure.string :as s]))

(defn not-blank [field values]
  (let [v (field values)]
    (when (or (not v) (s/blank? v)) "Blank field")))

(def e-mail not-blank)
(def password not-blank)
