(ns drill.form
  (:require [clojure.core.async :refer [<!]]
            [rum.core :refer [local cursor-in]]
            [drill.common.processes :refer [api-post]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(declare error*)

(def initial-state {:initial-values {}
                    :values {}
                    :validators {}
                    :show-errors false
                    :submit-error nil
                    :status :initial})

(def statuses #{:initial :pending :submitted :submit-failure})

(defn create-form [fields-spec]
  (let [collect #(into {} (map (juxt :name %) fields-spec))
        initial-values (collect :initial-value)
        validators (collect :validator)]
    (-> initial-state
        (assoc :initial-values initial-values)
        (assoc :values initial-values)
        (assoc :validators validators))))

(defn local-form [fields-spec key]
  (local (create-form fields-spec) key))

(defn valid? [form]
  (let [form-state @form
        field-names (keys (:validators form-state))]
    (every? nil? (for [field-name field-names]
                   (error* form-state field-name)))))

(defn status? [form status]
  (assert (statuses status) statuses)
  (= status (:status @form)))

(defn errors? [form]
  (and (:show-errors @form)
       (not (valid? form))))

(defn can-submit? [form]
  (not (or (status? form :pending) (errors? form))))

(defn submit-error [form]
  (:submit-error @form))

(defn set-status! [form status]
  (assert (statuses status) statuses)
  (swap! form assoc :status status))

(defn fail-submit! [form error]
  (swap! form #(-> %
                   (assoc :status :submit-failure)
                   (assoc :submit-error error))))

(defn show-errors!
  ([form] (show-errors! form true))
  ([form value] (swap! form assoc :show-errors value)))

(defn error* [form-state field-name]
  (let [validator (get-in form-state [:validators field-name])]
    (when validator
      (validator field-name (:values form-state)))))

(defn error
  [form field-name]
  (when (:show-errors @form)
    (error* @form field-name)))

(defn field-val [form field-name]
  (get-in @form [:values field-name]))

(defn field-atom [form field-name]
  (cursor-in form [:values field-name]))

(defn field [form field-name component]
  (component (field-atom form field-name)
             (error form field-name)))

(defn reset-form! [form]
  (let [initial-values (:initial-values @form)]
    (swap! form #(-> %
                     (assoc :values initial-values)
                     (assoc :show-errors false)
                     (assoc :submit-error nil)))))

(defn submit!
  ([form url] (submit! form url identity))
  ([form url convert]
   (set-status! form :pending)
   (go (let [form-data (-> form deref :values convert)
             resp (<! (api-post url form-data))
             error (:error resp)]
         (if error
           (fail-submit! form error)
           (do
             (set-status! form :submitted)
             (reset-form! form)))
         resp))))
