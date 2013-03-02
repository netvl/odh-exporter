(ns odh.ui
  (:use [seesaw core mig]))

;; Sample dialog

(defn show-sample-dialog
  [sample]
  (with-widgets [(text
                   :id :text-answer)
                 (text
                   :id :text-sample
                   :multi-line? true
                   :text sample)
                 (mig-panel :id :sample-panel
                   :items [["Source type" "split"]
                           [text-answer "growx, pushx, wrap"]
                           [text-sample "grow, push, spanx 2"]])]
    (let [sample-dialog
          (dialog
            :content sample-panel
            :title "Select source type"
            :option-type :ok-cancel
            :success-fn (fn [_] (text text-answer)))]
      (-> sample-dialog pack! show!))))

(defn ask-source-type
  [sample]
  (let [result (promise)]
    (invoke-later
      (deliver result (show-sample-dialog sample)))
    @result))

;; Logging

(def log-agent (agent))

(defn log
  [& args]
  (send-off log-agent (constantly (apply str args))))

(defn show-log-dialog
  []
  (with-widgets [(text
                   :id :text-log
                   :multi-line? true)
                 (mig-panel :id :log-panel
                   :items [["Log" "wrap"]
                           [text-log "growx, pushx"]])]
    (add-watch log-agent :log-dialog
      (fn [_ _ _ line]
        (text! text-log (str (text text-log) line "\n"))))
    (let [log-dialog
          (dialog
            :content log-panel
            :title "Log:")])))
