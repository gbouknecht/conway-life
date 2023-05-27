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
      (is (= (:geometry ui-state) geometry))))

  (testing "should have defaults"
    (let [ui-state (ui-state/make-ui-state (geometry/make-geometry))]
      (is (= (:mode ui-state) :stopped))
      (is (false? (:show-raster ui-state)))
      (is (= (:max-number-of-stored-cells ui-state) 50000000))))

  (testing "should be able to overwrite defaults"
    (let [ui-state (ui-state/make-ui-state (geometry/make-geometry)
                                           :mode :step
                                           :show-raster true
                                           :max-number-of-stored-cells 1000000)]
      (is (= (:mode ui-state) :step))
      (is (true? (:show-raster ui-state)))
      (is (= (:max-number-of-stored-cells ui-state) 1000000))))

  (testing "should be able to clear state"
    (let [ui-state (-> (ui-state/make-ui-state (geometry/make-geometry :center [10 20]
                                                                       :cursor [5 10]
                                                                       :cell-size 3)
                                               :mode :running)
                       (update :board #(board/fill-randomly % [-25 -15 50 30] 5))
                       (update :board simulator/next-generation)
                       (ui-state/clear))]
      (is (= (get-in ui-state [:board :generation-count]) 0))
      (is (= (board/number-of-on-cells (:board ui-state)) 0))
      (is (= (get-in ui-state [:geometry :center]) [0 0]))
      (is (= (get-in ui-state [:geometry :cursor]) [0 0]))
      (is (= (get-in ui-state [:geometry :cell-size]) 3))
      (is (= (:mode ui-state) :stopped)))))
