(ns odh.util
  (:import java.security.MessageDigest))

(defn hash-digest
  ([h] (hash-digest h "SHA-256"))
  ([h algo]
    (let [md (MessageDigest/getInstance algo)]
      (.update md (.getBytes h))
      (.digest md))))

(defn hex-digest
  [h]
  (let [d (hash-digest h)]
    (apply str (map #(format "%02x" (bit-and % 0xff)) d))))
