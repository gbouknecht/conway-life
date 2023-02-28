(ns conway-life.ui.keyboard
  (:require [conway-life.logic.board :as board]))

(defn key-typed [ui-state event]
  (letfn [(match? [& keys] (contains? (set keys) (:key event)))]
    (cond-> ui-state
            (match? :-) (update-in [:geometry :cell-size] (comp (partial max 1) dec))
            (match? := :+) (update-in [:geometry :cell-size] inc)
            (match? :0) (assoc-in [:geometry :cell-size] 1)
            (match? :s) (update :mode #(if (= % :running) :stopped :running))
            (match? :n) (assoc :mode :step)
            (match? :C) (assoc :board (board/make-board) :mode :stopped)
            (match? :c) (assoc-in [:geometry :center] [0 0])
            (match? :h) (update-in [:geometry :center] (fn [[x y]] [(+ x 10) y]))
            (match? :l) (update-in [:geometry :center] (fn [[x y]] [(- x 10) y]))
            (match? :k) (update-in [:geometry :center] (fn [[x y]] [x (- y 10)]))
            (match? :j) (update-in [:geometry :center] (fn [[x y]] [x (+ y 10)])))))
