(ns conway-life.ui.keyboard
  (:require [conway-life.logic.board :as board]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.ui-state :as ui-state]))

(defn key-pressed [ui-state event]
  (let [match-mode? (fn [mode] (= (:mode ui-state) mode))
        match-keys? (fn [& keys] (contains? (set keys) (:key event)))
        move-board-step-size (max 1 (quot 10 (get-in ui-state [:geometry :cell-size])))
        key-pressed
        (fn [ui-state]
          (cond-> ui-state
                  (match-keys? :-) (update-in [:geometry :cell-size] (comp (partial max 1) dec))
                  (match-keys? := :+) (update-in [:geometry :cell-size] inc)
                  (match-keys? :0) (assoc-in [:geometry :cell-size] 1)
                  (match-keys? :r) (update :show-raster not)
                  (match-keys? :s) (update :mode #(if (= % :running) :stopped :running))
                  (match-keys? :n) (assoc :mode :step)
                  (match-keys? :N) (-> (ui-state/pop-board)
                                       (assoc :mode :stopped))
                  (match-keys? :C) (ui-state/clear)
                  (match-keys? :c) (-> (assoc-in [:geometry :center] [0 0])
                                       (assoc-in [:geometry :cursor] [0 0]))))
        key-pressed-in-running-mode
        (fn [ui-state]
          (cond-> ui-state
                  (match-keys? :left) (update-in [:geometry :center] (fn [[x y]] [(+ x move-board-step-size) y]))
                  (match-keys? :right) (update-in [:geometry :center] (fn [[x y]] [(- x move-board-step-size) y]))
                  (match-keys? :up) (update-in [:geometry :center] (fn [[x y]] [x (- y move-board-step-size)]))
                  (match-keys? :down) (update-in [:geometry :center] (fn [[x y]] [x (+ y move-board-step-size)]))))
        key-pressed-in-stopped-mode
        (fn [ui-state]
          (letfn [(toggle-cell-state-at-cursor [ui-state]
                    (update ui-state :board #(board/toggle-cell-state % (get-in ui-state [:geometry :cursor]))))]
            (cond-> ui-state
                    (match-keys? :left) (update-in [:geometry :cursor] (fn [[x y]] [(dec x) y]))
                    (match-keys? :right) (update-in [:geometry :cursor] (fn [[x y]] [(inc x) y]))
                    (match-keys? :up) (update-in [:geometry :cursor] (fn [[x y]] [x (inc y)]))
                    (match-keys? :down) (update-in [:geometry :cursor] (fn [[x y]] [x (dec y)]))
                    (match-keys? :left :right :up :down) (update :geometry geometry/adjust-center-to-make-cursor-visible)
                    (match-keys? :space) (toggle-cell-state-at-cursor))))]
    (cond-> ui-state
            (match-mode? :running) (key-pressed-in-running-mode)
            (match-mode? :stopped) (key-pressed-in-stopped-mode)
            :always (key-pressed))))
