(ns conway-life.logic.board-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.logic.common :refer [off on]]))

(deftest about-board

  (testing "should be at generation 0 and without any 'on' cells on initialization"
    (let [board (board/make-board)]
      (is (= (:generation-count board) 0))
      (is (= (board/number-of-on-cells board) 0))))

  (testing "should be able to set state of cell 'on' or 'off'"
    (let [board (-> (board/make-board)
                    (board/set-cell-state [5 7] on)
                    (board/set-cell-state [5 8] on)
                    (board/set-cell-state [8 1] on)
                    (board/set-cell-state [5 7] off)
                    (board/set-cell-state [8 1] on)
                    (board/set-cell-state [2 3] on)
                    (board/set-cell-state [5 8] off)
                    (board/set-cell-state [5 8] off)
                    (board/set-cell-state [6 6] on))]
      (is (= (board/number-of-on-cells board) 3))
      (is (false? (board/on-cell? board [5 7])))
      (is (false? (board/on-cell? board [5 8])))
      (is (true? (board/on-cell? board [8 1])))
      (is (true? (board/on-cell? board [2 3])))
      (is (true? (board/on-cell? board [6 6])))
      (is (= (set (board/all-on-cell-coords board)) #{[8 1] [2 3] [6 6]}))))

  (testing "should be able to toggle state of cell between 'on' and 'off'"
    (let [board (-> (board/make-board)
                    (board/toggle-cell-state [5 7])
                    (board/toggle-cell-state [5 8])
                    (board/toggle-cell-state [8 1])
                    (board/toggle-cell-state [5 7]))]
      (is (= (board/number-of-on-cells board) 2))
      (is (false? (board/on-cell? board [5 7])))
      (is (true? (board/on-cell? board [5 8])))
      (is (true? (board/on-cell? board [8 1])))))

  (testing "should be able to provide coordinates for given bounds"
    (let [bounds [4 7 2 3]]
      (is (= (board/all-coords bounds) [[4 7] [5 7] [4 8] [5 8] [4 9] [5 9]]))))

  (testing "should be able to fill the board randomly"
    (let [[x y width height :as bounds] [10 20 500 300]
          percentage 75
          board (-> (board/make-board)
                    (board/fill-randomly bounds percentage))
          [all-xs all-ys] (let [flattened-coords (flatten (board/all-on-cell-coords board))]
                            [(take-nth 2 flattened-coords) (take-nth 2 (rest flattened-coords))])
          percentage-on-cells (/ (* (board/number-of-on-cells board) 100) (* width height))]
      (is (= (apply min all-xs) x))
      (is (= (apply max all-xs) (dec (+ x width))))
      (is (= (apply min all-ys) y))
      (is (= (apply max all-ys) (dec (+ y height))))
      (is (< (abs (- percentage percentage-on-cells)) 1)))))
