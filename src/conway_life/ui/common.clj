(ns conway-life.ui.common)

(defn time-ms [] (System/currentTimeMillis))
(defn time-ns [] (System/nanoTime))
(defn timed-call [f] (let [start (time-ns) result (f) end (time-ns)] [result (- end start)]))
