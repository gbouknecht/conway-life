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

    (testing "should zoom out on `-`, zoom in on `=` and `+` and reset on '0'"
      (letfn [(cell-size-after [& keys] (get-in (keys-typed ui-state keys) [:geometry :cell-size]))]
        (is (= (cell-size-after :-) 1))
        (is (= (cell-size-after :=) 2))
        (is (= (cell-size-after :+) 2))
        (is (= (cell-size-after := :=) 3))
        (is (= (cell-size-after := := :-) 2))
        (is (= (cell-size-after := := :0) 1))))

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

    (testing "should move board left on 'h', right on 'l', up on 'k', down on 'j' and center on 'c'"
      (letfn [(center-after [& keys] (get-in (keys-typed ui-state keys) [:geometry :center]))]
        (is (= (center-after :h) [10 0]))
        (is (= (center-after :h :h) [20 0]))
        (is (= (center-after :l) [-10 0]))
        (is (= (center-after :l :l) [-20 0]))
        (is (= (center-after :k) [0 -10]))
        (is (= (center-after :k :k) [0 -20]))
        (is (= (center-after :j) [0 10]))
        (is (= (center-after :j :j) [0 20]))
        (is (= (center-after :h :h :h :k :l :j :j :j :j) [20 30]))
        (is (= (center-after :h :h :h :k :l :j :j :j :j :c) [0 0]))))))
