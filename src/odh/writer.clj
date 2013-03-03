(ns odh.writer
  (:require [clojure.data.xml :as x]
            [clojure.zip :as z]
            [clojure.data.zip.xml :as zx])
  (:require [odh.ui :as ui]))

(defn log
  [& args]
  (apply ui/log "Writer" args))

(defmulti write-part :type)

(defmethod write-part :para
  [{:keys [content]}]
  (log "Writing paragraph")
  (println)
  (dorun (map write-part content)))

(defmethod write-part :habracut
  [{:keys [content]}]
  (log "Writing habracut")
  (println)
  (println (if content
             (str "<habracut text=\"" content "\" />")
             "<habracut />")))

(defmethod write-part :bold
  [{:keys [content]}]
  (log "Writing bold emphasis")
  (print (str "<strong>" content "</strong>")))

(defmethod write-part :italic
  [{:keys [content]}]
  (log "Writing italic emphasis")
  (print (str "<em>" content "</em>")))

(defmethod write-part :section
  [{{:keys [title level content]} :content}]
  (log "Writing section: " title)
  (println)
  (println (str "<h" level ">" title "</h" level ">"))
  (dorun (map write-part content)))

(defmethod write-part :link
  [{{:keys [href text]} :content}]
  (log "Writing link")
  (print (str "<a href=\"" href "\">" text "</a>")))

(defmethod write-part :code
  [{:keys [content]}]
  (log "Writing inline source piece")
  (print (str "<code>" content "</code>")))

(defmethod write-part :list-item
  [{:keys [content]}]
  (log "Writing list item")
  (print "<li>")
  (dorun (map write-part content))
  (println "</li>"))

(defmethod write-part :unordered-list
  [{:keys [content]}]
  (log "Writing unordered list")
  (println "<ul>")
  (dorun (map write-part content))
  (println "</ul>"))

(defmethod write-part :ordered-list
  [{:keys [content]}]
  (log "Writing ordered list")
  (println "<ol>")
  (dorun (map write-part content))
  (println "</ol>"))

(defmethod write-part :source
  [{:keys [content]}]
  (if-let [lang (ui/ask-source-type content)]
    (do
      (log "Writing source piece for " lang " language")
      (println (str "<source lang=\"" lang "\">"))
      (print content)
      (println "</source>"))
    (do
      (log "Writing simple preformatted piece")
      (println "<pre>")
      (print content)
      (println "</pre>"))))

(defmethod write-part :ruler
  [_]
  (log "Writing horizontal ruler")
  (println)
  (println "<hr/>"))

(defmethod write-part :text
  [{:keys [content]}]
  (log "Writing text piece")
  (print content))

(defn write
  [document destination]
  (log "Started writing output document")
  (binding [*out* destination] 
    (dorun (map write-part document)))
  (log "Finished writing the document"))

