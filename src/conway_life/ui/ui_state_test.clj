(ns conway-life.ui.ui-state-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.board :as board]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.ui-state :as ui-state]))

(deftest about-ui-state

  (testing "should have filled board randomly, centered on (0, 0) with give width/height and fill percentage"
    (let [make-board board/make-board
          saved-make-board-result (atom nil)
          fill-randomly board/fill-randomly
          saved-fill-randomly-args (atom nil)
          saved-fill-randomly-result (atom nil)]
      (with-redefs [board/make-board (fn []
                                       (reset! saved-make-board-result (make-board)))
                    board/fill-randomly (fn [& args]
                                          (reset! saved-fill-randomly-args args)
                                          (reset! saved-fill-randomly-result (apply fill-randomly args)))]
        (let [[_ _ width height :as bounds] [-250 -150 500 300]
              percentage 75
              geometry (geometry/make-geometry :center [20 30])
              ui-state (ui-state/make-ui-state [width height] percentage geometry)]
          (is (= @saved-fill-randomly-args [@saved-make-board-result bounds percentage]))
          (is (= (:board ui-state) @saved-fill-randomly-result))
          (is (= (:geometry ui-state) geometry))))))

  (testing "should have defaults"
    (let [ui-state (ui-state/make-ui-state [500 300] 75 (geometry/make-geometry))]
      (is (= (:mode ui-state) :stopped))
      (is (false? (:show-raster ui-state)))))

  (testing "should be able to overwrite defaults"
    (let [ui-state (ui-state/make-ui-state [500 300] 75 (geometry/make-geometry)
                                           :mode :step
                                           :show-raster true)]
      (is (= (:mode ui-state) :step))
      (is (true? (:show-raster ui-state))))))
