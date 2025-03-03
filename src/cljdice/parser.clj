(ns cljdice.parser
  (:require [cljdice.dice :as dice]
            [instaparse.core :as insta]))

(def dice-parser
  (insta/parser
   "term = dice | term op dice
    dice = ndice | ddice | ldice
    ndice = number
    ddice = count? <'d'> sides
    ldice = <'['> number (<','>? number)* <']'>
    count = number
    sides = number
    number = #'[0-9]+'
    op = plus | minus
    plus = '+'
    minus = '-'"
   :auto-whitespace :standard))

(defn- eval-number
  [[_ nstr]]
  (Integer/parseInt nstr))

(defn- eval-number-inner
  [[_ number-node]]
  (eval-number number-node))

(defn- eval-ldice
  [[_ & side-nodes]]
  (let [sides (map eval-number side-nodes)]
    (dice/die-with-sides-seq sides)))

(defn- eval-ndice
  [[_ value-node]]
    (dice/die-constant (eval-number value-node)))

(defn- eval-ddice
  ([_ sides-node]
   (let [sides (eval-number-inner sides-node)]
     (when (<= sides 0)
       (throw (ex-info "Dice must have at least 1 side" {:sides sides})))
     (dice/d sides)))
  ([_ count-node sides-node]
   (let [count (eval-number-inner count-node)
         sides (eval-number-inner sides-node)]
     (when (<= sides 0)
       (throw (ex-info "Dice must have at least 1 side" {:sides sides})))
     (dice/die-repeated count (dice/d sides)))))

(defn- eval-dice
  [[_ [subnode-type _ :as subnode]]]
  (case subnode-type
    :ddice (apply eval-ddice subnode)
    :ndice (eval-ndice subnode)
    :ldice (eval-ldice subnode)))

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
  "Parse a dice expression string like '2d6+1d4+3' and returns parse tree"
  [expr-str]
  (let [parse-tree (dice-parser expr-str)]
    (if (insta/failure? parse-tree)
      (throw (ex-info "Failed to parse dice expression"
                      {:expr expr-str
                       :reason (:reason parse-tree)}))
      parse-tree)))

(defn eval-dice-expression
  "Evaluate a dice expression string and returns a die value"
  [expr-str]
  (when (empty? expr-str)
    (throw (ex-info "Empty dice expression" {:expr expr-str})))
  (let [parse-tree (parse-dice-expression expr-str)]
    (apply eval-term parse-tree)))
