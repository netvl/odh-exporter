(ns odh.core
  (:gen-class)
  (:require [clojure.java.io :as io])
  (:require [odh [writer :as w] [parser :as p] [ui :as ui] [cache :as c]]))

(defn process
  [from to]
  (with-open [input (io/reader (io/file from))
              output (io/writer (io/file to))]
    (ui/log "Core" "Opened file '" from "' for reading")
    (ui/log "Core" "Opened file '" to "' for writing")
    (w/write (p/parse input) output)))

(defn validate-args
  [args]
  (and (>= (count args) 2) args))

(defn -main [& args]
  (if-let [[from to] (if (validate-args args)
                       args
                       (ui/ask-files))]
    (do (ui/present-log-window) (c/init-cache from) (process from to))
    (println "Usage: odh-exporter <source docbook file> <target file>")))
