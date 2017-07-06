(defproject drill "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.562"]
                 [org.clojure/core.async  "0.3.442"
                  :exclusions [org.clojure/tools.reader]]
                 [rum "0.10.8" :exclusions [cljsjs/react cljsjs/react-dom]]
                 [cljs-react-material-ui "0.2.44"]
                 [cljs-http "0.1.43"]
                 [camel-snake-kebab "0.4.0"]
                 [garden "1.3.1"]]

  :plugins [[lein-figwheel "0.5.10"]
            [lein-cljsbuild "1.1.6" :exclusions [[org.clojure/clojure]]]
            [lein-garden "0.3.0"]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                ;; the presence of a :figwheel configuration here
                ;; will cause figwheel to inject the figwheel client
                ;; into your build
                :figwheel {:on-jsload "drill.core/on-js-reload"
                           ;; :open-urls will pop open your application
                           ;; in the default browser once Figwheel has
                           ;; started and complied your application.
                           ;; Comment this out once it no longer serves you.
                           :open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main drill.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/drill.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]
                           :external-config
                           {:devtools/config
                            {:features-to-install [:formatters :hints :async]}}}}
               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/drill.js"
                           :main drill.core
                           :optimizations :advanced
                           :infer-externs true
                           :parallel-build true
                           :pretty-print false}}]}

  :garden {:builds [{:id "training"
                     :source-paths ["src"]
                     :stylesheet styles.core/training
                     :compiler {:output-to
                                "resources/public/css/compiled/training.css"
                                :pretty-print? true}}]}

  :figwheel {;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"
             :css-dirs ["resources/public/css"]} ;; watch and update CSS

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.2"]
                                  [figwheel-sidecar "0.5.10"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   ;; need to add dev source path here to get user.clj loaded
                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   ;; need to add the compliled assets to the :clean-targets
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     "resources/public/css/compiled"
                                                     :target-path]}})
