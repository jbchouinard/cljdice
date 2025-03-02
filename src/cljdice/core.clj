(ns cljdice.core
  (:require [cljdice.dice :as dice]
            [cljdice.parser :as parser])
  (:gen-class))

(defn roll-dice-expression
  "Roll dice based on a dice expression string and return the result"
  [expr-str]
  (try
    (let [die (parser/eval-dice-expression expr-str)]
      (dice/roll-die die))
    (catch Exception e
      (throw (IllegalArgumentException. 
              (str "Invalid dice expression: " expr-str " - " (.getMessage e)))))))

(defn print-help
  []
  (println "cljdice - A command-line dice roller")
  (println)
  (println "Usage: cljdice [dice-expression]")
  (println)
  (println "Examples:")
  (println "  cljdice 3d6        Roll three six-sided dice")
  (println "  cljdice d20+5      Roll a twenty-sided die and add 5")
  (println "  cljdice 2d4+3d6-2  Roll two four-sided dice, add three six-sided dice, subtract 2")
  (println)
  (println "Options:")
  (println "  --help             Show this help message"))

(defn process-args
  "Process command line arguments and return the result without exiting"
  [args]
  (let [args (if (sequential? args) args [args])]
    (cond
      (or (empty? args) (= (first args) "--help"))
      (do
        (when (empty? args)
          (println "Error: Please provide a dice expression"))
        (print-help)
        0)
      
      :else
      (let [expression (first args)]
        (try
          (println (roll-dice-expression expression))
          0
          (catch IllegalArgumentException e
            (println (.getMessage e))
            1))))))

(defn -main
  [& args]
  (let [exit-code (process-args args)]
    (System/exit exit-code)))

(comment
  ;; For REPL development
  (-main "3d6")
  (-main "d20+5")
  (process-args "3d6")
  (process-args "d20+5"))
