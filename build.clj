(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.string :as str]
            [clojure.java.shell :refer [sh]]))

(def lib 'cljdice/cljdice)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))
(def native-image "target/cljdice")

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'cljdice.core}))

(defn native-image [_]
  (uber nil)
  (println "Building native image...")
  (let [res (sh "clojure" "-M:native-image")]
    (if (zero? (:exit res))
      (println "Native image built successfully!")
      (println "Error building native image:" (:err res)))))
