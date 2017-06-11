(ns drill.utils)

(defn wrap-react-class [cls]
  (letfn [(component
            ([arg]
             (if (map? arg)
               (component arg ())
               (component {} arg)))
            ([props children]
             (. js/React (createElement
                          cls
                          (clj->js props) ;TODO: translate camel to kebab case
                          (clj->js children)))))]
    component))

(defn log [& args] (apply (.-log js/console) args))
