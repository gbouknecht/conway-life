(ns conway-life.ui.mouse-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.mouse :as mouse]
            [conway-life.ui.ui-state :as ui-state]))

(deftest about-single-clicked-and-double-clicked

  (doseq [clicked-fn [mouse/single-clicked mouse/double-clicked]]

    (testing clicked-fn

      (let [geometry (geometry/make-geometry :center [50 30] :window-size [200 100] :cell-size 3)
            ui-state (ui-state/make-ui-state [0 0] 0 geometry)]

        (testing "should do nothing when not in stopped mode"
          (let [ui-state (assoc ui-state :mode :running)]
            (is (= (clicked-fn ui-state {:x 10 :y 20}) ui-state))))

        (testing "should toggle cell state when in stopped mode"
          (let [board (-> (assoc ui-state :mode :stopped)
                          (clicked-fn {:x 100 :y 50})
                          (clicked-fn {:x 103 :y 50})
                          (clicked-fn {:x 100 :y 44})
                          (clicked-fn {:x 99 :y 54})
                          (clicked-fn {:x 106 :y 41})
                          (clicked-fn {:x 108 :y 39})
                          (:board))]
            (is (= (board/number-of-on-cells board) 4))
            (is (board/on-cell? board [50 30]))
            (is (board/on-cell? board [51 30]))
            (is (board/on-cell? board [50 32]))
            (is (board/on-cell? board [49 28]))
            (is (not (board/on-cell? board [52 33])))))))))
