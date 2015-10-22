(ns codecamp2015.main
  (:use [codecamp2015.bot :only [start-bot]]))

(defn -main [& args]
  (start-bot "bendersgame" "localhost" 4567 5678))
