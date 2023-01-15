(ns conway-life.rules
  (:require [conway-life.common :refer [off on]]))

(defn next-state [current-state number-of-on-neighbors]
  (cond
    (= number-of-on-neighbors 2) current-state
    (= number-of-on-neighbors 3) on
    :else off))
