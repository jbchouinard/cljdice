(ns cljdice.core-test
  (:require [cljdice.core :as core]
            [cljdice.dice :as dice]
            [clojure.test :refer [deftest is testing]]))

(defn roll-dice-expression 
  "Helper function to evaluate and roll a dice expression string."
  [expr] 
  (dice/roll-die (core/eval-dice-expression expr)))

;; Test suite for roll-dice-expression function
(deftest roll-dice-expression-test
  (testing "Rolling dice expressions"
    (testing "Single die"
      (let [result (roll-dice-expression "d6")]
        (is (integer? result))
        (is (<= 1 result 6))))
    
    (testing "Multiple dice"
      (let [result (roll-dice-expression "2d6")]
        (is (integer? result))
        (is (<= 2 result 12))))
    
    (testing "Addition"
      (let [result (roll-dice-expression "d6+3")]
        (is (integer? result))
        (is (<= 4 result 9))))
    
    (testing "Subtraction"
      (let [result (roll-dice-expression "d20-5")]
        (is (integer? result))
        (is (<= -4 result 15))))
    
    (testing "Complex expressions"
      (let [result (roll-dice-expression "2d4+3d6-2")]
        (is (integer? result))
        (is (<= 3 result 24)))))
  
  (testing "Invalid expressions"
    (testing "Non-dice expression"
      (is (thrown? IllegalArgumentException (roll-dice-expression "not a dice expression"))))
    (testing "Invalid die count"
      (is (thrown? IllegalArgumentException (roll-dice-expression "d0"))))
    (testing "Empty expression"
      (is (thrown? IllegalArgumentException (roll-dice-expression ""))))))

;; Test suite for process-args function
(deftest process-args-test
  (testing "process-args function with valid arguments"
    (with-redefs [println (fn [& _] nil)
                  core/eval-dice-expression (fn [_] (dice/d 6))]
      (is (= 0 (core/run ["d20"])))))
  
  (testing "process-args function with help flag"
    (with-redefs [println (fn [& _] nil)]
      (is (= 0 (core/run ["--help"])))))
  
  (testing "process-args function with no arguments"
    (with-redefs [println (fn [& _] nil)]
      (is (= 1 (core/run [])))))
  
  (testing "process-args function with invalid expression"
    (with-redefs [println (fn [& _] nil)
                  core/eval-dice-expression (fn [_] (throw (IllegalArgumentException. "Test exception")))]
      (is (= 1 (core/run ["invalid"]))))))