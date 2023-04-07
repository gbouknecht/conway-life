(ns conway-life.ui.mouse
  (:require [conway-life.logic.board :as board]
            [conway-life.ui.geometry :as geometry]))

(defn single-clicked [ui-state {window-x :x window-y :y}]
  (if (= (:mode ui-state) :stopped)
    (let [[x y] (geometry/to-coords [window-x window-y] (:geometry ui-state))]
      (assoc-in ui-state [:geometry :cursor] [x y]))
    ui-state))
(defn double-clicked [ui-state {window-x :x window-y :y}]
  (let [[x y] (geometry/to-coords [window-x window-y] (:geometry ui-state))]
    (if (= (:mode ui-state) :stopped)
      (-> ui-state
          (update :board #(board/toggle-cell-state % [x y]))
          (assoc-in [:geometry :cursor] [x y]))
      (assoc-in ui-state [:geometry :center] [x y]))))
