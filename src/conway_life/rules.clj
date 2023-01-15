(ns conway-life.rules)

(def off 0)
(def on 1)

(defn next-state [current-state number-of-on-neighbors]
  (cond
    (= number-of-on-neighbors 2) current-state
    (= number-of-on-neighbors 3) on
    :else off))
