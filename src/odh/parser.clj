(ns odh.parser
  (:require [clojure.data.xml :as x]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as zx]
            [clojure.java.io :as io]))

(defn parse
  [source]
  (construct-sequence
    (with-open [source (io/reader source)]
      (z/xml-zip (x/parse source)))))

(defn construct-sequence
  [root])
