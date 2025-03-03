(ns cljdice.random-test
  (:require [cljdice.random :as random]
            [clojure.test :refer [deftest is testing]]))

(defn test-rand-normal
  [mean std-dev]
  (let [sample-size 1000
        samples (repeatedly sample-size #(random/rand-normal mean std-dev))
        
        ;; For a normal distribution, approximately 68.27% of values 
        ;; should be within one standard deviation of the mean
        expected-within-one-std-dev (* sample-size 0.6827)
        tolerance (* sample-size 0.05)  ;; Allow 5% tolerance
        
        ;; Count how many samples are within one standard deviation
        within-one-std-dev (count (filter #(<= (Math/abs %) std-dev) samples))
        
        ;; Calculate the actual mean and standard deviation of the samples
        actual-mean (/ (reduce + samples) sample-size)
        variance (/ (reduce + (map #(Math/pow (- % actual-mean) 2) samples)) sample-size)
        actual-std-dev (Math/sqrt variance)]
    
    ;; Test that approximately the right number of values are within one standard deviation
    (is (<= (Math/abs (- within-one-std-dev expected-within-one-std-dev)) tolerance)
        (str "Expected approximately " expected-within-one-std-dev 
             " values within one standard deviation, but got " within-one-std-dev))
    
    ;; Test that the actual mean is close to the expected mean
    (is (< (Math/abs (- actual-mean mean)) 0.1)
        (str "Expected mean close to " mean ", but got " actual-mean))
    
    ;; Test that the actual standard deviation is close to the expected standard deviation
    (is (< (Math/abs (- actual-std-dev std-dev)) 0.1)
        (str "Expected standard deviation close to " std-dev ", but got " actual-std-dev))))

(defn test-rand-normal-seq
  [mean std-dev]
  (let [sample-size 1000
        samples (take sample-size (random/rand-normal-seq mean std-dev))
        
        ;; Calculate the actual mean and standard deviation of the samples
        actual-mean (/ (reduce + samples) sample-size)
        variance (/ (reduce + (map #(Math/pow (- % actual-mean) 2) samples)) sample-size)
        actual-std-dev (Math/sqrt variance)]
    
    ;; Test that the actual mean is close to the expected mean
    (is (< (Math/abs (- actual-mean mean)) 0.2)
        (str "Expected mean close to " mean ", but got " actual-mean))
    
    ;; Test that the actual standard deviation is close to the expected standard deviation
    (is (< (Math/abs (- actual-std-dev std-dev)) 0.2)
        (str "Expected standard deviation close to " std-dev ", but got " actual-std-dev))))

(defn test-rand-uniform
  [min-val max-val]
  (let [sample-size 1000
        samples (repeatedly sample-size #(random/rand-uniform min-val max-val))]
    
    ;; Test that all values are within the specified range
    (is (every? #(<= min-val % max-val) samples)
        "All values should be within the specified range")
    
    ;; Test that we have at least one value close to each end of the range
    (is (some #(<= % (+ min-val 1)) samples)
        "Should have at least one value close to the minimum")
    (is (some #(>= % (- max-val 1)) samples)
        "Should have at least one value close to the maximum")))

(deftest rand-normal-test
  (testing "rand-normal generates values with correct statistical properties"
    (test-rand-normal 0 1)))

(deftest rand-normal-seq-test
  (testing "rand-normal-seq generates a sequence of normally distributed values"
    (test-rand-normal-seq 5 2)))

(deftest rand-uniform-test
  (testing "rand-uniform generates values within the specified range"
    (test-rand-uniform 5 10)))
