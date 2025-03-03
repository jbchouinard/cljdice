(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.java.shell :as shell]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def lib 'cljdice/cljdice)
(def version (format "0.1.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file (format "target/%s-%s-standalone.jar" (name lib) version))
(def native-image-name "cljdice")
(def native-image-path (str "target/" native-image-name))
(def version-file "target/classes/cljdice/version.txt")

(defn clean [_]
  (b/delete {:path "target"}))

(defn write-version-file
  "Write the version to a resource file that will be included in the JAR"
  []
  (let [version-dir (str/replace (str/replace version-file #"/[^/]+$" "") #"^target/classes/" "")
        dir (io/file (str "target/classes/" version-dir))]
    (.mkdirs dir)
    (spit version-file version)))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (write-version-file)
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'cljdice.core})
  (println "Uber jar built successfully:" uber-file)
  uber-file)

(defn native-uber
  "Build an uber jar for native image compilation (excludes profile namespace)"
  [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir
               :include #(not (re-matches #".*profile\.clj$" %))})
  (write-version-file)
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir
                  :ns-compile #{'cljdice.core 'cljdice.dice 'cljdice.parser 'cljdice.stats 'cljdice.version}})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'cljdice.core})
  (println "Native uber jar built successfully:" uber-file)
  uber-file)

(defn native-image [_]
  (let [jar-path (native-uber nil)
        command ["native-image"
                 "--no-fallback"
                 "--initialize-at-build-time"
                 "--initialize-at-run-time=java.util.Random"
                 "-H:+ReportExceptionStackTraces"
                 "-H:+UnlockExperimentalVMOptions"
                 "-H:Log=registerResource:"
                 "-J-Dclojure.spec.skip-macros=true"
                 "-J-Dclojure.compiler.direct-linking=true"
                 "-J-Xmx3g"
                 (str "-H:Name=" native-image-path)
                 "-jar" jar-path]
        _ (println "Building native image with command:" (str/join " " command))
        result (apply shell/sh command)]
    (if (zero? (:exit result))
      (println "Native image built successfully:" native-image-path)
      (do
        (println "Native image build failed with exit code:" (:exit result))
        (println "Error:" (:err result))
        (println "Output:" (:out result))))))
