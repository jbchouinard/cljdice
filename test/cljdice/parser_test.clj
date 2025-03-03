(ns cljdice.parser-test
  (:require [clojure.test :refer [deftest is testing]]
            [cljdice.parser :as parser]
            [cljdice.dice :as dice]))

(deftest parse-dice-expression-test
  (testing "Parsing dice expressions"
    (testing "Valid expressions"
      (is (vector? (parser/parse-dice-expression "d6")))
      (is (vector? (parser/parse-dice-expression "2d6")))
      (is (vector? (parser/parse-dice-expression "d6+3")))
      (is (vector? (parser/parse-dice-expression "2d6+1d4")))
      (is (vector? (parser/parse-dice-expression "2d6+1d4-2"))))
    
    (testing "Invalid expressions"
      (is (thrown? clojure.lang.ExceptionInfo (parser/parse-dice-expression "")))
      (is (thrown? Exception (parser/parse-dice-expression "not a dice expression")))
      (is (thrown? Exception (parser/parse-dice-expression "d"))))))

(deftest eval-dice-expression-test
  (testing "Evaluating dice expressions"
    (testing "Single number"
      (let [result (parser/eval-dice-expression "5")]
        (is (= [:die/constant 5] result))))
    
    (testing "Single die"
      (let [result (parser/eval-dice-expression "d6")]
        (is (= :die/uniform (dice/die-type result)))
        (is (= [1 6] (dice/die-value result)))))
    
    (testing "Multiple dice"
      (let [result (parser/eval-dice-expression "2d4")]
        (is (= :die/repeated (dice/die-type result)))
        (is (= 2 (first (dice/die-value result))))
        (is (= :die/uniform (dice/die-type (second (dice/die-value result)))))))
    
    (testing "Addition"
      (let [result (parser/eval-dice-expression "d4+3")]
        (is (= :die/multi (dice/die-type result)))))
    
    (testing "Subtraction"
      (let [result (parser/eval-dice-expression "d6-1")]
        (is (= :die/multi (dice/die-type result)))))
    
    (testing "Complex expressions"
      (let [result (parser/eval-dice-expression "2d6+d4-2")]
        (is (= :die/multi (dice/die-type result))))))
  
  (testing "Edge cases"
    (testing "Zero-sided die"
      (is (thrown? clojure.lang.ExceptionInfo (parser/eval-dice-expression "d0"))))
    
    (testing "Negative-sided die"
      (is (thrown? clojure.lang.ExceptionInfo (parser/eval-dice-expression "d-1"))))
    
    (testing "Empty string"
      (is (thrown? clojure.lang.ExceptionInfo (parser/eval-dice-expression ""))))))