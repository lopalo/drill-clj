(ns drill.app
  (:require [rum.core :refer [defc defcc reactive react]]
            [cljs-react-material-ui.rum :as ui]
            [cljs-react-material-ui.icons :as ic]
            [drill.app-state :refer [user route set-route!]]
            [drill.utils :refer [log]]
            [drill.auth :refer [logout]]
            [drill.dictionary :refer [dictionary]]
            [drill.my-dictionary :refer [my-dictionary]]))

(defcc right-button
  [react-comp]
  (ui/icon-menu {:style (-> react-comp .-props .-style)
                 :icon-button-element
                 (ui/flat-button {:label (:name @user)
                                  :secondary true
                                  :icon (ic/action-account-circle)})}
                (ui/menu-item {:primary-text "Settings"})
                (ui/menu-item {:primary-text "Log Out"
                               :on-touch-tap logout})))

(-> (meta right-button)
    :rum/class
    (aset "muiName" "FlatButton"))

(def tabs #{"training"
            "my-dictionary"
            "dictionary"})

(defc app
  < reactive
  []
  [:div
   (ui/app-bar {:title "Drill"
                :show-menu-icon-button false
                :icon-element-right (right-button)})
   (ui/tabs {:value (-> route react first tabs (or "my-dictionary"))
             :on-change set-route!}
            (ui/tab {:label "training" :value "training"}
                    (ui/card (ui/card-title {:title "Training"})))
            (ui/tab {:label "my dictionary" :value "my-dictionary"}
                    (ui/card (my-dictionary)))
            (ui/tab {:label "dictionary" :value "dictionary"}
                    (dictionary)))])
