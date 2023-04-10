(ns conway-life.ui.actions
  (:require [conway-life.logic.board :as board]
            [conway-life.logic.simulator :as simulator]
            [conway-life.ui.common :refer [time-ms]]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.input-ui-state :as input-ui-state]
            [conway-life.ui.ui-state :as ui-state]))

(defmulti dispatch (fn ([_ type] type) ([_ type _] type)))

(defmethod dispatch :zoom-out [ui-state _]
  (update-in ui-state [:geometry :cell-size] (comp (partial max 1) dec)))
(defmethod dispatch :zoom-in [ui-state _]
  (update-in ui-state [:geometry :cell-size] inc))
(defmethod dispatch :set-window-size [ui-state _ window-size]
  (assoc-in ui-state [:geometry :window-size] window-size))
(defmethod dispatch :set-cell-size [ui-state _ cell-size]
  (assoc-in ui-state [:geometry :cell-size] cell-size))
(defmethod dispatch :show-raster [ui-state _]
  (update ui-state :show-raster not))

(defmethod dispatch :start-stop [ui-state _]
  (update ui-state :mode #(if (= % :running) :stopped :running)))
(defmethod dispatch :stop [ui-state _]
  (assoc ui-state :mode :stopped))
(defmethod dispatch :step [ui-state _]
  (assoc ui-state :mode :step))
(defmethod dispatch :next [ui-state _]
  (-> ui-state
      (ui-state/push-board)
      (update :board simulator/next-generation)))
(defmethod dispatch :previous [ui-state _]
  (-> ui-state
      (ui-state/pop-board)
      (assoc :mode :stopped)))
(defmethod dispatch :clear [ui-state _]
  (ui-state/clear ui-state))
(defmethod dispatch :update-time [ui-state _]
  (input-ui-state/update-time-ms ui-state (time-ms)))

(defmethod dispatch :move-center [ui-state _ [x y]]
  (assoc-in ui-state [:geometry :center] [x y]))
(defmethod dispatch :move-cursor [ui-state _ [x y]]
  (assoc-in ui-state [:geometry :cursor] [x y]))
(defmethod dispatch :move-to-origin [ui-state _]
  (-> ui-state
      (assoc-in [:geometry :center] [0 0])
      (assoc-in [:geometry :cursor] [0 0])))

(defn- move-center-by-factors-of-step-size [ui-state x-factor y-factor]
  (let [step-size (max 1 (quot 10 (get-in ui-state [:geometry :cell-size])))]
    (update-in ui-state [:geometry :center] (fn [[x y]] [(+ x (* x-factor step-size)) (+ y (* y-factor step-size))]))))
(defmethod dispatch :move-board-left [ui-state _]
  (move-center-by-factors-of-step-size ui-state 1 0))
(defmethod dispatch :move-board-right [ui-state _]
  (move-center-by-factors-of-step-size ui-state -1 0))
(defmethod dispatch :move-board-up [ui-state _]
  (move-center-by-factors-of-step-size ui-state 0 -1))
(defmethod dispatch :move-board-down [ui-state _]
  (move-center-by-factors-of-step-size ui-state 0 1))

(defn- move-cursor-by-deltas [ui-state xd yd]
  (-> ui-state
      (update-in [:geometry :cursor] (fn [[x y]] [(+ x xd) (+ y yd)]))
      (update :geometry geometry/adjust-center-to-make-cursor-visible)))
(defmethod dispatch :move-cursor-left [ui-state _]
  (move-cursor-by-deltas ui-state -1 0))
(defmethod dispatch :move-cursor-right [ui-state _]
  (move-cursor-by-deltas ui-state 1 0))
(defmethod dispatch :move-cursor-up [ui-state _]
  (move-cursor-by-deltas ui-state 0 1))
(defmethod dispatch :move-cursor-down [ui-state _]
  (move-cursor-by-deltas ui-state 0 -1))

(defmethod dispatch :toggle-cell-state-at-cursor [ui-state _]
  (update ui-state :board #(board/toggle-cell-state % (get-in ui-state [:geometry :cursor]))))
(defmethod dispatch :fill-board-randomly [ui-state _]
  (let [geometry (:geometry ui-state)
        [center-x center-y] (:center geometry)
        [width height] (mapv #(quot % (:cell-size geometry)) (:window-size geometry))
        x (- center-x (/ width 2))
        y (- center-y (/ height 2))
        bounds [x y width height]
        percentage 15]
    (update ui-state :board #(board/fill-randomly % bounds percentage))))
