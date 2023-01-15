(ns conway-life.board
  (:require [conway-life.common :refer [off on]]))

(defn make-board [] {:generation-count 0 :on-cells #{}})
(defn number-of-on-cells [board] (count (:on-cells board)))
(defn set-cell-state [board cell state] (update board :on-cells (condp = state off disj on conj) cell))
(defn cell-on? [board cell] (contains? (:on-cells board) cell))
