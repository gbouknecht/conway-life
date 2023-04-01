(ns conway-life.ui.ui-state
  (:require [conway-life.logic.board :as board]))

(defn make-ui-state [fill-size fill-percentage geometry
                     & {:keys [max-board-stack-size mode show-raster]
                        :or   {max-board-stack-size 500
                               mode                 :stopped
                               show-raster          false}}]
  (let [[width height] fill-size
        x (- (/ width 2))
        y (- (/ height 2))
        bounds [x y width height]]
    {:board                (board/fill-randomly (board/make-board) bounds fill-percentage)
     :board-stack          []
     :max-board-stack-size max-board-stack-size
     :geometry             geometry
     :mode                 mode
     :show-raster          show-raster}))
(defn push-board [ui-state]
  (-> ui-state
      (update :board-stack conj (:board ui-state))
      (update :board-stack #(vec (take-last (:max-board-stack-size ui-state) %)))))
(defn pop-board [ui-state]
  (if (empty? (:board-stack ui-state))
    ui-state
    (-> ui-state
        (assoc :board (peek (:board-stack ui-state)))
        (update :board-stack pop))))
