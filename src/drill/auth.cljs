(ns drill.auth
  (:require [clojure.set :refer [rename-keys]]
            [clojure.core.async :refer [<!]]
            [rum.core :refer [defc defcs local react reactive]]
            [cljs-react-material-ui.core :refer [color]]
            [cljs-react-material-ui.rum :as ui]
            [drill.form :as f]
            [drill.validators :as v]
            [drill.utils :refer [log]]
            [drill.app-state :refer [user]]
            [drill.common.processes :refer [api-get api-post]]
            [drill.common.mixins :refer [loading loader]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(declare auth-forms login register)

(defn set-user! [usr]
  (reset! user usr))

(defn fetch-user! []
  (go (let [data (<! (api-get "auth/user" {}))]
        (when (-> data :error nil?)
          (set-user! data)))))

(def init
  {:did-mount
   (fn [state]
     (go (<! (fetch-user!))
         (reset! (loading state) false))
     state)})

(defn logout []
  (go (let [data (<! (api-post "auth/logout" {}))]
        (when (-> data :error nil?)
          (set-user! nil)))))

(defc e-mail-field < reactive
  [value error]
  (ui/text-field {:floating-label-text "E-mail"
                  :id "e-mail"
                  :class-name :field
                  :error-text error
                  :value (react value)
                  :on-change (fn [_ v] (reset! value v))}))

(defc password-field < reactive
  [value error]
  (ui/text-field {:floating-label-text "Password"
                  :id "password"
                  :class-name :field
                  :type "password"
                  :error-text error
                  :value (react value)
                  :on-change (fn [_ v] (reset! value v))}))

(defc auth
  < (loader) init
  []
  [:.auth (login) (register)])

(def login-form [{:name :e-mail :validator v/e-mail :initial-value ""}
                 {:name :password :validator v/password :initial-value ""}])

(defcs login < (f/local-form login-form ::form)
  [state]
  (let [form (::form state)
        error (f/submit-error form)
        convert #(rename-keys % {:e-mail :email})
        try-submit!
        #(do (f/show-errors! form)
             (when (f/valid? form)
               (go (let [resp (<! (f/submit! form "auth/login" convert))]
                     (when (-> resp :error nil?)
                       (set-user! (:user resp)))))))]

    (ui/card
     {:class-name :auth-form}
     (ui/card-title {:title "Login"
                     :title-color (color :cyan-500)
                     :subtitle error
                     :subtitle-color (color :pink-700)})
     (ui/card-text
      (f/field form :e-mail e-mail-field)
      (f/field form :password password-field))
     (ui/card-actions
      (ui/raised-button {:label "Submit"
                         :disabled (not (f/can-submit? form))
                         :on-touch-tap try-submit!})))))



;TODO: expandable card for register
(defc register [] [:div])
