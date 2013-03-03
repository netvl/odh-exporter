(ns odh.cache
  (:require [clojure.string :as s]
            [clojure.java.io :as io])
  (:require [odh [util :as u]]))

(def cache (atom {}))
(def ^:private cache-file (atom nil))

(defn init-cache
  [source]
  (let [cache-file-name (s/replace source #"\.xml$" ".cache")]
    (reset! cache-file cache-file-name)
    (when (.exists (io/file cache-file-name))
      (reset! cache (read-string (slurp cache-file-name))))))

(defn update-cache
  [s t]
  (swap! cache assoc (u/hex-digest s) (or t :nil)))

(defn query-cache
  [s]
  (get @cache (u/hex-digest s)))

(defn setup-watch
  []
  (add-watch cache :cache-store
    (fn [_ _ _ cache]
      (spit @cache-file (pr-str cache)))))

(defn teardown-watch
  []
  (remove-watch cache :cache-store))

(setup-watch)
