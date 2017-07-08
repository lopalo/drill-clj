(ns drill.settings
  (:require [rum.core :refer [defc reactive react local]]
            [cljs-react-material-ui.rum :as ui]
            [drill.app-state :refer [languages *profile set-profile-field!]]))

(declare working-set-size)

(defc settings < reactive
  [*show-settings]
  (let [close! #(reset! *show-settings false)
        close-btn (ui/flat-button {:label "Ok"
                                   :primary true
                                   :on-touch-tap close!})
        profile (react *profile)]
    (ui/dialog
     {:title "Settings"
      :open (react *show-settings)
      :auto-scroll-body-content true
      :actions [close-btn]
      :modal false
      :on-request-close close!}
     (ui/select-field
      {:floating-label-text "Speak language"
       :value (:speakLanguage profile)
       :on-change #(set-profile-field! :speakLanguage %3)}
      (for [lang languages]
        (ui/menu-item {:key lang
                       :value lang
                       :primary-text lang})))
     (working-set-size profile))))

(defc working-set-size [profile]
  (let [ws-size (:workingSetSize profile)]
    [:div
     "Working set size: "
     ws-size
     (ui/slider {:value ws-size
                 :on-change #(set-profile-field! :workingSetSize %2)
                 :min 5
                 :max 15
                 :step 1
                 :axis "x"})]))

