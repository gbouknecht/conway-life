(ns conway-life.ui.keyboard-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.keyboard :as keyboard]
            [conway-life.logic.simulator :as simulator]
            [conway-life.ui.ui-state :as ui-state]))

(def ^:private ui-state (ui-state/make-ui-state (geometry/make-geometry :center [0 0]
                                                                        :cursor [0 0]
                                                                        :window-size [100 200]
                                                                        :cell-size 1
                                                                        :margin-top 10)))
(defn- keys-pressed [ui-state keys] (reduce #(keyboard/key-pressed %1 {:key %2}) ui-state keys))

(deftest about-keyboard

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
      (is (= (get-in ui-state [:board :generation-count]) 2))
      (is (> (board/number-of-on-cells (:board ui-state)) 0))
      (let [ui-state (keys-pressed ui-state [:C])]
        (is (= (get-in ui-state [:board :generation-count]) 0))
        (is (= (board/number-of-on-cells (:board ui-state)) 0)))))

  (letfn [(center-after [mode & keys]
            (let [ui-state (assoc ui-state :mode mode)]
              (get-in (keys-pressed ui-state keys) [:geometry :center])))
          (cursor-after [mode & keys]
            (let [ui-state (assoc ui-state :mode mode)]
              (get-in (keys-pressed ui-state keys) [:geometry :cursor])))]

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
      (is (= (cursor-after :running :down) [0 0])))

    (testing "should adjust center when cursor moves out of view"
      (is (= (apply center-after :stopped (repeat 50 :left)) [0 0]))
      (is (= (apply center-after :stopped (repeat 51 :left)) [-1 0]))
      (is (= (apply center-after :stopped (repeat 49 :right)) [0 0]))
      (is (= (apply center-after :stopped (repeat 50 :right)) [1 0]))
      (is (= (apply center-after :stopped (repeat 89 :up)) [0 0]))
      (is (= (apply center-after :stopped (repeat 90 :up)) [0 1]))
      (is (= (apply center-after :stopped (repeat 100 :down)) [0 0]))
      (is (= (apply center-after :stopped (repeat 101 :down)) [0 -1]))))

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
                       (assoc-in [:geometry :cursor] [4 5])
                       (keys-pressed [:c]))]
      (is (= (get-in ui-state [:geometry :center]) [0 0]))
      (is (= (get-in ui-state [:geometry :cursor]) [0 0]))))

  (testing "should toggle cell state at cursor on ' ' when mode is :stopped"
    (let [board (-> ui-state
                    (assoc :mode :stopped)
                    (assoc-in [:geometry :cursor] [4 5])
                    (keys-pressed [:space])
                    (assoc-in [:geometry :cursor] [13 11])
                    (keys-pressed [:space])
                    (assoc-in [:geometry :cursor] [1 2])
                    (keys-pressed [:space :space])
                    (:board))]
      (is (= (board/number-of-on-cells board) 2))
      (is (true? (board/on-cell? board [4 5])))
      (is (true? (board/on-cell? board [13 11])))))

  (testing "should not toggle cell state at cursor on ' ' when mode is :running"
    (let [board (-> ui-state
                    (assoc :mode :running)
                    (assoc-in [:geometry :cursor] [4 5])
                    (keys-pressed [:space])
                    (:board))]
      (is (= (board/number-of-on-cells board) 0))))

  (testing "should fill board randomly bounded by window on 'R' when mode is :stopped"
    (let [fill-randomly board/fill-randomly
          saved-fill-randomly-args (atom nil)
          saved-fill-randomly-result (atom nil)]
      (with-redefs [board/fill-randomly (fn [& args]
                                          (reset! saved-fill-randomly-args args)
                                          (reset! saved-fill-randomly-result (apply fill-randomly args)))]
        (let [board (:board ui-state)
              filled-board (-> ui-state
                               (assoc-in [:geometry :center] [20 30])
                               (assoc-in [:geometry :window-size] [101 201])
                               (assoc :mode :stopped)
                               (keys-pressed [:R])
                               (:board))
              bounds [-30 -70 101 201]
              percentage 15]
          (is (= @saved-fill-randomly-args [board bounds percentage]))
          (is (= filled-board @saved-fill-randomly-result))))))

  (testing "should not fill board randomly on 'R' when mode is :running"
    (let [board (-> ui-state
                    (assoc :mode :running)
                    (keys-pressed [:R])
                    (:board))]
      (is (= (board/number-of-on-cells board) 0)))))

(deftest about-undo-redo

  (let [ui-state-0 (-> ui-state
                       (assoc :mode :stopped)
                       (keys-pressed [:space :right :space :right :space]))
        ui-state-1 (-> ui-state-0
                       (keys-pressed [:up :space :left :up :space]))]

    (testing "should undo latest board change on 'u' when mode is :stopped"
      (is (= (-> ui-state-1 (keys-pressed [:u]) :board) (-> ui-state-0 (keys-pressed [:up :space]) :board)))
      (is (= (-> ui-state-1 (keys-pressed [:u :u]) :board) (:board ui-state-0)))
      (is (= (-> ui-state-1 (keys-pressed [:u :u :u :u :u]) :board) (:board ui-state)))
      (is (= (-> ui-state-1 (keys-pressed [:u :u :u :u :u :u]) :board) (:board ui-state))))

    (testing "should not undo latest board change on 'u' when mode is :running"
      (let [ui-state-0 (assoc ui-state-0 :mode :running)]
        (is (= (-> ui-state-0 (keys-pressed [:u])) ui-state-0))))

    (testing "should redo latest undo on 'U' when mode is :stopped"
      (is (= (-> ui-state-1 (keys-pressed [:u :U]) :board) (:board ui-state-1)))
      (is (= (-> ui-state-1 (keys-pressed [:u :U :u :u]) :board) (:board ui-state-0)))
      (is (= (-> ui-state-1 (keys-pressed [:u :u :U :U]) :board) (:board ui-state-1)))
      (is (= (-> ui-state-1 (keys-pressed [:u :U :u :U]) :board) (:board ui-state-1)))
      (is (= (-> ui-state-1 (keys-pressed [:u :u :U :U :U]) :board) (:board ui-state-1))))

    (testing "should not redo latest undo on 'U' when mode is :stopped"
      (let [ui-state-1 (-> ui-state-1
                           (keys-pressed [:u])
                           (assoc :mode :running))]
        (is (= (-> ui-state-1 (keys-pressed [:U]) :board) (:board ui-state-1)))))

    (testing "should clear redo actions when new action is execute after undo"
      (is (= (-> ui-state-1 (keys-pressed [:u :u :down :space :U]) :board) (-> ui-state-0 (keys-pressed [:down :space]) :board)))
      (is (= (-> ui-state-1 (keys-pressed [:u :u :up :space :U]) :board) (-> ui-state-0 (keys-pressed [:up :space]) :board))))))
