(ns cljdice.version
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- load-version-from-resource
  "Load version from the version.txt resource file.
   Returns a default development version if the resource is not found."
  []
  (try
    (-> (io/resource "cljdice/version.txt")
        slurp
        str/trim)
    (catch Exception _
      (let [git-version (try
                          (-> (Runtime/getRuntime)
                              (.exec "git describe --tags --always")
                              .getInputStream
                              (slurp)
                              str/trim)
                          (catch Exception _
                            nil))]
        (or git-version "0.0.0-DEVELOPMENT")))))

(def ^:private version-info
  {:version (load-version-from-resource)})

(defn version
  "Get the current version of the application."
  []
  (:version version-info))

(defn version-string
  "Get a formatted version string suitable for display."
  []
  (format "cljdice version %s" (version)))
