(ns conway-life.ui.mouse
  (:require [conway-life.logic.board :as board]
            [conway-life.ui.geometry :as geometry]))

(defn single-clicked [ui-state {window-x :x window-y :y}]
  (if (= (:mode ui-state) :stopped)
    (let [[x y] (geometry/to-coords [window-x window-y] (:geometry ui-state))]
      (update ui-state :board #(board/toggle-cell-state % (board/make-cell x y))))
    ui-state))
(def double-clicked single-clicked)
