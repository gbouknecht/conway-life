(ns conway-life.logic.simulator
  (:require [clojure.set :refer [union]]
            [conway-life.logic.board :as board]
            [conway-life.logic.common :refer [off on]]
            [conway-life.logic.rules :as rules]))

(defn next-generation [board]
  (let [on-cells (board/on-cells board)
        counted-neighbors (reduce (fn [counted-neighbors [x y]]
                                    (let [neighbor-cells (for [dy [-1 0 1] dx [-1 0 1] :when (not (= 0 dx dy))] [(+ x dx) (+ y dy)])]
                                      (reduce (fn [counted-neighbors neighbor-cell]
                                                (update counted-neighbors neighbor-cell (fnil inc 0)))
                                              counted-neighbors neighbor-cells)))
                                  {} on-cells)
        current-state #(if (contains? on-cells %) on off)
        set-cell-states (fn [board] (reduce (fn [board cell]
                                              (let [state (rules/next-state (current-state cell) (get counted-neighbors cell 0))]
                                                (board/set-cell-state board cell state)))
                                            board (union on-cells (set (keys counted-neighbors)))))]
    (-> board
        (set-cell-states)
        (update :generation-count inc))))
