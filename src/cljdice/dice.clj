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

(defmethod roll-die
  :die/repeated
  [[_ [count die]]]
  (reduce + (map roll-die (repeat count die))))

(defn die-or-scalar-type [x]
  (if (vector? x)
    (die-type x)
    :scalar))

(defmulti die-seq die-type)

(defmethod die-seq :die/repeated [[_ [count die]]] (mapcat die-seq (repeat count die)))

(defmethod die-seq :die/multi [[_ dice]] (mapcat die-seq dice))

(defmethod die-seq :default [die] (cons die nil))

(defn compact-dice
  "Combines repeated dice where possible."
  [dice-seq]
  (let [dice-seq-flat (mapcat die-seq dice-seq)
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
    [(die-or-scalar-type a) (die-or-scalar-type b)])
  :hierarchy #'clojure.core/global-hierarchy)

(defmethod dice+
  [:scalar :scalar]
  [na nb]
  (die-constant (+ na nb)))

(defmethod dice+
  [:scalar ::die]
  [number die]
  (shift-die die number))

(defmethod dice+
  [::die :scalar]
  [die number]
  (shift-die die number))

(defmethod dice+
  [:die/constant :die/constant]
  [[_ n1] [_ n2]]
  (die-constant (+ n1 n2)))

(defmethod dice+
  [:die/constant ::die]
  [[_ n] die]
  (shift-die die n))

(defmethod dice+
  [::die :die/constant]
  [die [_ n]]
  (shift-die die n))

(defmethod dice+
  [:die/multi :die/constant]
  [[_ dvec] die]
  (compact-die (conj dvec die)))

(defmethod dice+
  [:die/constant :die/multi]
  [die [_ dvec]]
  (compact-die (conj dvec die)))

(defmethod dice+
  [:die/multi :die/multi]
  [[_ dvec1] [_ dvec2]]
  (compact-die (into dvec1 dvec2)))

(defmethod dice+
  [:die/multi ::die]
  [die [_ dvec]]
  (compact-die (conj dvec die)))

(defmethod dice+
  [::die :die/multi]
  [[_ dvec] die]
  (compact-die (conj dvec die)))

(defmethod dice+
  [::die ::die]
  [die1 die2]
  (compact-die [die1 die2]))

(defn dice-sum [& dice] (reduce dice+ dice))