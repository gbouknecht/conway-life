(ns conway-life.ui.actions-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.ui.actions :as actions]
            [conway-life.ui.common :refer [timed-call]]
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
      (check ui-state (make-action :restore-randomly-filled-board (:board ui-state))))

    (let [board (board/toggle-cell-state (:board ui-state) [0 0])]
      (check (actions/dispatch ui-state :restore-board board) (make-action :restore-board board)))))

(deftest about-storing-board-after-long-during-actions

  (let [duration-result-ns (atom nil)
        with-duration (fn [duration-ms action-fn] (do (reset! duration-result-ns (* duration-ms 1000000)) (action-fn)))]
    (with-redefs [timed-call (fn [action-fn] [(action-fn) @duration-result-ns])]

      (testing "should store board when redoable actions applied to latest stored board take more than 200 ms"
        (let [ui-state-0 (with-duration 150 #(actions/dispatch ui-state :toggle-cell-state [0 0]))
              ui-state-1 (with-duration 50 #(actions/dispatch ui-state-0 :toggle-cell-state [1 0]))
              ui-state-2 (with-duration 1 #(actions/dispatch ui-state-1 :toggle-cell-state [2 0]))
              ui-state-3 (with-duration 150 #(actions/dispatch ui-state-2 :toggle-cell-state [3 0]))
              ui-state-4 (with-duration 50 #(actions/dispatch ui-state-3 :toggle-cell-state [4 0]))
              ui-state-5 (with-duration 1 #(actions/dispatch ui-state-4 :toggle-cell-state [5 0]))
              ui-state-6 (reduce #(actions/dispatch %1 %2) ui-state-5 [:undo :undo :undo])
              ui-state-7 (with-duration 200 #(actions/dispatch ui-state-6 :toggle-cell-state [7 0]))
              ui-state-8 (with-duration 1 #(actions/dispatch ui-state-7 :toggle-cell-state [8 0]))]
          (is (= (:redoable-actions ui-state-0) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])]))
          (is (= (:redoable-actions ui-state-1) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :toggle-cell-state [1 0])]))
          (is (= (:redoable-actions ui-state-2) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :toggle-cell-state [1 0])
                                                 (make-action :toggle-cell-state [2 0])
                                                 (make-action :restore-board (:board ui-state-2))]))
          (is (= (:redoable-actions ui-state-3) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :toggle-cell-state [1 0])
                                                 (make-action :toggle-cell-state [2 0])
                                                 (make-action :restore-board (:board ui-state-2))
                                                 (make-action :toggle-cell-state [3 0])]))
          (is (= (:redoable-actions ui-state-4) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :toggle-cell-state [1 0])
                                                 (make-action :toggle-cell-state [2 0])
                                                 (make-action :restore-board (:board ui-state-2))
                                                 (make-action :toggle-cell-state [3 0])
                                                 (make-action :toggle-cell-state [4 0])]))
          (is (= (:redoable-actions ui-state-5) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :toggle-cell-state [1 0])
                                                 (make-action :toggle-cell-state [2 0])
                                                 (make-action :restore-board (:board ui-state-2))
                                                 (make-action :toggle-cell-state [3 0])
                                                 (make-action :toggle-cell-state [4 0])
                                                 (make-action :toggle-cell-state [5 0])
                                                 (make-action :restore-board (:board ui-state-5))]))
          (is (= (:redoable-actions ui-state-6) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :toggle-cell-state [1 0])
                                                 (make-action :toggle-cell-state [2 0])
                                                 (make-action :restore-board (:board ui-state-2))]))
          (is (= (:redoable-actions ui-state-7) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :toggle-cell-state [1 0])
                                                 (make-action :toggle-cell-state [2 0])
                                                 (make-action :restore-board (:board ui-state-2))
                                                 (make-action :toggle-cell-state [7 0])]))
          (is (= (:redoable-actions ui-state-8) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :toggle-cell-state [1 0])
                                                 (make-action :toggle-cell-state [2 0])
                                                 (make-action :restore-board (:board ui-state-2))
                                                 (make-action :toggle-cell-state [7 0])
                                                 (make-action :toggle-cell-state [8 0])
                                                 (make-action :restore-board (:board ui-state-8))]))))

      (testing "should enforce maximum number of stored cells by removing old stored boards"
        (let [ui-state (assoc ui-state :max-number-of-stored-cells 9)
              ui-state-0 (with-duration 201 #(actions/dispatch ui-state :toggle-cell-state [0 0]))
              ui-state-1 (with-duration 201 #(actions/dispatch ui-state-0 :toggle-cell-state [1 0]))
              ui-state-2 (with-duration 201 #(actions/dispatch ui-state-1 :toggle-cell-state [2 0]))
              ui-state-3 (with-duration 201 #(actions/dispatch ui-state-2 :toggle-cell-state [3 0]))]
          (is (= (:redoable-actions ui-state-0) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :restore-board (:board ui-state-0))]))
          (is (= (:redoable-actions ui-state-1) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :restore-board (:board ui-state-0))
                                                 (make-action :toggle-cell-state [1 0])
                                                 (make-action :restore-board (:board ui-state-1))]))
          (is (= (:redoable-actions ui-state-2) [(make-action :restore-board (:board ui-state))
                                                 (make-action :toggle-cell-state [0 0])
                                                 (make-action :restore-board (:board ui-state-0))
                                                 (make-action :toggle-cell-state [1 0])
                                                 (make-action :restore-board (:board ui-state-1))
                                                 (make-action :toggle-cell-state [2 0])
                                                 (make-action :restore-board (:board ui-state-2))]))
          (is (= (:redoable-actions ui-state-3) [(make-action :restore-board (:board ui-state-1))
                                                 (make-action :toggle-cell-state [2 0])
                                                 (make-action :restore-board (:board ui-state-2))
                                                 (make-action :toggle-cell-state [3 0])
                                                 (make-action :restore-board (:board ui-state-3))]))))

      (testing "should keep latest stored board even if it exceeds maximum number of stored cells"
        (let [ui-state (as-> ui-state ui-state
                             (assoc ui-state :max-number-of-stored-cells 2)
                             (with-duration 150 #(actions/dispatch ui-state :toggle-cell-state [0 0]))
                             (with-duration 50 #(actions/dispatch ui-state :toggle-cell-state [1 0]))
                             (with-duration 1 #(actions/dispatch ui-state :toggle-cell-state [2 0])))]
          (is (= (:redoable-actions ui-state) [(make-action :restore-board (:board ui-state))])))))))
