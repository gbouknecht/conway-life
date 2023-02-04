(ns conway-life.ui.geometry-test
  (:require [clojure.test :refer :all]
            [conway-life.ui.geometry :as geometry]))

(deftest about-geometry

  (testing "should have defaults"
    (let [geometry (geometry/make-geometry)]
      (is (= (:center geometry) [0 0]))
      (is (nil? (:window-size geometry)))
      (is (= (:cell-size geometry) 1))))

  (testing "should be able to overwrite defaults"
    (let [geometry (geometry/make-geometry :center [2 3] :window-size [400 500] :cell-size 4)]
      (is (= (:center geometry) [2 3]))
      (is (= (:window-size geometry) [400 500]))
      (is (= (:cell-size geometry) 4))))

  (testing "should be able to map board coordinates to window coordinates"
    (are [center window-size cell-size coords window-coords]
      (= (geometry/to-window-coords coords (geometry/make-geometry :center center
                                                                   :window-size window-size
                                                                   :cell-size cell-size))
         window-coords)
      [0 0] [400 500] 1 [30 60] [230 190]
      [0 0] [400 500] 2 [30 60] [259 129]
      [0 0] [400 500] 3 [-30 60] [109 69]
      [0 0] [400 500] 3 [30 -60] [289 429]
      [0 0] [400 500] 4 [-30 -60] [78 488]
      [20 30] [400 500] 1 [30 60] [210 220]
      [20 30] [400 500] 2 [30 60] [219 189])))
