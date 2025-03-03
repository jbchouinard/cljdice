(ns cljdice.core
  (:require [cljdice.dice :as dice]
            [cljdice.parser :as parser]
            [clojure.pprint :as pprint]
            [clojure.string :as string]
            [clojure.tools.cli :as cli])
  (:gen-class))

(defn eval-dice-expression
  "Evaluate dice expression, raising IllegalArgumentException if invalid."
  [expr-str]
  (try
    (parser/eval-dice-expression expr-str)
    (catch Exception e
      (throw (IllegalArgumentException.
              (str "Invalid dice expression \"" expr-str "\": " (.getMessage e)))))))

(defn usage [options-summary]
  (->> ["cljdice - Virtual dice roller"
        ""
        "Usage: cljdice expression"
        ""
        "Options:"
        options-summary
        ""
        "Dice expression examples:"
        "  d6"
        "  2d6+1d4"
        "  2d12+2"]
       (string/join \newline)))

(def cli-options
  [["-h" "--help" "Show help message"]
   ["-s" "--show" "Show parsed dice expression instead of rolling"]
   ["-e" "--exact" "Use exact calculation for large number of dice (disables normal approximation)"]])

(defn run
  [args]
  (let [opts (cli/parse-opts args cli-options)
        options (:options opts)
        errors (:errors opts)
        expr (or (first (:arguments opts)) "")]
    (when (:exact options)
      (swap! dice/*config* assoc :use-exact-calculation true))
    
    (cond
      errors
      (do
        (println (string/join \newline errors))
        0)
      (:help options)
      (do
        (println (usage (:summary opts)))
        0)
      :else
      (try (let [die (eval-dice-expression expr)]
             (if (:show options)
               (pprint/pprint die)
               (println (dice/roll-die die))))
           0
           (catch IllegalArgumentException e
             (println (.getMessage e))
             1)))))

(defn -main
  [& args]
  (System/exit (run args)))

(comment
  ;; For REPL development
  (-main "3d6")
  (-main "d20+5")
  (run "3d6")
  (run "d20+5"))
