(ns cljdice-dev.stats
  (:require [cljdice.stats :as stats]))

(defn normal-approximation-distribution
  "Generates a discrete approximation of the normal distribution for dice rolls.
   
   Parameters:
   - n: Number of dice
   - sides: Number of sides per die
   - min-value: Minimum possible sum (usually n)
   - max-value: Maximum possible sum (usually n*sides)
   
   Returns a map of possible outcomes to their probabilities according to normal approximation."
  [n sides]
  (let [[mean stddev] (stats/uniform-dice-stats n sides)
        min-value n
        max-value (* n sides)
        outcomes (range min-value (inc max-value))
        
        ;; Calculate normal PDF for each outcome
        pdf-values (map (fn [x]
                          (let [z (/ (- x mean) stddev)
                                pdf-value (/ (Math/exp (/ (* z z) -2))
                                            (* stddev (Math/sqrt (* 2 Math/PI))))]
                            [x pdf-value]))
                        outcomes)
        
        ;; Create map and normalize to ensure probabilities sum to 1
        unnormalized-dist (into {} pdf-values)
        total (reduce + (vals unnormalized-dist))]
    
    (reduce-kv (fn [m k v] (assoc m k (/ v total))) 
               {} 
               unnormalized-dist)))

(defn kolmogorov-smirnov-statistic
  "Calculates the Kolmogorov-Smirnov statistic between two discrete probability distributions.
   This measures the maximum absolute difference between their cumulative distribution functions."
  [dist1 dist2]
  (let [all-outcomes (-> (concat (keys dist1) (keys dist2))
                         distinct
                         sort)
        
        ;; Create a function to safely get probability (default to 0 if missing)
        safe-get (fn [dist outcome] (get dist outcome 0.0))
        
        ;; Calculate CDFs for both distributions over the same set of outcomes
        probs1 (map #(safe-get dist1 %) all-outcomes)
        probs2 (map #(safe-get dist2 %) all-outcomes)
        
        cdf1 (reductions + probs1)
        cdf2 (reductions + probs2)
        
        ;; Calculate absolute differences between CDFs
        differences (map #(Math/abs (- %1 %2)) cdf1 cdf2)]
    
    ;; Return the maximum difference
    (apply max differences)))

(defn compare-dice-distributions
  "Compares the exact dice distribution with its normal approximation.
   
   Parameters:
   - n: Number of dice
   - sides: Number of sides per die
   
   Returns a map with:
   - :exact - The exact probability distribution
   - :normal - The normal approximation distribution
   - :ks-stat - The Kolmogorov-Smirnov statistic measuring the difference"
  [n sides]
  (let [exact (stats/uniform-dice-sum-pdf n sides)
        normal (normal-approximation-distribution n sides)
        ks-stat (kolmogorov-smirnov-statistic exact normal)]
    {:exact exact
     :normal normal
     :ks-stat ks-stat}))

(defn benchmark-distribution
  "Benchmark the performance of calculating dice distributions.
   Returns a map with execution times in milliseconds."
  [n sides]
  (println "Benchmarking" n "d" sides ":")
  
  ;; Benchmark exact distribution calculation
  (println "  Calculating exact distribution...")
  (let [start-time (System/currentTimeMillis)
        exact-result (stats/uniform-dice-sum-pdf n sides)
        exact-time (- (System/currentTimeMillis) start-time)]
    (println "  Exact distribution calculation took" exact-time "ms")
    (println "  Distribution has" (count exact-result) "outcomes")
    
    ;; Benchmark normal approximation calculation
    (println "  Calculating normal approximation...")
    (let [start-time (System/currentTimeMillis)
          normal-result (normal-approximation-distribution n sides)
          normal-time (- (System/currentTimeMillis) start-time)]
      (println "  Normal approximation calculation took" normal-time "ms")
      (println "  Distribution has" (count normal-result) "outcomes")
      
      ;; Return timing results
      {:exact-time exact-time
       :normal-time normal-time
       :exact-size (count exact-result)
       :normal-size (count normal-result)})))

;; Lookup table of KS statistics for different dice combinations
;; Format: {[n sides] ks-statistic}
(def ^:private ks-statistic-table
  (delay
    (into {}
          (for [n [1 3 5 10 25 50 100]
                sides [4 6 8 10 12 20 100]]
            (let [result (compare-dice-distributions n sides)]
              [[n sides] (:ks-stat result)])))))

(defn generate-ks-statistic-table
  "Generates and returns the KS statistic lookup table.
   This is computationally expensive and should only be called once
   during initialization or for debugging purposes."
  []
  @ks-statistic-table)
