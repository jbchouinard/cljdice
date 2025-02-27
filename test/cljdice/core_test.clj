(ns cljdice.core-test
  (:require [clojure.test :refer :all]
            [cljdice.core :as core]))

(deftest roll-dice-test
  (testing "roll-dice returns the correct number of results"
    (is (= 1 (count (core/roll-dice 1 6))))
    (is (= 3 (count (core/roll-dice 3 20)))))
  
  (testing "roll-dice returns values within the correct range"
    (let [results (core/roll-dice 100 6)]
      (is (every? #(<= 1 % 6) results)))
    
    (let [results (core/roll-dice 100 20)]
      (is (every? #(<= 1 % 20) results)))))

(deftest format-roll-result-test
  (testing "format-roll-result formats correctly"
    (is (= "Rolled: 1, 2, 3\nTotal: 6"
           (core/format-roll-result [1 2 3])))
    (is (= "Rolled: 20\nTotal: 20"
           (core/format-roll-result [20])))))
