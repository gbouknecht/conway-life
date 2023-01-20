(ns conway-life.board-test
  (:require [clojure.test :refer :all]
            [conway-life.board :as board]
            [conway-life.common :refer [off on]]))

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
      (is (false? (board/cell-on? board [5 7])))
      (is (false? (board/cell-on? board [5 8])))
      (is (true? (board/cell-on? board [8 1])))
      (is (true? (board/cell-on? board [2 3])))
      (is (true? (board/cell-on? board [6 6])))
      (is (= (board/on-cells board) #{[8 1] [2 3] [6 6]})))))
