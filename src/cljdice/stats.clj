(ns cljdice.stats)

(defn rand-uniform
  "Returns random integer between n0 and n1 (inclusive).
   Handles cases where n0 > n1 by sorting the input values."
  [n0 n1]
  (let [[nmin nmax] (sort [n0 n1])]
    (+ nmin (rand-int (- nmax nmin -1)))))

(defn ^:private rand-normal-seq
  "Returns a lazy sequence of random values from normal distribution using Box-Muller transform.
   Each call to the Box-Muller algorithm generates two values, both of which are used.
   
   With no args: Returns a sequence of values from standard normal distribution (mean=0, std-dev=1).
   With mean, std-dev: Returns a sequence of values from normal distribution with given parameters."
  []
  (lazy-seq
   (let [u1 (rand)
         u2 (rand)
         r (Math/sqrt (* -2 (Math/log u1)))
         theta (* 2 Math/PI u2)
         z1 (* r (Math/cos theta))
         z2 (* r (Math/sin theta))]
     (cons z1 (cons z2 (rand-normal-seq))))))

;; Cached sequence for efficiency
(def ^:private normal-random-seq (rand-normal-seq))

(defn ^:private rand-normal-box-muller
  "Returns random value from normal distribution using Box-Muller transform.
   
   Uses a shared sequence for efficiency to avoid wasting random values."
  []
  (let [value (first normal-random-seq)]
    (alter-var-root #'normal-random-seq rest)
    value))

;; Initialize Random at runtime instead of build time
(def ^:private java-random-atom (atom nil))

(defn ^:private get-java-random
  "Gets or initializes the java.util.Random instance.
   This ensures the Random is created at runtime, not build time."
  []
  (if-let [r @java-random-atom]
    r
    (let [new-random (java.util.Random.)]
      (reset! java-random-atom new-random)
      new-random)))

(defn ^:private rand-normal-polar
  "Returns normally-distributed random value using java.util.Random.nextGaussian,
   which uses the Marsaglia polar method."
  []
  (.nextGaussian (get-java-random)))

(def ^:private default-rand-normal-method :polar)

(defn rand-normal
  ([]
   (rand-normal {:method default-rand-normal-method}))
  ([opts]
   (let [{:keys [method] :or {method default-rand-normal-method}} (or opts {})]
     (case method
       :polar (rand-normal-polar)
       :box-muller (rand-normal-box-muller)
       (throw (IllegalArgumentException. (str "Unsupported random method: " method))))))
  ([mean stddev]
   (+ mean (* stddev (rand-normal))))
  ([mean stddev opts]
   (+ mean (* stddev (rand-normal opts)))))

(defn ^:private convolve-distributions
  "Convolve two discrete probability distributions.
   Each distribution is a map from outcome to probability."
  [dist1 dist2]
  (reduce-kv
   (fn [result k1 p1]
     (reduce-kv
      (fn [r k2 p2]
        (let [sum (+ k1 k2)
              prob (* p1 p2)]
          (update r sum (fnil + 0) prob)))
      result
      dist2))
   {}
   dist1))

(defn single-die-distribution
  "Returns the probability distribution for a single die with s sides.
   Result is a map from outcome (1 to s) to probability (1/s)."
  [sides]
  (let [prob (/ 1.0 sides)]
    (into {} (map #(vector % prob) (range 1 (inc sides))))))

(defn dice-sum-distribution
  "Calculates the exact probability distribution for the sum of n dice with s sides each.
   Returns a map where keys are possible sums and values are their probabilities."
  [n sides]
  (cond
    (< n 1) {}
    (= n 1) (single-die-distribution sides)
    :else (let [single-dist (single-die-distribution sides)
                rest-dist (dice-sum-distribution (dec n) sides)]
            (convolve-distributions single-dist rest-dist))))

(defn dice-stats
  "Calculates the mean and standard deviation for n dice with s sides each.
   Returns a vector [mean stddev]."
  [n sides]
  (let [mean (/ (* n (inc sides)) 2)
        variance (/ (* n sides sides) 12)]
    [mean (Math/sqrt variance)]))
