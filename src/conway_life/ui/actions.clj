(ns conway-life.ui.actions
  (:require [conway-life.logic.board :as board]
            [conway-life.logic.simulator :as simulator]
            [conway-life.ui.common :refer [time-ms timed-call]]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.input-ui-state :as input-ui-state]
            [conway-life.ui.ui-state :as ui-state])
  (:import (java.time LocalDateTime)))

(def ^:private total-duration-threshold-ms 200)

(defn- push-redoable-actions [ui-state action]
  (cond-> ui-state
          (not (:redo action)) (update :redo-actions empty)
          :always (update :redoable-actions conj (assoc action :redo true))))

(defn- store-board [ui-state]
  (-> ui-state
      (push-redoable-actions {:type    :restore-board
                              :payload (:board ui-state)
                              :redo    true})
      (assoc :total-duration-ms 0)))

(defn call-and-push [action-fn redoable-action]
  (let [[ui-state duration-ms] (timed-call action-fn)
        total-duration-ms (+ (:total-duration-ms ui-state) duration-ms)]
    (cond-> ui-state
            :always (assoc :total-duration-ms total-duration-ms)
            :always (push-redoable-actions redoable-action)
            (> total-duration-ms total-duration-threshold-ms) (store-board))))

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
  (call-and-push #(update ui-state :board simulator/next-generation) action))
(defmethod dispatch-action :clear [ui-state action]
  (call-and-push #(ui-state/clear ui-state) action))
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
  (call-and-push (fn []
                   (-> ui-state
                       (dispatch :move-cursor cursor)
                       (update :board #(board/toggle-cell-state % cursor))))
                 action))
(defmethod dispatch-action :toggle-cell-state-at-cursor [ui-state _]
  (dispatch ui-state :toggle-cell-state (get-in ui-state [:geometry :cursor])))
(defmethod dispatch-action :restore-randomly-filled-board [ui-state {board :payload :as action}]
  (call-and-push #(assoc ui-state :board board) action))
(defmethod dispatch-action :fill-board-randomly [ui-state _]
  (let [geometry (:geometry ui-state)
        [center-x center-y] (:center geometry)
        [width height] (mapv #(quot % (:cell-size geometry)) (:window-size geometry))
        x (- center-x (quot width 2))
        y (- center-y (quot height 2))
        bounds [x y width height]
        percentage 15]
    (dispatch ui-state :restore-randomly-filled-board (board/fill-randomly (:board ui-state) bounds percentage))))

(defmethod dispatch-action :restore-board [ui-state {board :payload}]
  (-> ui-state
      (assoc :board board)
      (store-board)))
(defmethod dispatch-action :undo [ui-state _]
  (loop [latest-action (peek (:redoable-actions ui-state))
         rest-redoable-actions (pop (:redoable-actions ui-state))]
    (if (empty? rest-redoable-actions)
      (do
        (assert (= (:type latest-action) :restore-board))
        ui-state)
      (if (= (:type latest-action) :restore-board)
        (recur (peek rest-redoable-actions) (pop rest-redoable-actions))
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
              (update :redo-actions conj latest-action)))))))
(defmethod dispatch-action :redo [ui-state _]
  (let [redo-action (peek (:redo-actions ui-state))]
    (if (not redo-action)
      ui-state
      (-> ui-state
          (update :redo-actions pop)
          (dispatch-action redo-action)))))

(defmethod dispatch-action :print-statistics [ui-state _]
  (let [board (:board ui-state)
        header (format "--- Conway's Game of Life statistics (%s) ---" (LocalDateTime/now))
        footer (apply str (repeat (count header) "-"))
        stored-boards (filter #(= (:type %) :restore-board) (:redoable-actions ui-state))]
    (println header)
    (println (format "Generation                       : %d" (:generation-count board)))
    (println (format "Number of cells                  : %d" (board/number-of-on-cells board)))
    (println (format "Number of redoable actions       : %d" (count (:redoable-actions ui-state))))
    (println (format "Number of redo actions           : %d" (count (:redo-actions ui-state))))
    (println (format "Number of stored boards          : %d" (count stored-boards)))
    (println (format "Number of stored cells           : %d" (->> stored-boards (map (comp board/number-of-on-cells :payload)) (reduce +))))
    (println (format "Duration from latest stored board: %d ms" (:total-duration-ms ui-state)))
    (println footer)
    ui-state))
