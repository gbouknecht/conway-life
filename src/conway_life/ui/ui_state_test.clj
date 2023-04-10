(ns conway-life.ui.ui-state-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.logic.simulator :as simulator]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.ui-state :as ui-state]))

(deftest about-ui-state

  (testing "should have empty board after initialization"
    (let [geometry (geometry/make-geometry :center [20 30])
          ui-state (ui-state/make-ui-state geometry)]
      (is (= (:board ui-state) (board/make-board)))
      (is (empty? (:board-stack ui-state)))
      (is (= (:geometry ui-state) geometry))))

  (testing "should have defaults"
    (let [ui-state (ui-state/make-ui-state (geometry/make-geometry))]
      (is (= (:max-board-stack-size ui-state) 500))
      (is (= (:mode ui-state) :stopped))
      (is (false? (:show-raster ui-state)))))

  (testing "should be able to overwrite defaults"
    (let [ui-state (ui-state/make-ui-state (geometry/make-geometry)
                                           :max-board-stack-size 175
                                           :mode :step
                                           :show-raster true)]
      (is (= (:max-board-stack-size ui-state) 175))
      (is (= (:mode ui-state) :step))
      (is (true? (:show-raster ui-state)))))

  (testing "should be able to clear state"
    (let [ui-state (-> (ui-state/make-ui-state (geometry/make-geometry :center [10 20]
                                                                       :cursor [5 10]
                                                                       :cell-size 3)
                                               :mode :running)
                       (update :board #(board/fill-randomly % [-25 -15 50 30] 5))
                       (ui-state/push-board)
                       (update :board simulator/next-generation)
                       (ui-state/clear))]
      (is (= (get-in ui-state [:board :generation-count]) 0))
      (is (= (board/number-of-on-cells (:board ui-state)) 0))
      (is (= (get-in ui-state [:geometry :center]) [0 0]))
      (is (= (get-in ui-state [:geometry :cursor]) [0 0]))
      (is (= (get-in ui-state [:geometry :cell-size]) 3))
      (is (= (-> ui-state ui-state/pop-board :board) (:board ui-state)))
      (is (= (:mode ui-state) :stopped))))

  (testing "should be able to push/pop board on stack limited by max-board-stack-size"
    (let [ui-state-0 (ui-state/make-ui-state (geometry/make-geometry) :max-board-stack-size 3)
          board-1 (board/toggle-cell-state (:board ui-state-0) [0 0])
          board-2 (board/toggle-cell-state board-1 [1 0])
          board-3 (board/toggle-cell-state board-2 [0 1])
          board-4 (board/toggle-cell-state board-3 [1 1])
          ui-state-1 (-> ui-state-0
                         ui-state/push-board (assoc :board board-1)
                         ui-state/push-board (assoc :board board-2)
                         ui-state/push-board (assoc :board board-3)
                         ui-state/push-board (assoc :board board-4))]
      (is (= (-> ui-state-1 ui-state/pop-board :board) board-3))
      (is (= (-> ui-state-1 ui-state/pop-board ui-state/pop-board :board) board-2))
      (is (= (-> ui-state-1 ui-state/pop-board ui-state/pop-board ui-state/pop-board :board) board-1))
      (is (= (-> ui-state-1 ui-state/pop-board ui-state/pop-board ui-state/pop-board ui-state/pop-board :board) board-1)))))
