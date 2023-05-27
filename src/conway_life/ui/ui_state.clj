(ns conway-life.ui.ui-state
  (:require [conway-life.logic.board :as board]))

(defn make-ui-state [geometry
                     & {:keys [mode show-raster max-number-of-stored-cells]
                        :or   {mode                       :stopped
                               show-raster                false
                               max-number-of-stored-cells 50000000}}]
  {:board                      (board/make-board)
   :geometry                   geometry
   :mode                       mode
   :show-raster                show-raster
   :max-number-of-stored-cells max-number-of-stored-cells})
(defn clear [ui-state]
  (-> ui-state
      (assoc :board (board/make-board))
      (assoc-in [:geometry :center] [0 0])
      (assoc-in [:geometry :cursor] [0 0])
      (assoc :mode :stopped)))
