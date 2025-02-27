(ns cljdice.core
  (:require [babashka.cli :as cli]
            [clojure.string :as str])
  (:gen-class))

(defn roll-dice
  "Roll n dice with s sides each"
  [n s]
  (repeatedly n #(inc (rand-int s))))

(defn format-roll-result
  "Format the results of a dice roll"
  [dice-results]
  (let [total (reduce + dice-results)]
    (format "Rolled: %s\nTotal: %d"
            (str/join ", " dice-results)
            total)))

(def cli-options
  {:dice {:default 1
          :coerce :long
          :desc "Number of dice to roll"}
   :sides {:default 6
           :coerce :long
           :desc "Number of sides on each die"}
   :help {:coerce :boolean
          :desc "Show this help message"}})

(defn print-help []
  (println "cljdice - A simple command-line dice roller")
  (println)
  (println "Usage: cljdice [options]")
  (println)
  (println "Options:")
  (println (cli/format-opts {:spec cli-options})))

(defn -main [& args]
  (let [opts (cli/parse-opts args {:spec cli-options})
        {:keys [dice sides help]} opts]
    (cond
      help (print-help)
      
      (or (< dice 1) (< sides 1))
      (do
        (println "Error: Both dice and sides must be positive numbers")
        (System/exit 1))
      
      :else
      (-> (roll-dice dice sides)
          format-roll-result
          println)))
  (System/exit 0))

(comment
  ;; For REPL development
  (-main "--dice" "3" "--sides" "20")
  (-main "--help")
  )
