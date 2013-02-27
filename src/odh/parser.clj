(ns odh.parser
  (:require [clojure.data.xml :as x]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as zx]
            [clojure.java.io :as io]))

(defrecord document [title content])

(defrecord part [type content])

(declare transform-node)

(defn create-para
  [node]
  (part. :para (map transform-node (z/children node))))

(defn create-habracut
  [node]
  (part. :habracut (zx/attr node :text)))

(defn create-emphasis
  [node]
  (if (= (zx/attr node :role) "bold")
    (part. :bold (zx/text node))
    (part. :italic (zx/text node))))

(defn create-section
  [node])

(defn create-link
  [node]
  (let [href (zx/attr node :href)
        text (zx/text node)]
    (part. :link {:href href :text text})))

(defn create-code
  [node]
  (part. :code node))

(defn create-itemizedlist
  [node])

(defn create-orderedlist
  [node]
  )

(defn create-programlisting
  [node]
  (part. :source node))

(defn create-text
  [node]
  (part. :text node))

(defmacro dispatch-tag
  "(dispatch-tag node a b c)
  ->
  (case (:tag node)
    :a (create-a node)
    :b (create-b node)
    :c (create-c node))"
  [node & tags]
  `(case (:tag ~node)
     ~@(mapcat #(vector (keyword %) `(~(symbol (str "create-" (name %))) ~node)) tags)))

(defn transform-node
  [node]
  (if (string? node)
    (create-text node)
    (dispatch-tag node
      :para :habracut :emphasis :section :link :code
      :itemizedlist :orderedlist :programlisting)))

(defn construct-sequence
  [root]
  (map transform-node (z/children root)))

(defn parse
  [source]
  (construct-sequence
    (with-open [source (io/reader source)]
      (z/xml-zip (x/parse source)))))

