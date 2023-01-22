(ns conway-life.logic.board
  (:require [conway-life.logic.common :refer [off on]]))

(defn make-board [] {:generation-count 0 :on-cells #{}})
(defn make-cell [x y] [x y])
(defn cell-coord [cell] cell)
(defn number-of-on-cells [board] (count (:on-cells board)))
(defn set-cell-state [board cell state] (update board :on-cells (condp = state off disj on conj) cell))
(defn cell-on? [board cell] (contains? (:on-cells board) cell))
(defn on-cells [board] (:on-cells board))
