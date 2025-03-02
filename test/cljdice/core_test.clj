(ns cljdice.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [cljdice.core :as core]))

(deftest roll-dice-expression-test
  (testing "Rolling dice expressions"
    (testing "Single die"
      (let [result (core/roll-dice-expression "d6")]
        (is (integer? result))
        (is (<= 1 result 6))))
    
    (testing "Multiple dice"
      (let [result (core/roll-dice-expression "2d6")]
        (is (integer? result))
        (is (<= 2 result 12))))
    
    (testing "Addition"
      (let [result (core/roll-dice-expression "d6+3")]
        (is (integer? result))
        (is (<= 4 result 9))))
    
    (testing "Subtraction"
      (let [result (core/roll-dice-expression "d20-5")]
        (is (integer? result))
        (is (<= -4 result 15))))
    
    (testing "Complex expressions"
      (let [result (core/roll-dice-expression "2d4+3d6-2")]
        (is (integer? result))
        (is (<= 3 result 24)))))
  
  (testing "Invalid expressions"
    (is (thrown? IllegalArgumentException (core/roll-dice-expression "not a dice expression")))
    (is (thrown? IllegalArgumentException (core/roll-dice-expression "d0")))
    (is (thrown? IllegalArgumentException (core/roll-dice-expression "")))))

(deftest process-args-test
  (testing "process-args function with valid arguments"
    (with-redefs [println (fn [& args] nil)
                  core/roll-dice-expression (fn [_] 42)]
      (is (= 0 (core/process-args ["d20"])))))
  
  (testing "process-args function with help flag"
    (with-redefs [println (fn [& args] nil)]
      (is (= 0 (core/process-args ["--help"])))))
  
  (testing "process-args function with no arguments"
    (with-redefs [println (fn [& args] nil)]
      (is (= 0 (core/process-args [])))))
  
  (testing "process-args function with invalid expression"
    (with-redefs [println (fn [& args] nil)
                  core/roll-dice-expression (fn [_] (throw (IllegalArgumentException. "Test exception")))]
      (is (= 1 (core/process-args ["invalid"]))))))