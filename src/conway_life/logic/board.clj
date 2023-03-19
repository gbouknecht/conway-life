(ns conway-life.logic.board
  (:require [conway-life.logic.common :refer [off on]]))

(defn make-board [] {:generation-count 0 :on-cells #{}})
(defn number-of-on-cells [board] (count (:on-cells board)))
(defn set-cell-state [board cell state] (update board :on-cells (condp = state off disj on conj) cell))
(defn on-cell? [board cell] (contains? (:on-cells board) cell))
(defn toggle-cell-state [board cell] (set-cell-state board cell (if (on-cell? board cell) off on)))
(defn on-cells [board] (:on-cells board))
(defn all-coords [[x y width height]] (for [dy (range height) dx (range width)] [(+ x dx) (+ y dy)]))
(defn fill-randomly [board bounds percentage]
  (reduce (fn [board [x y]] (set-cell-state board [x y] (if (< (rand-int 100) percentage) on off)))
          board
          (all-coords bounds)))
