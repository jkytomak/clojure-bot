(defproject codecamp-2015-2 "0.1.0-SNAPSHOT"
  :description "Reaktor CodeCamp 2015"
  :dependencies [[org.clojure/clojure "1.7.0"]

                 ;; UDP
                 [aleph "0.4.0"]

                 ;; JSON
                 [cheshire "5.5.0"]

                 ;; Testing
                 [speclj "3.3.1"]

                 ;; Utils
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.3"]]
  :main codecamp2015.main
  :target-path "target/%s"

  :prep-tasks [
       "javac"
       "compile"
  ]

  :plugins [[speclj "3.3.1"]
            [lein-environ "1.0.0"]
            [lein-shell "0.4.0"]
            [lein-auto "0.1.2"]
            [lein-ancient "0.6.7"]]

  :test-paths ["spec"]

  :aliases {"buildfront" ^{:doc "Build frontend code with npm"}
            ["do" ["shell" "npm" "install"] ["shell" "npm" "run" "build"]]})
