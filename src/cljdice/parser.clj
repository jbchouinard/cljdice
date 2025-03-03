(ns cljdice.parser
  (:require [cljdice.dice :as dice]
            [instaparse.core :as insta]))

;; Grammar definition for dice expressions
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

;; Helper functions for parse tree evaluation
(defn- eval-number
  "Converts a number node to an integer."
  [[_ nstr]]
  (Integer/parseInt nstr))

(defn- eval-number-inner
  "Extracts and evaluates a number from a nested node."
  [[_ number-node]]
  (eval-number number-node))

(defn- eval-ldice
  "Evaluates a list dice node (e.g., [1,2,3])."
  [[_ & side-nodes]]
  (let [sides (map eval-number side-nodes)]
    (dice/die-with-sides-seq sides)))

(defn- eval-ndice
  "Evaluates a number dice node (e.g., 5)."
  [[_ value-node]]
    (dice/die-constant (eval-number value-node)))

(defn- eval-ddice
  "Evaluates a standard dice node (e.g., d6 or 2d6)."
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
  "Evaluates a dice node by dispatching to the appropriate handler."
  [[_ [subnode-type _ :as subnode]]]
  (case subnode-type
    :ddice (apply eval-ddice subnode)
    :ndice (eval-ndice subnode)
    :ldice (eval-ldice subnode)))

(defn- eval-term
  "Evaluates a term node, which can be a single die or an operation on dice."
  ([_ dice-node]
   (eval-dice dice-node))
  ([_ term-node [_ op-node] dice-node]
   (let [op (first op-node)
         right-die (eval-dice dice-node)
         right-die-signed (if (= op :minus) (dice/negate-die right-die) right-die)
         left-die (apply eval-term term-node)]
     (dice/dice+ left-die right-die-signed))))

;; Public API functions
(defn parse-dice-expression
  "Parse a dice expression string like '2d6+1d4+3' and returns parse tree.
   Throws an exception if the expression is invalid."
  [expr-str]
  (let [parse-tree (dice-parser expr-str)]
    (if (insta/failure? parse-tree)
      (throw (ex-info "Failed to parse dice expression"
                      {:expr expr-str
                       :reason (:reason parse-tree)}))
      parse-tree)))

(defn eval-dice-expression
  "Evaluate a dice expression string and returns a die value.
   Throws an exception if the expression is empty or invalid."
  [expr-str]
  (when (empty? expr-str)
    (throw (ex-info "Empty dice expression" {:expr expr-str})))
  (let [parse-tree (parse-dice-expression expr-str)]
    (apply eval-term parse-tree)))
