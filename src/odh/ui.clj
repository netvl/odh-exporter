(ns odh.ui
  (:use [seesaw core mig]))

(defn show-sample-dialog
  [sample]
  (with-widgets [(text :id :answer-text)
                 (text :id :sample-text
                       :multi-line? true
                       :editable? false)
                 (mig-panel :id :sample-panel
                            :items [["Source type"]
                                    [answer-text "growx, wrap"]
                                    [sample-text "grow, push, wrap"]])]
    (let [sample-dialog (dialog :content sample-panel
                                :title "Select source type"
                                :option-type :ok-cancel
                                :success-fn (fn [_] (text answer-text)))]
      (-> sample-dialog pack! show!))))

(defn ask-source-type
  [sample]
  (let [result (promise)]
    (invoke-later
      (deliver result (show-sample-dialog sample)))
    @result))
