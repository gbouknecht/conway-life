(ns conway-life.ui.actions-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.ui.actions :as actions]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.input-ui-state :as input-ui-state]
            [conway-life.ui.ui-state :as ui-state]))

(def ^:private ui-state (merge (ui-state/make-ui-state (geometry/make-geometry :center [10 20]
                                                                               :cursor [30 40]
                                                                               :window-size [100 200]
                                                                               :cell-size 1
                                                                               :margin-top 10))
                               (input-ui-state/make-input-ui-state)))
(defn- make-action [type payload] {:type type :payload payload :redo true})

(deftest about-not-undoable-actions

  (letfn [(check [ui-state]
            (let [redoable-actions (:redoable-actions ui-state)]
              (is (= (-> redoable-actions count) 1))
              (is (= (-> redoable-actions peek (assoc :payload nil)) (make-action :restore-board nil)))))]

    (check (actions/dispatch ui-state :zoom-out))
    (check (actions/dispatch ui-state :zoom-in))
    (check (actions/dispatch ui-state :set-window-size [200 300]))
    (check (actions/dispatch ui-state :set-cell-size 5))
    (check (actions/dispatch ui-state :show-raster))

    (check (actions/dispatch ui-state :start-stop))
    (check (actions/dispatch ui-state :stop))
    (check (actions/dispatch ui-state :step))
    (check (actions/dispatch ui-state :update-time))

    (check (actions/dispatch ui-state :move-center [15 25]))
    (check (actions/dispatch ui-state :move-cursor [35 45]))
    (check (actions/dispatch ui-state :move-to-origin))

    (check (actions/dispatch ui-state :move-board-left))
    (check (actions/dispatch ui-state :move-board-right))
    (check (actions/dispatch ui-state :move-board-up))
    (check (actions/dispatch ui-state :move-board-down))

    (check (actions/dispatch ui-state :move-cursor-left))
    (check (actions/dispatch ui-state :move-cursor-right))
    (check (actions/dispatch ui-state :move-cursor-up))
    (check (actions/dispatch ui-state :move-cursor-down))))

(deftest about-undoable-actions

  (letfn [(check [ui-state expected-redoable-action]
            (let [redoable-actions (:redoable-actions ui-state)]
              (is (= (-> redoable-actions count) 2))
              (is (= (-> redoable-actions peek) expected-redoable-action))
              (is (= (-> redoable-actions pop peek (assoc :payload nil)) (make-action :restore-board nil)))))]

    (check (actions/dispatch ui-state :next) (make-action :next nil))
    (check (actions/dispatch ui-state :clear) (make-action :clear nil))

    (check (actions/dispatch ui-state :toggle-cell-state [35 45]) (make-action :toggle-cell-state [35 45]))
    (check (actions/dispatch ui-state :toggle-cell-state-at-cursor) (make-action :toggle-cell-state [30 40]))
    (let [ui-state (actions/dispatch ui-state :fill-board-randomly)]
      (check ui-state (make-action :restore-board (:board ui-state))))

    (let [board (board/toggle-cell-state (:board ui-state) [0 0])]
      (check (actions/dispatch ui-state :restore-board board) (make-action :restore-board board)))))
