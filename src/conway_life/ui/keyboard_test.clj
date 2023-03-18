(ns conway-life.ui.keyboard-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.keyboard :as keyboard]
            [conway-life.logic.simulator :as simulator]
            [conway-life.ui.ui-state :as ui-state]))

(deftest about-keyboard

  (let [ui-state (ui-state/make-ui-state [0 0] 0 (geometry/make-geometry))
        keys-typed (fn [ui-state keys] (reduce (fn [ui-state key] (keyboard/key-typed ui-state {:key key}))
                                               ui-state keys))]

    (testing "should zoom out on '-', zoom in on '=' and '+' and reset on '0'"
      (letfn [(cell-size-after [& keys] (get-in (keys-typed ui-state keys) [:geometry :cell-size]))]
        (is (= (cell-size-after :-) 1))
        (is (= (cell-size-after :=) 2))
        (is (= (cell-size-after :+) 2))
        (is (= (cell-size-after := :=) 3))
        (is (= (cell-size-after := := :-) 2))
        (is (= (cell-size-after := := :0) 1))))

    (testing "should show/hide raster on 'r'"
      (letfn [(show-raster-after [& keys] (:show-raster (keys-typed ui-state keys)))]
        (is (false? (show-raster-after)))
        (is (true? (show-raster-after :r)))
        (is (false? (show-raster-after :r :r)))))

    (testing "should start/stop on 's' and step on 'n'"
      (letfn [(mode-after [& keys] (:mode (keys-typed ui-state keys)))]
        (is (= (mode-after :s) :running))
        (is (= (mode-after :s :s) :stopped))
        (is (= (mode-after :s :s :s) :running))
        (is (= (mode-after :n) :step))
        (is (= (mode-after :n :n) :step))
        (is (= (mode-after :n :s) :running))))

    (testing "should clear board on 'C'"
      (let [ui-state (-> ui-state
                         (update :board #(board/fill-randomly % [10 20 50 30] 75))
                         (update :board simulator/next-generation)
                         (update :board simulator/next-generation))]
        (is (= 2 (get-in ui-state [:board :generation-count])))
        (is (< 0 (board/number-of-on-cells (:board ui-state))))
        (let [ui-state (keys-typed ui-state [:C])]
          (is (= 0 (get-in ui-state [:board :generation-count])))
          (is (= 0 (board/number-of-on-cells (:board ui-state)))))))

    (letfn [(center-after [mode & keys]
              (let [ui-state (assoc ui-state :mode mode)]
                (get-in (keys-typed ui-state keys) [:geometry :center])))
            (cursor-after [mode & keys]
              (let [ui-state (assoc ui-state :mode mode)]
                (get (keys-typed ui-state keys) :cursor)))]

      (testing "should move board left/right/up/down on 'h', 'l', 'k', 'j' when mode is :running"
        (is (= (center-after :running :h) [10 0]))
        (is (= (center-after :running :h :h) [20 0]))
        (is (= (center-after :running :l) [-10 0]))
        (is (= (center-after :running :l :l) [-20 0]))
        (is (= (center-after :running :k) [0 -10]))
        (is (= (center-after :running :k :k) [0 -20]))
        (is (= (center-after :running :j) [0 10]))
        (is (= (center-after :running :j :j) [0 20]))
        (is (= (center-after :running :h :h :h :k :l :j :j :j :j) [20 30])))

      (testing "should not move board left/right/up/down on 'h', 'l', 'k', 'j' when mode is :stopped"
        (is (= (center-after :stopped :h) [0 0]))
        (is (= (center-after :stopped :l) [0 0]))
        (is (= (center-after :stopped :k) [0 0]))
        (is (= (center-after :stopped :j) [0 0])))

      (testing "should move cursor left/right/up/down on 'h', 'l', 'k', 'j' when mode is :stopped"
        (is (= (cursor-after :stopped :h) [-1 0]))
        (is (= (cursor-after :stopped :h :h) [-2 0]))
        (is (= (cursor-after :stopped :l) [1 0]))
        (is (= (cursor-after :stopped :l :l) [2 0]))
        (is (= (cursor-after :stopped :k) [0 1]))
        (is (= (cursor-after :stopped :k :k) [0 2]))
        (is (= (cursor-after :stopped :j) [0 -1]))
        (is (= (cursor-after :stopped :j :j) [0 -2]))
        (is (= (cursor-after :stopped :h :h :h :k :l :j :j :j :j) [-2 -3])))

      (testing "should not move cursor left/right/up/down on 'h', 'l', 'k', 'j' when mode is :running"
        (is (= (cursor-after :running :h) [0 0]))
        (is (= (cursor-after :running :l) [0 0]))
        (is (= (cursor-after :running :k) [0 0]))
        (is (= (cursor-after :running :j) [0 0]))))

    (testing "should let move board step size depends on cell size"
      (letfn [(center-after [cell-size & keys]
                (let [ui-state (-> ui-state
                                   (assoc-in [:geometry :cell-size] cell-size)
                                   (assoc :mode :running))]
                  (get-in (keys-typed ui-state keys) [:geometry :center])))]
        (is (= (center-after 2 :h) [5 0]))
        (is (= (center-after 3 :h) [3 0]))
        (is (= (center-after 4 :h) [2 0]))
        (is (= (center-after 6 :h) [1 0]))
        (is (= (center-after 10 :h) [1 0]))
        (is (= (center-after 11 :h) [1 0]))
        (is (= (center-after 20 :h) [1 0]))
        (is (= (center-after 2 :l) [-5 0]))
        (is (= (center-after 10 :l) [-1 0]))
        (is (= (center-after 11 :l) [-1 0]))
        (is (= (center-after 20 :l) [-1 0]))
        (is (= (center-after 2 :k) [0 -5]))
        (is (= (center-after 10 :k) [0 -1]))
        (is (= (center-after 11 :k) [0 -1]))
        (is (= (center-after 20 :k) [0 -1]))
        (is (= (center-after 2 :j) [0 5]))
        (is (= (center-after 10 :j) [0 1]))
        (is (= (center-after 11 :j) [0 1]))
        (is (= (center-after 20 :j) [0 1]))))

    (testing "should center board and cursor on 'c'"
      (let [ui-state (-> ui-state
                         (assoc-in [:geometry :center] [2 3])
                         (assoc :cursor [4 5])
                         (keys-typed [:c]))]
        (is (= (get-in ui-state [:geometry :center]) [0 0]))
        (is (= (get ui-state :cursor) [0 0]))))

    (testing "should toggle cell state at cursor on ' ' when mode is :stopped"
      (let [board (-> ui-state
                      (assoc :mode :stopped)
                      (assoc :cursor [4 5])
                      (keys-typed [:space])
                      (assoc :cursor [13 11])
                      (keys-typed [:space])
                      (assoc :cursor [1 2])
                      (keys-typed [:space :space])
                      (:board))]
        (is (= (board/number-of-on-cells board) 2))
        (is (true? (board/on-cell? board [4 5])))
        (is (true? (board/on-cell? board [13 11])))))

    (testing "should not toggle cell state at cursor on ' ' when mode is :running"
      (let [board (-> ui-state
                      (assoc :mode :running)
                      (assoc :cursor [4 5])
                      (keys-typed [:space])
                      (:board))]
        (is (= (board/number-of-on-cells board) 0))))))
