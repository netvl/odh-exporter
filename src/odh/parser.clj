(ns odh.parser
  (:require [clojure.data.xml :as x]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as zx]
            [clojure.java.io :as io]))

(defrecord document [title content])

(defrecord part [type content])

(defn fullstr
  [node]
  (apply str (-> node z/node :content)))

(declare transform-node)

(defn map-transform
  [s]
  (filter identity (map transform-node s)))

(defn create-para
  [node]
  (part. :para (map-transform (z/children node))))

(defn create-habracut
  [node]
  (part. :habracut (zx/attr node :text)))

(defn create-emphasis
  [node]
  (if (= (zx/attr node :role) "bold")
    (part. :bold (fullstr node))
    (part. :italic (fullstr node))))

(defn section-level
  [id]
  (inc (count (filter #{\_} (seq id)))))

(defn create-section
  [node]
  (let [title (zx/xml1-> node :title zx/text)]
    (part. :section
      {:title title
       :level (section-level (zx/attr node :xml/id))
       :content (map-transform (z/children node))})))

(defn create-link
  [node]
  (let [href (zx/attr node :xlink/href)
        text (fullstr node)]
    (part. :link {:href href :text text})))

(defn create-code
  [node]
  (part. :code (fullstr node)))

(defn create-listitem
  [node]
  (part. :list-item (map-transform (z/children node))))

(defn create-itemizedlist
  [node]
  (part. :unordered-list
    (map create-listitem (zx/xml-> node :listitem))))

(defn create-orderedlist
  [node]
  (part. :ordered-list
    (map create-listitem (zx/xml-> node :listitem))))

(defn create-programlisting
  [node]
  (part. :source (fullstr node)))

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
     ~@(mapcat #(vector (keyword %) `(~(symbol (str "create-" (name %))) (z/xml-zip ~node))) tags)
     nil))

(defn transform-node
  [node]
  (if (string? node)
    (create-text node)
    (dispatch-tag node
      :para :habracut :emphasis :section :link :code
      :itemizedlist :orderedlist :programlisting)))

(defn construct-sequence
  [root]
  (map-transform (z/children root)))

(defn read-xml
  [source]
  (z/xml-zip (x/parse source)))

(defn parse
  [source]
  (construct-sequence (read-xml source)))

