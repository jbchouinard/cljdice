(ns cljdice.profile
  (:require [clj-async-profiler.core :as prof]
            [cljdice.core :as core]))

;; Entry point function used by the :profile-run alias in deps.edn via :exec-fn
(defn profile-main
  "Profile the main function with given arguments.
   
   This function:
   1. Starts the async profiler
   2. Runs the main function multiple times with the provided args
   3. Stops the profiler and generates a flamegraph
   4. Serves the UI for viewing profiling results
   
   Options:
   - :args - Vector of command line arguments to pass to main (default: [\"1000d6\"])"
  [opts]
  (println "Starting profiler...")
  (reset! prof/async-profiler-agent-path "/opt/async-profiler/lib/libasyncProfiler.so")
  (prof/start {:event :cpu :filter "cljdice"})
  
  (let [args (or (:args opts) ["1000d6"])]
    (println "Running main 10 times with args:" args)
    (dotimes [_ 10]
      (core/run args)))
  
  (println "Stopping profiler...")
  (prof/stop {:flamegraph? true}) 
  (println "Profiling complete. View at http://localhost:8080.")
  (prof/serve-ui 8080))

;; Example usage of profile-main from REPL or command line
(comment
  ;; Run profiling with default args (1000d6)
  (profile-main {})
  
  ;; Run profiling with custom args
  (profile-main {:args ["3d6+2"]})
  
  ;; This is used by the :profile-run alias in deps.edn:
  ;; clojure -X:profile-run
  ;; clojure -X:profile-run :args '["3d6+2"]'
  )
