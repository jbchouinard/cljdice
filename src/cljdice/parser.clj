(ns cljdice.parser
  (:require [cljdice.dice :as dice]
            [instaparse.core :as insta]))

(def dice-parser
  (insta/parser
   "term = dice | term op dice
    dice = ndice | ddice
    ndice = number
    ddice = count? <'d'> sides
    count = number
    sides = number
    number = #'[0-9]+'
    op = plus | minus
    plus = '+'
    minus = '-'"))


(defn- eval-number-encl
  [_ [_ value]]
  (Integer/parseInt value))

(defn- eval-ndice
  [node]
  (let [n (apply eval-number-encl node)]
    (dice/die-constant n)))

(defn- eval-ddice
  ([_ sides-node]
   (let [sides (apply eval-number-encl sides-node)]
     (when (<= sides 0)
       (throw (ex-info "Dice must have at least 1 side" {:sides sides})))
     (dice/d sides)))
  ([_ count-node sides-node]
   (let [count (apply eval-number-encl count-node)
         sides (apply eval-number-encl sides-node)]
     (when (<= sides 0)
       (throw (ex-info "Dice must have at least 1 side" {:sides sides})))
     (dice/die-repeated count (dice/d sides)))))

(defn- eval-dice
  [[_ & children]]
  (let [subnode (first children)
        subnode-type (first subnode)]
    (case subnode-type
      :ddice (apply eval-ddice subnode)
      :ndice (eval-ndice subnode))))

(defn- eval-term
  ([_ dice-node]
   (eval-dice dice-node))
  ([_ term-node [_ op-node] dice-node]
   (let [op (first op-node)
         right-die (eval-dice dice-node)
         right-die-signed (if (= op :minus) (dice/negate-die right-die) right-die)
         left-die (apply eval-term term-node)]
     (dice/dice+ left-die right-die-signed))))

(defn parse-dice-expression
  "Parse a dice expression string like '2d6+1d4+3' and return a data structure"
  [expr-str]
  (let [parse-tree (dice-parser expr-str)]
    (if (insta/failure? parse-tree)
      (throw (ex-info "Failed to parse dice expression" 
                      {:expr expr-str
                       :reason (:reason parse-tree)}))
      parse-tree)))

(defn eval-dice-expression
  "Evaluate a dice expression string and return a single die representing all possible outcomes"
  [expr-str]
  (when (empty? expr-str)
    (throw (ex-info "Empty dice expression" {:expr expr-str})))
  (let [parse-tree (parse-dice-expression expr-str)]
    (apply eval-term parse-tree)))
