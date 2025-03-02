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
           :main 'cljdice.core})
  (println "Uber jar built successfully:" uber-file)
  uber-file)

(defn check-native-image []
  (let [result (sh "which" "native-image")]
    (if (zero? (:exit result))
      {:success true :path (str/trim (:out result))}
      {:success false :message "GraalVM native-image tool not found in PATH. 
Please install GraalVM and the native-image tool. 
See https://www.graalvm.org/docs/getting-started/ for installation instructions."})))

(defn native-image [_]
  (let [jar-file (uber nil)
        native-image-check (check-native-image)]
    (if (:success native-image-check)
      (do
        (println "Building native image using:" (:path native-image-check))
        (println "This may take a few minutes...")
        (let [cmd ["clojure" "-M:native-image"]
              res (apply sh cmd)]
          (if (zero? (:exit res))
            (println "Native image built successfully!")
            (do
              (println "Error building native image:")
              (println (:err res))
              (println "Command output:")
              (println (:out res))))))
      (println (:message native-image-check)))))
