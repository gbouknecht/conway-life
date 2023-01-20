(ns conway-life.simulator-test
  (:require
    [clojure.string :as str]
    [clojure.test :refer :all]
    [conway-life.board :as board]
    [conway-life.simulator :as simulator]))

(defn- set-pattern [board [x y] pattern]
  (letfn [(to-int [^Character c] (Character/digit c 10))]
    (reduce (fn [board [[dx dy] state]] (board/set-cell-state board (board/make-cell (+ x dx) (+ y dy)) state))
            board
            (->> pattern
                 (reverse)
                 (map-indexed (fn [dy row] (map-indexed (fn [dx state] [[dx dy] (to-int state)]) row)))
                 (apply concat)))))
(defn- get-pattern [board [x y width height]]
  (->> (for [dy (range height) dx (range width)] (if (board/cell-on? board (board/make-cell (+ x dx) (+ y dy))) "1" "0"))
       (partition width)
       (reverse)
       (mapv str/join)))
(defn- take-patterns [n board bounds] (->> board
                                           (iterate simulator/next-generation)
                                           (take n)
                                           (mapv #(get-pattern % bounds))))

(deftest about-simulator

  (testing "should increment generation count for each next generation"
    (let [boards (iterate simulator/next-generation (board/make-board))]
      (is (= (take 6 (map :generation-count boards)) [0 1 2 3 4 5]))))

  (testing "should generate empty board when board is empty"
    (let [board (simulator/next-generation (board/make-board))]
      (is (= (board/number-of-on-cells board) 0))))

  (testing "should generate empty board when board has only a single 'on' cell"
    (let [board (simulator/next-generation (-> (board/make-board)
                                               (set-pattern [0 0] ["1"])))]
      (is (= (board/number-of-on-cells board) 0))))

  (testing "should generate empty board when board has only two 'on' cells"
    (let [board (simulator/next-generation (-> (board/make-board)
                                               (set-pattern [0 0] ["11"])))]
      (is (= (board/number-of-on-cells board) 0))))

  (testing "should generate blinker when board has three 'on' cells in a row"
    (let [patterns (take-patterns 3
                                  (-> (board/make-board)
                                      (set-pattern [5 7]
                                                   ["111"]))
                                  [4 5 5 5])]
      (is (= patterns [["00000"
                        "00000"
                        "01110"
                        "00000"
                        "00000"]
                       ["00000"
                        "00100"
                        "00100"
                        "00100"
                        "00000"]
                       ["00000"
                        "00000"
                        "01110"
                        "00000"
                        "00000"]]))))

  (testing "should generate block when board has three 'on' cells in a L shape"
    (let [patterns (take-patterns 3
                                  (-> (board/make-board)
                                      (set-pattern [2 3]
                                                   ["10"
                                                    "11"]))
                                  [1 2 4 4])]
      (is (= patterns [["0000"
                        "0100"
                        "0110"
                        "0000"]
                       ["0000"
                        "0110"
                        "0110"
                        "0000"]
                       ["0000"
                        "0110"
                        "0110"
                        "0000"]]))))

  (testing "should generate a beehive when board has four 'on' cells in a row"
    (let [patterns (take-patterns 4
                                  (-> (board/make-board)
                                      (set-pattern [0 0]
                                                   ["1111"]))
                                  [-1 -2 6 5])]
      (is (= patterns [["000000"
                        "000000"
                        "011110"
                        "000000"
                        "000000"]
                       ["000000"
                        "001100"
                        "001100"
                        "001100"
                        "000000"]
                       ["000000"
                        "001100"
                        "010010"
                        "001100"
                        "000000"]
                       ["000000"
                        "001100"
                        "010010"
                        "001100"
                        "000000"]]))))

  (testing "should generate a traffic light when has a T tetromino"
    (let [patterns (take-patterns 11
                                  (-> (board/make-board)
                                      (set-pattern [-1 -1]
                                                   ["111"
                                                    "010"]))
                                  [-5 -5 11 11])]
      (is (= patterns [["00000000000"
                        "00000000000"
                        "00000000000"
                        "00000000000"
                        "00000000000"
                        "00001110000"
                        "00000100000"
                        "00000000000"
                        "00000000000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000000000"
                        "00000000000"
                        "00000000000"
                        "00000100000"
                        "00001110000"
                        "00001110000"
                        "00000000000"
                        "00000000000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000000000"
                        "00000000000"
                        "00000000000"
                        "00001110000"
                        "00000000000"
                        "00001010000"
                        "00000100000"
                        "00000000000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000000000"
                        "00000000000"
                        "00000100000"
                        "00000100000"
                        "00001010000"
                        "00000100000"
                        "00000100000"
                        "00000000000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000000000"
                        "00000000000"
                        "00000000000"
                        "00001110000"
                        "00001010000"
                        "00001110000"
                        "00000000000"
                        "00000000000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000000000"
                        "00000000000"
                        "00000100000"
                        "00001010000"
                        "00010001000"
                        "00001010000"
                        "00000100000"
                        "00000000000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000000000"
                        "00000000000"
                        "00000100000"
                        "00001110000"
                        "00011011000"
                        "00001110000"
                        "00000100000"
                        "00000000000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000000000"
                        "00000000000"
                        "00001110000"
                        "00010001000"
                        "00010001000"
                        "00010001000"
                        "00001110000"
                        "00000000000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000000000"
                        "00000100000"
                        "00001110000"
                        "00010101000"
                        "00111011100"
                        "00010101000"
                        "00001110000"
                        "00000100000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000000000"
                        "00001110000"
                        "00000000000"
                        "00100000100"
                        "00100000100"
                        "00100000100"
                        "00000000000"
                        "00001110000"
                        "00000000000"
                        "00000000000"]
                       ["00000000000"
                        "00000100000"
                        "00000100000"
                        "00000100000"
                        "00000000000"
                        "01110001110"
                        "00000000000"
                        "00000100000"
                        "00000100000"
                        "00000100000"
                        "00000000000"]]))))

  (testing "should generate a moving glider"
    (let [patterns (take-patterns 5
                                  (-> (board/make-board)
                                      (set-pattern [-1 -1]
                                                   ["010"
                                                    "001"
                                                    "111"]))
                                  [-2 -3 6 6])]
      (is (= patterns [["000000"
                        "001000"
                        "000100"
                        "011100"
                        "000000"
                        "000000"]
                       ["000000"
                        "000000"
                        "010100"
                        "001100"
                        "001000"
                        "000000"]
                       ["000000"
                        "000000"
                        "000100"
                        "010100"
                        "001100"
                        "000000"]
                       ["000000"
                        "000000"
                        "001000"
                        "000110"
                        "001100"
                        "000000"]
                       ["000000"
                        "000000"
                        "000100"
                        "000010"
                        "001110"
                        "000000"]])))))
