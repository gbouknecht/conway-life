(ns conway-life.ui.ui-state
  (:require [conway-life.logic.board :as board]))

(defn make-ui-state [fill-size fill-percentage geometry
                     & {:keys [mode show-raster]
                        :or   {mode        :stopped
                               show-raster false}}]
  (let [[width height] fill-size
        x (- (/ width 2))
        y (- (/ height 2))
        bounds [x y width height]]
    {:board       (board/fill-randomly (board/make-board) bounds fill-percentage)
     :geometry    geometry
     :mode        mode
     :show-raster show-raster}))
