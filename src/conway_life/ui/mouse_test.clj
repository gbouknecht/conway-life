(ns conway-life.ui.mouse-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.logic.common :refer [on]]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.mouse :as mouse]
            [conway-life.ui.ui-state :as ui-state]))

(deftest about-single-clicked

  (let [geometry (geometry/make-geometry :center [50 30] :cursor [40 20] :window-size [200 100] :cell-size 3)
        ui-state (ui-state/make-ui-state geometry)]

    (testing "should move cursor to cell when mode is :stopped"
      (let [ui-state (assoc ui-state :mode :stopped)]
        (is (= (mouse/single-clicked ui-state {:x 106 :y 41}) (assoc-in ui-state [:geometry :cursor] [52 33])))))

    (testing "should do nothing when mode is :running"
      (let [ui-state (assoc ui-state :mode :running)]
        (is (= (mouse/single-clicked ui-state {:x 10 :y 20}) ui-state))))))

(deftest about-double-clicked

  (let [geometry (geometry/make-geometry :center [50 30] :cursor [40 20] :window-size [200 100] :cell-size 3)
        ui-state (ui-state/make-ui-state geometry)]

    (testing "should toggle cell state when mode is :stopped"
      (let [ui-state (assoc ui-state :mode :stopped)
            board (-> ui-state
                      (mouse/double-clicked {:x 100 :y 50})
                      (mouse/double-clicked {:x 103 :y 50})
                      (mouse/double-clicked {:x 100 :y 44})
                      (mouse/double-clicked {:x 99 :y 54})
                      (mouse/double-clicked {:x 106 :y 41})
                      (mouse/double-clicked {:x 108 :y 39})
                      (:board))]
        (is (= (board/number-of-on-cells board) 4))
        (is (board/on-cell? board [50 30]))
        (is (board/on-cell? board [51 30]))
        (is (board/on-cell? board [50 32]))
        (is (board/on-cell? board [49 28]))
        (is (not (board/on-cell? board [52 33])))))

    (testing "should move cursor to cell when mode is :stopped"
      (let [ui-state (assoc ui-state :mode :stopped)]
        (is (= (mouse/double-clicked ui-state {:x 106 :y 41}) (-> ui-state
                                                                  (update :board #(board/set-cell-state % [52 33] on))
                                                                  (assoc-in [:geometry :cursor] [52 33]))))))

    (testing "should move center to clicked cell when mode is :running"
      (let [ui-state (assoc ui-state :mode :running)]
        (is (= (mouse/double-clicked ui-state {:x 106 :y 41}) (assoc-in ui-state [:geometry :center] [52 33])))))))
