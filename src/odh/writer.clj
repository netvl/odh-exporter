(ns odh.writer
  (:require [clojure.data.xml :as x]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as zx]))

(defmulti write-part :type)

(defmethod write-part :para
  [{:keys [content]}]
  (println)
  (dorun (map write-part content)))

(defmethod write-part :habracut
  [{:keys [content]}]
  (println)
  (println (if content
             (str "<habracut text=\"" content "\" />")
             "<habracut />")))

(defmethod write-part :bold
  [{:keys [content]}]
  (print (str "<strong>" content "</strong>")))

(defmethod write-part :italic
  [{:keys [content]}]
  (print (str "<em>" content "</em>")))

(defmethod write-part :section
  [{{:keys [title level content]} :content}]
  (println)
  (println (str "<h" level ">" title "</h" level ">"))
  (dorun (map write-part content)))

(defmethod write-part :link
  [{{:keys [href text]} :content}]
  (print (str "<a href=\"" href "\">" text "</a>")))

(defmethod write-part :code
  [{:keys [content]}]
  (print (str "<code>" content "</code>")))

(defmethod write-part :list-item
  [{:keys [content]}]
  (print "<li>")
  (dorun (map write-part content))
  (println "</li>"))

(defmethod write-part :unordered-list
  [{:keys [content]}]
  (println "<ul>")
  (dorun (map write-part content))
  (println "</ul>"))

(defmethod write-part :ordered-list
  [{:keys [content]}]
  (println "<ol>")
  (dorun (map write-part content))
  (println "</ol>"))

(defmethod write-part :source
  [{:keys [content]}]
  (println "<source lang=\"\">")
  (println content)
  (println "</source>"))

(defmethod write-part :text
  [{:keys [content]}]
  (print content))

(defn write
  [document destination]
  (binding [*out* destination] 
    (dorun (map write-part document))))

