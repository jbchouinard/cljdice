(ns cljdice.core
  (:require [cljdice.dice :as dice]
            [cljdice.parser :as parser]
            [cljdice.version :as version]
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

(defn pprint-df
  "Pretty prints a probability distribution (PDF or CDF) with percentages instead of ratios.
   
   Parameters:
   - dist: A map where keys are outcomes and values are probabilities
   - title: Optional title to display before the distribution
   - decimal-places: Number of decimal places to show (default: 2)"
  ([dist] (pprint-df dist nil 2))
  ([dist title] (pprint-df dist title 2))
  ([dist title decimal-places]
   (when title
     (println title))
   (if (empty? dist)
     (println "No data to display.")
     (let [sorted-dist (sort-by first (seq dist))
           max-outcome-width (apply max (map (comp count str first) sorted-dist))
           outcome-format (str "%" max-outcome-width "d")
           percent-format (str "%6." decimal-places "f%%")]
       (doseq [[outcome prob] sorted-dist]
         (println (format (str outcome-format ": " percent-format)
                          outcome
                          (* 100.0 (double prob)))))))))

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
   ["-p" "--parse" "Show parsed dice expression instead of rolling"]
   ["-e" "--exact" "Use exact calculation for large number of dice (disables normal approximation)"]
   ["-v" "--version" "Show version information"]
   [nil, "--pdf" "Show probability distribution function of the dice expression"]
   [nil, "--cdf" "Show cumulative distribution function of the dice expression"]])

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
      (throw (IllegalArgumentException. (string/join \newline errors)))
      
      (:help options)
      (println (usage (:summary opts)))
      
      (:version options)
      (println (version/version-string))
      
      :else
      (let [die (eval-dice-expression expr)]
        (cond
          (:parse options)
          (pprint/pprint die)

          (:pdf options)
          (pprint-df (dice/die-pdf die)
                     (str "Probability Distribution for: " expr)
                     6)

          (:cdf options)
          (pprint-df (dice/die-cdf die)
                     (str "Cumulative Distribution for: " expr)
                     6)

          :else
          (println (dice/roll-die die)))))))

(defn -main
  [& args]
  (try
    (run args)
    (catch IllegalArgumentException e
      (println (.getMessage e))
      (System/exit 1))
    (catch Exception e
      (println "An unexpected error occurred:" (.getMessage e))
      (System/exit 1))))

(comment
  ;; For REPL development
  (-main "3d6")
  (-main "d20+5")
  (run "3d6")
  (run "d20+5"))
