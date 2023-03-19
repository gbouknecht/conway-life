(ns conway-life.ui.keyboard-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.keyboard :as keyboard]
            [conway-life.logic.simulator :as simulator]
            [conway-life.ui.ui-state :as ui-state]))

(deftest about-keyboard

  (let [ui-state (ui-state/make-ui-state [0 0] 0 (geometry/make-geometry))
        keys-pressed (fn [ui-state keys] (reduce (fn [ui-state key] (keyboard/key-pressed ui-state {:key key}))
                                                 ui-state keys))]

    (testing "should zoom out on '-', zoom in on '=' and '+' and reset on '0'"
      (letfn [(cell-size-after [& keys] (get-in (keys-pressed ui-state keys) [:geometry :cell-size]))]
        (is (= (cell-size-after :-) 1))
        (is (= (cell-size-after :=) 2))
        (is (= (cell-size-after :+) 2))
        (is (= (cell-size-after := :=) 3))
        (is (= (cell-size-after := := :-) 2))
        (is (= (cell-size-after := := :0) 1))))

    (testing "should show/hide raster on 'r'"
      (letfn [(show-raster-after [& keys] (:show-raster (keys-pressed ui-state keys)))]
        (is (false? (show-raster-after)))
        (is (true? (show-raster-after :r)))
        (is (false? (show-raster-after :r :r)))))

    (testing "should start/stop on 's' and step on 'n'"
      (letfn [(mode-after [& keys] (:mode (keys-pressed ui-state keys)))]
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
        (let [ui-state (keys-pressed ui-state [:C])]
          (is (= 0 (get-in ui-state [:board :generation-count])))
          (is (= 0 (board/number-of-on-cells (:board ui-state)))))))

    (letfn [(center-after [mode & keys]
              (let [ui-state (assoc ui-state :mode mode)]
                (get-in (keys-pressed ui-state keys) [:geometry :center])))
            (cursor-after [mode & keys]
              (let [ui-state (assoc ui-state :mode mode)]
                (get (keys-pressed ui-state keys) :cursor)))]

      (testing "should move board on left/right/up/down when mode is :running"
        (is (= (center-after :running :left) [10 0]))
        (is (= (center-after :running :left :left) [20 0]))
        (is (= (center-after :running :right) [-10 0]))
        (is (= (center-after :running :right :right) [-20 0]))
        (is (= (center-after :running :up) [0 -10]))
        (is (= (center-after :running :up :up) [0 -20]))
        (is (= (center-after :running :down) [0 10]))
        (is (= (center-after :running :down :down) [0 20]))
        (is (= (center-after :running :left :left :left :up :right :down :down :down :down) [20 30])))

      (testing "should not move board on left/right/up/down when mode is :stopped"
        (is (= (center-after :stopped :left) [0 0]))
        (is (= (center-after :stopped :right) [0 0]))
        (is (= (center-after :stopped :up) [0 0]))
        (is (= (center-after :stopped :down) [0 0])))

      (testing "should move cursor on left/right/up/down when mode is :stopped"
        (is (= (cursor-after :stopped :left) [-1 0]))
        (is (= (cursor-after :stopped :left :left) [-2 0]))
        (is (= (cursor-after :stopped :right) [1 0]))
        (is (= (cursor-after :stopped :right :right) [2 0]))
        (is (= (cursor-after :stopped :up) [0 1]))
        (is (= (cursor-after :stopped :up :up) [0 2]))
        (is (= (cursor-after :stopped :down) [0 -1]))
        (is (= (cursor-after :stopped :down :down) [0 -2]))
        (is (= (cursor-after :stopped :left :left :left :up :right :down :down :down :down) [-2 -3])))

      (testing "should not move cursor on left/right/up/down when mode is :running"
        (is (= (cursor-after :running :left) [0 0]))
        (is (= (cursor-after :running :right) [0 0]))
        (is (= (cursor-after :running :up) [0 0]))
        (is (= (cursor-after :running :down) [0 0]))))

    (testing "should let move board step size depends on cell size"
      (letfn [(center-after [cell-size & keys]
                (let [ui-state (-> ui-state
                                   (assoc-in [:geometry :cell-size] cell-size)
                                   (assoc :mode :running))]
                  (get-in (keys-pressed ui-state keys) [:geometry :center])))]
        (is (= (center-after 2 :left) [5 0]))
        (is (= (center-after 3 :left) [3 0]))
        (is (= (center-after 4 :left) [2 0]))
        (is (= (center-after 6 :left) [1 0]))
        (is (= (center-after 10 :left) [1 0]))
        (is (= (center-after 11 :left) [1 0]))
        (is (= (center-after 20 :left) [1 0]))
        (is (= (center-after 2 :right) [-5 0]))
        (is (= (center-after 10 :right) [-1 0]))
        (is (= (center-after 11 :right) [-1 0]))
        (is (= (center-after 20 :right) [-1 0]))
        (is (= (center-after 2 :up) [0 -5]))
        (is (= (center-after 10 :up) [0 -1]))
        (is (= (center-after 11 :up) [0 -1]))
        (is (= (center-after 20 :up) [0 -1]))
        (is (= (center-after 2 :down) [0 5]))
        (is (= (center-after 10 :down) [0 1]))
        (is (= (center-after 11 :down) [0 1]))
        (is (= (center-after 20 :down) [0 1]))))

    (testing "should center board and cursor on 'c'"
      (let [ui-state (-> ui-state
                         (assoc-in [:geometry :center] [2 3])
                         (assoc :cursor [4 5])
                         (keys-pressed [:c]))]
        (is (= (get-in ui-state [:geometry :center]) [0 0]))
        (is (= (get ui-state :cursor) [0 0]))))

    (testing "should toggle cell state at cursor on ' ' when mode is :stopped"
      (let [board (-> ui-state
                      (assoc :mode :stopped)
                      (assoc :cursor [4 5])
                      (keys-pressed [:space])
                      (assoc :cursor [13 11])
                      (keys-pressed [:space])
                      (assoc :cursor [1 2])
                      (keys-pressed [:space :space])
                      (:board))]
        (is (= (board/number-of-on-cells board) 2))
        (is (true? (board/on-cell? board [4 5])))
        (is (true? (board/on-cell? board [13 11])))))

    (testing "should not toggle cell state at cursor on ' ' when mode is :running"
      (let [board (-> ui-state
                      (assoc :mode :running)
                      (assoc :cursor [4 5])
                      (keys-pressed [:space])
                      (:board))]
        (is (= (board/number-of-on-cells board) 0))))))
