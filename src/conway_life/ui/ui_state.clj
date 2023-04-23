(ns conway-life.ui.ui-state
  (:require [conway-life.logic.board :as board]))

(defn make-ui-state [geometry
                     & {:keys [mode show-raster]
                        :or   {mode        :stopped
                               show-raster false}}]
  {:board       (board/make-board)
   :geometry    geometry
   :mode        mode
   :show-raster show-raster})
(defn clear [ui-state]
  (-> ui-state
      (assoc :board (board/make-board))
      (assoc-in [:geometry :center] [0 0])
      (assoc-in [:geometry :cursor] [0 0])
      (assoc :mode :stopped)))
