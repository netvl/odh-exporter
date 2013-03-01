(ns odh.ui
  (:use [seesaw core mig]))

(defn show-sample-dialog
  [sample]
  (with-widgets [(text :id :text-answer)
                 (text
                   :id :text-sample
                   :multi-line? true
                   :text sample)
                 (mig-panel :id :sample-panel
                            :items [["Source type" "split"]
                                    [text-answer "growx, pushx, wrap"]
                                    [text-sample "grow, push, spanx 2"]])]
    (let [sample-dialog (dialog :content sample-panel
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
