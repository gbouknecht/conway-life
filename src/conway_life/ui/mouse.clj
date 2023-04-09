(ns conway-life.ui.mouse
  (:require [conway-life.ui.actions :as actions]
            [conway-life.ui.geometry :as geometry]))

(defn single-clicked [ui-state {window-x :x window-y :y}]
  (if (= (:mode ui-state) :stopped)
    (let [[x y] (geometry/to-coords [window-x window-y] (:geometry ui-state))]
      (actions/dispatch ui-state :move-cursor [x y]))
    ui-state))
(defn double-clicked [ui-state {window-x :x window-y :y}]
  (let [[x y] (geometry/to-coords [window-x window-y] (:geometry ui-state))]
    (if (= (:mode ui-state) :stopped)
      (-> ui-state
          (actions/dispatch :move-cursor [x y])
          (actions/dispatch :toggle-cell-state-at-cursor))
      (actions/dispatch ui-state :move-center [x y]))))
