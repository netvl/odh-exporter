(ns odh.parser
  (:require [clojure.data.xml :as x]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as zx]
            [clojure.java.io :as io])
  (:require [odh.ui :as ui]))

(defn log
  [& args]
  (apply ui/log "Parser" args))

(defrecord document [title content])

(defrecord part [type content])

(defn preprocess
  [^String s]
  (-> s
      (.replaceAll "---" "&mdash;")))

(defn fullstr*
  [node]
  (apply str (-> node z/node :content)))

(defn fullstr
  [node]
  (preprocess (fullstr* node)))

(declare transform-node)

(defn map-transform
  [s]
  (filter identity (map transform-node s)))

(defn create-para
  [node]
  (log "Encountered para node")
  (part. :para (map-transform (z/children node))))

(defn create-habracut
  [node]
  (log "Encountered habracut node")
  (part. :habracut (zx/attr node :text)))

(defn create-emphasis
  [node]
  (if (= (zx/attr node :role) "bold")
    (do
      (log "Encountered bold emphasis node")
      (part. :bold (fullstr node)))
    (do
      (log "Encountered italic emphasis node")
      (part. :italic (fullstr node)))))

(defn section-level
  [id]
  (inc (count (filter #{\_} (seq id)))))

(defn create-section
  [node]
  (let [title (fullstr (zx/xml1-> node :title))]
    (log "Encountered section node: " title)
    (part. :section
      {:title title
       :level (section-level (zx/attr node :xml/id))
       :content (map-transform (z/children node))})))

(defn create-link
  [node]
  (let [href (zx/attr node :xlink/href)
        text (fullstr node)]
    (log "Encountered link node:\n    " text " -> " href)
    (part. :link {:href href :text text})))

(defn create-code
  [node]
  (log "Encountered inline code node")
  (part. :code (fullstr node)))

(defn create-listitem
  [node]
  (log "Encountered list item node")
  (part. :list-item (map-transform (z/children node))))

(defn create-itemizedlist
  [node]
  (log "Encountered unordered list node")
  (part. :unordered-list
    (map create-listitem (zx/xml-> node :listitem))))

(defn create-orderedlist
  [node]
  (log "Encountered ordered list node")
  (part. :ordered-list
    (map create-listitem (zx/xml-> node :listitem))))

(defn create-programlisting
  [node]
  (log "Encountered source node")
  (part. :source (fullstr* node)))

(defn create-hr
  [node]
  (log "Encountered horizontal ruler node")
  (part. :ruler nil))

(defn create-img
  [node]
  (log "Encountered image node")
  (part. :image {:src (zx/attr node :src)
                 :linkify (zx/attr node :linkify)}))

(defn create-text
  [node]
  (log "Encountered textual node")
  (part. :text (preprocess node)))

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
      :itemizedlist :orderedlist :programlisting :hr :img)))

(defn construct-sequence
  [root]
  (map-transform (z/children root)))

(defn read-xml
  "Creates XML zipper for the XML document read from the given source."
  [source]
  (z/xml-zip (x/parse source)))

(defn parse
  "Entry point for the parser, loads a document from the given source 
  (either input stream or reader) and creates nested sequence of document parts."
  [source]
  (log "Started parsing XML source")
  (construct-sequence (read-xml source)))

