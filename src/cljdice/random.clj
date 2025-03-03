(ns cljdice.random)

(defn rand-uniform
  "Returns random integer between n0 and n1 (inclusive).
   Handles cases where n0 > n1 by sorting the input values."
  [n0 n1]
  (let [[nmin nmax] (sort [n0 n1])]
    (+ nmin (rand-int (- nmax nmin -1)))))

(defn rand-normal-seq
  "Returns a lazy sequence of random values from normal distribution using Box-Muller transform.
   Each call to the Box-Muller algorithm generates two values, both of which are used.
   
   With no args: Returns a sequence of values from standard normal distribution (mean=0, std-dev=1).
   With mean, std-dev: Returns a sequence of values from normal distribution with given parameters."
  ([]
   (lazy-seq
    (let [u1 (rand)
          u2 (rand)
          r (Math/sqrt (* -2 (Math/log u1)))
          theta (* 2 Math/PI u2)
          z1 (* r (Math/cos theta))
          z2 (* r (Math/sin theta))]
      (cons z1 (cons z2 (rand-normal-seq))))))
  ([mean std-dev]
   (map #(+ mean (* std-dev %)) (rand-normal-seq))))

;; Cached sequence for efficiency
(def ^:private normal-random-seq (rand-normal-seq))

(defn rand-normal
  "Returns random value from normal distribution using Box-Muller transform.
   
   With no args: Returns a value from standard normal distribution (mean=0, std-dev=1).
   With mean, std-dev: Returns a value from normal distribution with given parameters.
   
   Uses a shared sequence for efficiency to avoid wasting random values."
  ([]
   (let [value (first normal-random-seq)]
     (alter-var-root #'normal-random-seq rest)
     value))
  ([mean std-dev]
   (+ mean (* std-dev (rand-normal)))))
