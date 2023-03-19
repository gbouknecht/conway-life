(ns conway-life.ui.mouse-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.mouse :as mouse]
            [conway-life.ui.ui-state :as ui-state]))

(deftest about-single-clicked-and-double-clicked

  (doseq [clicked-fn [mouse/single-clicked mouse/double-clicked]]

    (testing (str clicked-fn " should toggle cell state when mode is :stopped")
      (let [geometry (geometry/make-geometry :center [50 30] :window-size [200 100] :cell-size 3)
            ui-state (ui-state/make-ui-state [0 0] 0 geometry :mode :stopped)
            board (-> ui-state
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
        (is (not (board/on-cell? board [52 33])))))

    (testing (str clicked-fn " should move cursor to clicked cell when mode is :stopped")
      (let [geometry (geometry/make-geometry :center [50 30] :window-size [200 100] :cell-size 3)
            ui-state (-> (ui-state/make-ui-state [0 0] 0 geometry :mode :stopped)
                         (clicked-fn {:x 100 :y 50}))]
        (is (= (:cursor ui-state) [50 30]))))))

(deftest about-single-clicked

  (testing "should do nothing when mode is :running"
    (let [geometry (geometry/make-geometry :center [50 30] :window-size [200 100] :cell-size 3)
          ui-state (ui-state/make-ui-state [0 0] 0 geometry :mode :running)]
      (is (= (mouse/single-clicked ui-state {:x 10 :y 20}) ui-state)))))

(deftest about-double-clicked

  (testing "should center when mode is :running"
    (let [geometry (geometry/make-geometry :center [50 30] :window-size [200 100] :cell-size 3)
          ui-state (ui-state/make-ui-state [0 0] 0 geometry :mode :running)]
      (is (= (get-in (mouse/double-clicked ui-state {:x 106 :y 41}) [:geometry :center]) [52 33])))))
