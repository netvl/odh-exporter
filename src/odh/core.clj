(ns odh.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:require [odh.writer :as w]
            [odh.parser :as p]))

(defn process
  [from to]
  (with-open [input (io/reader (io/file from))
              output (io/writer (io/file to))]
    (w/write (p/parse input) output)))

(defn validate-args
  [args]
  (and (> (count args) 2) args))

(defn -main [& args]
  (if-let [[from to] (validate-args args)]
    (process from to)
    (println "Usage: odh-exporter <docbook xml file> <output text file>")))
