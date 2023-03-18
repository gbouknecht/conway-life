(ns conway-life.ui.keyboard
  (:require [conway-life.logic.board :as board]))

(defn key-typed [ui-state event]
  (let [match-mode? (fn [mode] (= (:mode ui-state) mode))
        match-keys? (fn [& keys] (contains? (set keys) (:key event)))
        move-board-step-size (max 1 (quot 10 (get-in ui-state [:geometry :cell-size])))
        key-typed
        (fn [ui-state]
          (cond-> ui-state
                  (match-keys? :-) (update-in [:geometry :cell-size] (comp (partial max 1) dec))
                  (match-keys? := :+) (update-in [:geometry :cell-size] inc)
                  (match-keys? :0) (assoc-in [:geometry :cell-size] 1)
                  (match-keys? :r) (update :show-raster not)
                  (match-keys? :s) (update :mode #(if (= % :running) :stopped :running))
                  (match-keys? :n) (assoc :mode :step)
                  (match-keys? :C) (assoc :board (board/make-board) :mode :stopped)
                  (match-keys? :c) (-> (assoc-in [:geometry :center] [0 0])
                                       (assoc :cursor [0 0]))))
        key-typed-in-running-mode
        (fn [ui-state]
          (cond-> ui-state
                  (match-keys? :h) (update-in [:geometry :center] (fn [[x y]] [(+ x move-board-step-size) y]))
                  (match-keys? :l) (update-in [:geometry :center] (fn [[x y]] [(- x move-board-step-size) y]))
                  (match-keys? :k) (update-in [:geometry :center] (fn [[x y]] [x (- y move-board-step-size)]))
                  (match-keys? :j) (update-in [:geometry :center] (fn [[x y]] [x (+ y move-board-step-size)]))))
        key-typed-in-stopped-mode
        (fn [ui-state]
          (letfn [(toggle-cell-state-at-cursor [ui-state]
                    (update ui-state :board #(board/toggle-cell-state % (board/make-cell (:cursor ui-state)))))]
            (cond-> ui-state
                    (match-keys? :h) (update :cursor (fn [[x y]] [(dec x) y]))
                    (match-keys? :l) (update :cursor (fn [[x y]] [(inc x) y]))
                    (match-keys? :k) (update :cursor (fn [[x y]] [x (inc y)]))
                    (match-keys? :j) (update :cursor (fn [[x y]] [x (dec y)]))
                    (match-keys? :space) (toggle-cell-state-at-cursor))))]
    (cond-> ui-state
            (match-mode? :running) (key-typed-in-running-mode)
            (match-mode? :stopped) (key-typed-in-stopped-mode)
            :always (key-typed))))
