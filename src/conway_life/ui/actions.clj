(ns conway-life.ui.actions
  (:require [conway-life.logic.board :as board]
            [conway-life.logic.simulator :as simulator]
            [conway-life.ui.common :refer [time-ms]]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.input-ui-state :as input-ui-state]
            [conway-life.ui.ui-state :as ui-state]))

(defn push-redoable-actions [ui-state action]
  (cond-> ui-state
          (not (:redo action)) (update :redo-actions empty)
          :always (update :redoable-actions conj (assoc action :redo true))))

(defn- store-board [ui-state]
  (push-redoable-actions ui-state {:type    :restore-board
                                   :payload (:board ui-state)
                                   :redo    true}))

(defmulti dispatch-action (fn ([_ action] (:type action))))

(defn dispatch
  ([ui-state type] (dispatch ui-state type nil))
  ([ui-state type payload] (cond-> ui-state
                                   (empty? (:redoable-actions ui-state)) (store-board)
                                   :always (dispatch-action {:type type :payload payload}))))

(defmethod dispatch-action :zoom-out [ui-state _]
  (update-in ui-state [:geometry :cell-size] (comp (partial max 1) dec)))
(defmethod dispatch-action :zoom-in [ui-state _]
  (update-in ui-state [:geometry :cell-size] inc))
(defmethod dispatch-action :set-window-size [ui-state {window-size :payload}]
  (assoc-in ui-state [:geometry :window-size] window-size))
(defmethod dispatch-action :set-cell-size [ui-state {cell-size :payload}]
  (assoc-in ui-state [:geometry :cell-size] cell-size))
(defmethod dispatch-action :show-raster [ui-state _]
  (update ui-state :show-raster not))

(defmethod dispatch-action :start-stop [ui-state _]
  (update ui-state :mode #(if (= % :running) :stopped :running)))
(defmethod dispatch-action :stop [ui-state _]
  (assoc ui-state :mode :stopped))
(defmethod dispatch-action :step [ui-state _]
  (assoc ui-state :mode :step))
(defmethod dispatch-action :next [ui-state action]
  (-> ui-state
      (update :board simulator/next-generation)
      (push-redoable-actions action)))
(defmethod dispatch-action :clear [ui-state action]
  (-> ui-state
      (ui-state/clear)
      (push-redoable-actions action)))
(defmethod dispatch-action :update-time [ui-state _]
  (input-ui-state/update-time-ms ui-state (time-ms)))

(defmethod dispatch-action :move-center [ui-state {[x y] :payload}]
  (assoc-in ui-state [:geometry :center] [x y]))
(defmethod dispatch-action :move-cursor [ui-state {[x y] :payload}]
  (-> ui-state
      (assoc-in [:geometry :cursor] [x y])
      (update :geometry geometry/adjust-center-to-make-cursor-visible)))
(defmethod dispatch-action :move-to-origin [ui-state _]
  (-> ui-state
      (assoc-in [:geometry :center] [0 0])
      (assoc-in [:geometry :cursor] [0 0])))

(defn- move-center-by-factors-of-step-size [ui-state x-factor y-factor]
  (let [step-size (max 1 (quot 10 (get-in ui-state [:geometry :cell-size])))
        [x y] (get-in ui-state [:geometry :center])]
    (dispatch ui-state :move-center [(+ x (* x-factor step-size)) (+ y (* y-factor step-size))])))
(defmethod dispatch-action :move-board-left [ui-state _]
  (move-center-by-factors-of-step-size ui-state 1 0))
(defmethod dispatch-action :move-board-right [ui-state _]
  (move-center-by-factors-of-step-size ui-state -1 0))
(defmethod dispatch-action :move-board-up [ui-state _]
  (move-center-by-factors-of-step-size ui-state 0 -1))
(defmethod dispatch-action :move-board-down [ui-state _]
  (move-center-by-factors-of-step-size ui-state 0 1))

(defn- move-cursor-by-deltas [ui-state xd yd]
  (let [[x y] (get-in ui-state [:geometry :cursor])]
    (dispatch ui-state :move-cursor [(+ x xd) (+ y yd)])))
(defmethod dispatch-action :move-cursor-left [ui-state _]
  (move-cursor-by-deltas ui-state -1 0))
(defmethod dispatch-action :move-cursor-right [ui-state _]
  (move-cursor-by-deltas ui-state 1 0))
(defmethod dispatch-action :move-cursor-up [ui-state _]
  (move-cursor-by-deltas ui-state 0 1))
(defmethod dispatch-action :move-cursor-down [ui-state _]
  (move-cursor-by-deltas ui-state 0 -1))

(defmethod dispatch-action :toggle-cell-state [ui-state {cursor :payload :as action}]
  (-> ui-state
      (dispatch :move-cursor cursor)
      (update :board #(board/toggle-cell-state % cursor))
      (push-redoable-actions action)))
(defmethod dispatch-action :toggle-cell-state-at-cursor [ui-state _]
  (dispatch ui-state :toggle-cell-state (get-in ui-state [:geometry :cursor])))
(defmethod dispatch-action :fill-board-randomly [ui-state _]
  (let [geometry (:geometry ui-state)
        [center-x center-y] (:center geometry)
        [width height] (mapv #(quot % (:cell-size geometry)) (:window-size geometry))
        x (- center-x (/ width 2))
        y (- center-y (/ height 2))
        bounds [x y width height]
        percentage 15]
    (-> ui-state
        (update :board #(board/fill-randomly % bounds percentage))
        (store-board))))

(defmethod dispatch-action :restore-board [ui-state {board :payload}]
  (-> ui-state
      (assoc :board board)
      (store-board)))
(defmethod dispatch-action :undo [ui-state _]
  (let [latest-action (peek (:redoable-actions ui-state))
        rest-redoable-actions (pop (:redoable-actions ui-state))]
    (if (empty? rest-redoable-actions)
      (do
        (assert (= (:type latest-action) :restore-board))
        ui-state)
      (let [[actions-to-redo rest-redoable-actions] (loop [actions-to-redo `()
                                                           rest-redoable-actions rest-redoable-actions]
                                                      (if (= (:type (peek actions-to-redo)) :restore-board)
                                                        [actions-to-redo rest-redoable-actions]
                                                        (recur (conj actions-to-redo (peek rest-redoable-actions))
                                                               (pop rest-redoable-actions))))
            dispatch-actions #(reduce dispatch-action %1 %2)]
        (-> ui-state
            (assoc :redoable-actions rest-redoable-actions)
            (dispatch-actions actions-to-redo)
            (update :redo-actions conj latest-action))))))
(defmethod dispatch-action :redo [ui-state _]
  (let [redo-action (peek (:redo-actions ui-state))]
    (if (not redo-action)
      ui-state
      (-> ui-state
          (update :redo-actions pop)
          (dispatch-action redo-action)))))
