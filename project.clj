(defproject enchanter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main enchanter.web
  :plugins [[lein-ring "0.8.8"]]
  :ring {:handler enchanter.web/app}
  :uberjar-name "enchanter.jar"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [incanter "1.5.4"]
                 [hiccup "1.0.4"]
                 [ring/ring "1.2.1"]
                 [compojure "1.1.6"]
                 [swingrepl "1.3.0"
                  :exclusions [org.clojure/clojure
                               org.clojure/clojure-contrib]]
                 [jline/jline "2.11"]])
