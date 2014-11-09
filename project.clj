(defproject quand "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.2.0"]
                 [ring/ring-defaults "0.1.2"]
                 [prone "0.6.0"]
                 [http-kit "2.1.16"]
                 [hiccup "1.0.5"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [cheshire "5.3.1"]]
  :plugins [[lein-ring "0.8.13"]
            [cider/cider-nrepl "0.8.0-SNAPSHOT"]]
  :main quand.core.handler
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
