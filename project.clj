(defproject xiast "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring "1.2.1"]
                 [enlive "1.1.4"]
                 [com.taoensso/tower "2.0.1"]
                 [hiccup "1.0.4"]
                 [clj-http "0.1.3"]
                 [prismatic/schema "0.2.1"]]
  :plugins [[lein-ring "0.8.8"]]
  :main ^:skip-aot xiast.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[ring-serve "0.1.2"]
                                  [ring-mock "0.1.5"]]}}
  :ring {:handler xiast.core/app})
