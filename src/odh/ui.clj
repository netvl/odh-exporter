(ns odh.ui
  (:use [seesaw core chooser mig])
  (:require [clj-time [core :as tc] [format :as tf]]
            [clojure.string :as s])
  (:require [odh.cache :as c]))

;; Source language sample dialog

(def languages
  ["Bash" "C#" "C++" "CSS" "Diff" "HTML" "XML" "Ini" "Java" "JavaScript" "PHP" "Perl" "Python" "Ruby" "SQL" "1C"
   "AVR" "ActionScript" "Apache" "Axapta" "CMake" "CoffeeScript" "DOS" ".bat" "Delphi" "Django" "Erlang" "Erlang"
   "Go" "Haskell" "Lisp" "Lua" "MEL" "Markdown" "Matlab" "Nginx" "Objective" "Parser3" "Python" "RenderMan"
   "Rust" "Scala" "Smalltalk" "TeX" "VBScript" "VHDL" "Vala"])

(def source-state (atom :normal))
(def source-previous (atom nil))
(def source-using-cache (atom true))

(declare log)

(defn create-sample-dialog
  []
  (with-widgets [(combobox
                   :id :combo-answer
                   :model languages)
                 (text
                   :id :text-sample
                   :multi-line? true
                   :editable? false)
                 (mig-panel
                   :id :sample-panel
                   :items [["Source type" "split"]
                           [combo-answer "growx, pushx, wrap"]
                           [(scrollable text-sample) "grow, push, spanx 2"]])]
    (let [sample-dialog
          (dialog
            :content sample-panel
            :modal? true
            :title "Select source type"
            :options [(button
                        :text "Confirm"
                        :listen [:action #(return-from-dialog %
                                            (reset! source-previous (selection combo-answer)))])
                      (button
                        :text "Same for all"
                        :listen [:action #(do	(reset! source-state :same-for-all)
                                           		(return-from-dialog % (reset! source-previous (selection combo-answer))))])
                      (button
                        :text "Skip"
                        :listen [:action #(return-from-dialog % nil)])
                      (button
                        :text "Skip all"
                        :listen [:action #(do (reset! source-state :skip-all)
                                           		(return-from-dialog % nil))])])]
      sample-dialog)))

(defn show-sample-dialog
  [sample]
  (if-let [result (c/query-cache sample)]
    (do
      (log "Cache" "Found item in cache: " result)
      (if (= result :nil) nil result))
    (let [dialog (create-sample-dialog)]
      (c/update-cache sample
        (case @source-state
          :normal (do
                    (text! (select (to-root dialog) [:#text-sample]) sample)
                    (selection! (select (to-root dialog) [:#combo-answer]) @source-previous)
                    (-> dialog pack! show!))
          :skip-all nil
          :same-for-all @source-previous)))))

(defn ask-source-type
  [sample]
  (let [result (promise)]
    (invoke-later
      (deliver result (show-sample-dialog sample)))
    @result))

;; Logging

(def log-agent (agent nil))

(defn now
  []
  (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss.SSS") (tc/now)))

(defn log
  [source & args]
  (send-off log-agent (constantly (apply str "[" (now) "] <" source "> " args))))

(defn show-log-window
  []
  (with-widgets [(text
                   :id :text-log
                   :multi-line? true
                   :editable? false)
                 (mig-panel
                   :id :log-panel
                   :items [[(scrollable text-log) "grow, push, wrap"]
                           [(button
                              :text "Exit"
                              :listen [:action #(do
                                                  (shutdown-agents)
                                                  (dispose! (to-root %)))]) "split"]])]
    (.setUpdatePolicy (.getCaret text-log) javax.swing.text.DefaultCaret/ALWAYS_UPDATE)
    (add-watch log-agent :log-dialog
      (fn [_ _ _ line]
        (.append text-log (str line "\n"))))
    (let [log-window
          (frame
            :title "Log"
            :content log-panel
            :on-close :dispose)]
      (-> log-window pack! show!))))

(defn present-log-window
  []
  (invoke-later
    (show-log-window)))

;; File find dialog

(defn create-file-find-dialog
  []
  (with-widgets [(text :id :text-source-file)
                 (text :id :text-dest-file)
                 (mig-panel
                   :id :file-find-panel
                   :items [["Source file"]
                           [text-source-file "pushx, growx, wrap"]
                           ["Destination file"]
                           [text-dest-file "pushx, growx, wrap"]])]
    (let [file-find-dialog
          (dialog
            :title "Select source and destination files"
            :content file-find-panel
            :modal? true
            :options [(button
                        :text "Select source file..."
                        :listen [:action (fn [e]
                                           (when-let [source-file (choose-file (to-root e)
                                                                    :type :open
                                                                    :filters [["Docbook" ["xml"]]])]
                                             (let [source-file (.getAbsolutePath source-file)]
                                               (text! text-source-file source-file)
                                               (when (empty? (text text-dest-file))
                                                 (text! text-dest-file (s/replace source-file #"\.xml$" ".habr"))))))])
                      (button
                        :text "Select destination file..."
                        :listen [:action (fn [e]
                                           (when-let [dest-file (choose-file (to-root e)
                                                                  :type :save)]
                                             (let [dest-file (.getAbsolutePath dest-file)]
                                               (text! text-dest-file dest-file))))])
                      (button
                        :text "OK"
                        :listen [:action (fn [e] (return-from-dialog e (and
                                                                         (seq (text text-source-file))
                                                                         (seq (text text-dest-file))
                                                                         [(text text-source-file)
                                                                          (text text-dest-file)])))])
                      (button
                        :text "Cancel"
                        :listen [:action (fn [e] (return-from-dialog e nil))])])]
      file-find-dialog)))

(defn ask-files
  []
  (let [result (promise)]
    (invoke-later
      (deliver result (-> (create-file-find-dialog) pack! show!)))
    @result))

