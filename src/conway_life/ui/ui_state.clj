(ns conway-life.ui.ui-state
  (:require [conway-life.logic.board :as board]))

(defn make-initial-ui-state [fill-size fill-percentage geometry
                             & {:keys [mode] :or {mode :stopped}}]
  (let [[width height] fill-size
        x (- (/ width 2))
        y (- (/ height 2))
        bounds [x y width height]]
    {:board    (board/fill-randomly (board/make-board) bounds fill-percentage)
     :geometry geometry
     :mode     mode}))
