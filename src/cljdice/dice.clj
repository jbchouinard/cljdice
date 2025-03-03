(ns cljdice.dice)

(derive :die/uniform ::die)
(derive :die/with-sides ::die)
(derive :die/constant ::die)
(derive :die/multi ::die)
(derive :die/repeated ::die)

(defn rand-uniform
  "Returns random integer between n0 and n1 (inclusive)."
  [n0 n1]
  (let [[nmin nmax] (sort [n0 n1])]
    (+ nmin (rand-int (- nmax nmin -1)))))

(defn rand-normal
  "Box-Muller transform to generate random normal value"
  ([]
   (let [u1 (rand)
         u2 (rand)]
     (* (Math/sqrt (* -2 (Math/log u1)))
        (Math/cos (* 2 Math/PI u2)))))
  ([mean std-dev]
   (+ mean (* std-dev (rand-normal)))))

(defn d
  ([n] [:die/uniform [1 n]])
  ([n m] [:die/uniform [n m]]))

(defn die-with-sides [& sides] [:die/with-sides (vec sides)])

(defn die-with-sides-seq [sides-seq] [:die/with-sides (vec sides-seq)])

(defn die-constant [n] [:die/constant n])

(defn die-repeated [count die] [:die/repeated [count die]])

(defn die-multi [& dice] [:die/multi (vec dice)])

(defn die-multi-seq [dice-seq] [:die/multi (vec dice-seq)])

(defn die-type [die] (first die))

(defn die-value [die] (nth die 1))

(defmulti shift-die (fn [die _] (die-type die)))

(defmethod shift-die
  :die/uniform
  [[_ [from to]] n]
  (d (+ from n) (+ to n)))

(defmethod shift-die
  :die/with-sides
  [[_ sides] n]
  (die-with-sides-seq (map #(+ n %) sides)))

(defmethod shift-die
  :die/constant
  [[_ n] m]
  (die-constant (+ n m)))

(defmethod shift-die
  :die/multi
  [[_ dice] n]
  (die-multi-seq (conj dice (die-constant n))))

(defmethod shift-die
  :die/repeated
  [[_ [count die]] n]
  (die-repeated count (shift-die die n)))

(defmulti scale-die (fn [die _] (die-type die)))

(defmethod scale-die
  :die/uniform
  [[_ [from to]] n]
  (d (* from n) (* to n)))

(defmethod scale-die
  :die/with-sides
  [[_ sides] n]
  (die-with-sides-seq (map #(* n %) sides)))

(defmethod scale-die
  :die/constant
  [[_ n] m]
  (die-constant (* m n)))

(defmethod scale-die
  :die/multi
  [[_ dice] n]
  (die-multi-seq (map #(scale-die % n) dice)))

(defmethod scale-die
  :die/repeated
  [[_ [count die]] n]
  (die-repeated count (scale-die die n)))

(defn negate-die [die] (scale-die die -1))

(defmulti roll-die (fn [die] (die-type die)))

(defmethod roll-die
  :die/uniform
  [[_ [from to]]]
  (rand-uniform from to))

(defmethod roll-die
  :die/with-sides
  [[_ sides]]
  (rand-nth sides))

(defmethod roll-die
  :die/constant
  [[_ n]]
  n)

(defmethod roll-die
  :die/multi
  [[_ dice]]
  (reduce + (map roll-die dice)))

(defn bounded-int [v vmin vmax]
  (max vmin (min vmax (Math/round (double v)))))

(defn roll-die-normal-approx
  [count [_ [from to]]]
  (let [sides (+ 1 (- to from))
        mean (* count (/ (+ from to) 2))
        variance (* count (- (* sides sides) 1) (/ 1 12))
        std-dev (Math/sqrt variance)]
    (println (str "Roll die normal approx mean=" mean ", stddev=" std-dev))
    (bounded-int (rand-normal mean std-dev) (* count from) (* count to))))

(defmethod roll-die
  :die/repeated
  [[_ [count [die-type _ :as die]]]]
  (if (and (= die-type :die/uniform) (> count 100))
    (roll-die-normal-approx count die)
    (reduce + (map roll-die (repeat count die)))))

(defmulti die-seq die-type)

(defmethod die-seq :die/repeated [[_ [count die]]] (mapcat die-seq (repeat count die)))

(defmethod die-seq :die/multi [[_ dice]] (mapcat die-seq dice))

(defmethod die-seq :default [die] (cons die nil))

(defn compact-const-dice
  [dice-seq]
  (let [grouped (group-by #(= :die/constant (first %)) dice-seq)
        const-dice (get grouped true)
        other-dice (get grouped false)
        const-die (die-constant (reduce + (map #(nth % 1) const-dice)))]
    (if (seq const-dice) (cons const-die other-dice) other-dice)))

(defn compact-dice
  "Combines repeated dice where possible."
  [dice-seq]
  (let [dice-seq-const-compacted (compact-const-dice dice-seq)
        dice-seq-flat (mapcat die-seq dice-seq-const-compacted)
        freqs (frequencies dice-seq-flat)]
    (into [] (map (fn [[k v]] (if (= 1 v) k (die-repeated v k))) (seq freqs)))))

(defn compact-die
  "Compacts a seq of dice into a single or multi die as appropriate."
  [dice-seq]
  (let [dvec (compact-dice dice-seq)]
    (if (= 1 (count dvec)) (first dvec) (die-multi-seq dvec))))

(defmulti dice+
  "Add dice or numbers together."
  (fn [a b]
    [(die-type a) (die-type b)])
  :hierarchy #'clojure.core/global-hierarchy)

(defmethod dice+
  [:die/constant :die/constant]
  [[_ n1] [_ n2]]
  (die-constant (+ n1 n2)))

(defmethod dice+
  [:die/multi :die/multi]
  [[_ dvec1] [_ dvec2]]
  (compact-die (into dvec1 dvec2)))

(defmethod dice+
  [:die/multi ::die]
  [[_ dvec] die]
  (compact-die (conj dvec die)))

(defmethod dice+
  [::die :die/multi]
  [die [_ dvec]]
  (compact-die (conj dvec die)))

(defmethod dice+
  [::die ::die]
  [die1 die2]
  (compact-die [die1 die2]))

(defn dice-sum [& dice] (reduce dice+ dice))