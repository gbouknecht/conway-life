(ns conway-life.ui.ui-state
  (:require [conway-life.logic.board :as board]))

(defn make-ui-state [fill-size fill-percentage geometry
                     & {:keys [mode cursor show-raster]
                        :or   {mode        :stopped
                               cursor      [0 0]
                               show-raster false}}]
  (let [[width height] fill-size
        x (- (/ width 2))
        y (- (/ height 2))
        bounds [x y width height]]
    {:board       (board/fill-randomly (board/make-board) bounds fill-percentage)
     :geometry    geometry
     :mode        mode
     :cursor      cursor
     :show-raster show-raster}))
