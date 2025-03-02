(ns cljdice.dice-test
  (:require [clojure.test :refer [deftest is testing]]
            [cljdice.dice :as dice]))

(deftest die-creation-test
  (testing "Creating dice"
    (is (= [:die/uniform [1 6]] (dice/d 6)))
    (is (= [:die/uniform [1 20]] (dice/d 20)))
    (is (= [:die/uniform [3 8]] (dice/d 3 8)))
    (is (= [:die/with-sides [1 2 3 4 5 6]] (dice/die-with-sides 1 2 3 4 5 6)))
    (is (= [:die/with-sides [1 2 3]] (dice/die-with-sides-seq [1 2 3])))
    (is (= [:die/constant 5] (dice/die-constant 5)))
    (is (= [:die/multi [(dice/d 6) (dice/d 8)]] 
           (dice/die-multi (dice/d 6) (dice/d 8))))
    (is (= [:die/multi [(dice/d 6) (dice/d 8)]] 
           (dice/die-multi-seq [(dice/d 6) (dice/d 8)])))
    (is (= [:die/repeated [3 (dice/d 6)]] (dice/die-repeated 3 (dice/d 6))))))

(deftest die-type-test
  (testing "Getting die type"
    (is (= :die/uniform (dice/die-type (dice/d 6))))
    (is (= :die/with-sides (dice/die-type (dice/die-with-sides 1 2 3))))
    (is (= :die/constant (dice/die-type (dice/die-constant 5))))
    (is (= :die/multi (dice/die-type (dice/die-multi (dice/d 6) (dice/d 8)))))
    (is (= :die/repeated (dice/die-type (dice/die-repeated 3 (dice/d 6)))))))

(deftest die-value-test
  (testing "Getting die value"
    (is (= [1 6] (dice/die-value (dice/d 6))))
    (is (= [1 2 3] (dice/die-value (dice/die-with-sides 1 2 3))))
    (is (= 5 (dice/die-value (dice/die-constant 5))))
    (is (= [(dice/d 6) (dice/d 8)] (dice/die-value (dice/die-multi (dice/d 6) (dice/d 8)))))
    (is (= [3 (dice/d 6)] (dice/die-value (dice/die-repeated 3 (dice/d 6)))))))

(deftest shift-die-test
  (testing "Shifting dice"
    (is (= [:die/uniform [4 9]] (dice/shift-die (dice/d 6) 3)))
    (is (= [:die/with-sides [4 5 6]] (dice/shift-die (dice/die-with-sides 1 2 3) 3)))
    (is (= [:die/constant 8] (dice/shift-die (dice/die-constant 5) 3)))
    (let [shifted (dice/shift-die (dice/die-multi (dice/d 6)) 3)]
      (is (= :die/multi (dice/die-type shifted)))
      (is (= 2 (count (dice/die-value shifted)))))
    (is (= [:die/repeated [3 [:die/uniform [4 9]]]] 
           (dice/shift-die (dice/die-repeated 3 (dice/d 6)) 3)))))

(deftest scale-die-test
  (testing "Scaling dice"
    (is (= [:die/uniform [2 12]] (dice/scale-die (dice/d 6) 2)))
    (is (= [:die/with-sides [2 4 6]] (dice/scale-die (dice/die-with-sides 1 2 3) 2)))
    (is (= [:die/constant 10] (dice/scale-die (dice/die-constant 5) 2)))
    (let [scaled (dice/scale-die (dice/die-multi (dice/d 6)) 2)]
      (is (= :die/multi (dice/die-type scaled)))
      (is (= 1 (count (dice/die-value scaled)))))
    (is (= [:die/repeated [3 [:die/uniform [2 12]]]] 
           (dice/scale-die (dice/die-repeated 3 (dice/d 6)) 2)))))

(deftest roll-die-test
  (testing "Rolling dice"
    (let [d6 (dice/d 6)
          result (dice/roll-die d6)]
      (is (integer? result))
      (is (<= 1 result 6)))
    
    (let [sides [10 20 30]
          die (dice/die-with-sides-seq sides)
          result (dice/roll-die die)]
      (is (some #{result} sides)))
    
    (is (= 5 (dice/roll-die (dice/die-constant 5))))
    
    (let [multi-die (dice/die-multi (dice/die-constant 2) (dice/die-constant 3))
          result (dice/roll-die multi-die)]
      (is (= 5 result)))
    
    (let [repeated-die (dice/die-repeated 3 (dice/die-constant 2))
          result (dice/roll-die repeated-die)]
      (is (= 6 result)))))

(deftest dice-plus-test
  (testing "Adding dice"
    (testing "scalar + scalar"
      (is (= [:die/constant 7] (dice/dice+ 3 4))))
    
    (testing "scalar + die"
      (let [result (dice/dice+ 3 (dice/d 6))]
        (is (= :die/uniform (dice/die-type result)))
        (is (= [4 9] (dice/die-value result)))))
    
    (testing "die + scalar"
      (let [result (dice/dice+ (dice/d 6) 3)]
        (is (= :die/uniform (dice/die-type result)))
        (is (= [4 9] (dice/die-value result)))))
    
    (testing "constant + constant"
      (let [result (dice/dice+ (dice/die-constant 3) (dice/die-constant 4))]
        (is (= :die/constant (dice/die-type result)))
        (is (= 7 (dice/die-value result)))))
    
    (testing "die + die"
      (let [result (dice/dice+ (dice/d 4) (dice/d 6))]
        (is (= :die/multi (dice/die-type result)))
        (is (= 2 (count (dice/die-value result))))))))

(deftest dice-seq-test
  (testing "Converting dice to sequences"
    (is (= '([:die/uniform [1 6]]) (dice/die-seq (dice/d 6))))
    
    (is (= '([:die/constant 5]) (dice/die-seq (dice/die-constant 5))))
    
    (let [multi-die (dice/die-multi (dice/d 6) (dice/d 8))
          result (dice/die-seq multi-die)]
      (is (= 2 (count result)))
      (is (some #(= % [:die/uniform [1 6]]) result))
      (is (some #(= % [:die/uniform [1 8]]) result)))
    
    (let [repeated-die (dice/die-repeated 3 (dice/die-constant 2))
          result (dice/die-seq repeated-die)]
      (is (= 3 (count result)))
      (is (every? #(= % [:die/constant 2]) result)))))

(deftest compact-dice-test
  (testing "Compacting dice"
    (let [dice-seq [(dice/d 6) (dice/d 6) (dice/d 8)]
          result (dice/compact-dice dice-seq)]
      (is (= 2 (count result)))
      (is (some #(= % [:die/repeated [2 [:die/uniform [1 6]]]]) result))
      (is (some #(= % [:die/uniform [1 8]]) result)))
    
    (let [dice-seq [(dice/die-constant 5) (dice/die-constant 5) (dice/die-constant 5)]
          result (dice/compact-dice dice-seq)]
      (is (= 1 (count result)))
      (is (= [:die/repeated [3 [:die/constant 5]]] (first result))))))

(deftest compact-die-test
  (testing "Compacting dice into a single die"
    (let [dice-seq [(dice/d 6)]
          result (dice/compact-die dice-seq)]
      (is (= [:die/uniform [1 6]] result)))
    
    (let [dice-seq [(dice/d 6) (dice/d 8)]
          result (dice/compact-die dice-seq)]
      (is (= :die/multi (dice/die-type result)))
      (is (= 2 (count (dice/die-value result)))))))

(deftest dice-sum-test
  (testing "Summing multiple dice"
    (let [result (dice/dice-sum (dice/d 6) (dice/d 8) (dice/die-constant 3))]
      (is (= :die/multi (dice/die-type result)))
      (is (= 3 (count (dice/die-value result)))))
    
    (let [result (dice/dice-sum (dice/die-constant 2) (dice/die-constant 3) (dice/die-constant 4))]
      (is (= :die/constant (dice/die-type result)))
      (is (= 9 (dice/die-value result))))))