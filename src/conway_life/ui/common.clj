(ns conway-life.ui.common)

(defn time-ms [] (System/currentTimeMillis))
(defn timed-call [f] (let [start (time-ms) result (f) end (time-ms)] [result (- end start)]))
