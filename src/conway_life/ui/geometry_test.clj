(ns conway-life.ui.geometry-test
  (:require [clojure.test :refer :all]
            [conway-life.ui.geometry :as geometry]))

(deftest about-geometry

  (testing "should have defaults"
    (let [geometry (geometry/make-geometry)]
      (is (= (:center geometry) [0 0]))
      (is (= (:cursor geometry) [0 0]))
      (is (nil? (:window-size geometry)))
      (is (= (:cell-size geometry) 1))
      (is (= (:margin-top geometry) 30))))

  (testing "should be able to overwrite defaults"
    (let [geometry (geometry/make-geometry :center [2 3]
                                           :cursor [4 5]
                                           :window-size [400 500]
                                           :cell-size 4
                                           :margin-top 25)]
      (is (= (:center geometry) [2 3]))
      (is (= (:cursor geometry) [4 5]))
      (is (= (:window-size geometry) [400 500]))
      (is (= (:cell-size geometry) 4))
      (is (= (:margin-top geometry) 25))))

  (testing "should be able to map (board) coordinate to window coordinate of upper-left corner"
    (are [center window-size cell-size coords window-coords]
      (= (geometry/to-window-coords coords (geometry/make-geometry :center center
                                                                   :window-size window-size
                                                                   :cell-size cell-size))
         window-coords)
      [0 0] [400 500] 1 [0 0] [200 250]
      [0 0] [400 500] 1 [1 0] [201 250]
      [0 0] [400 500] 1 [0 1] [200 249]
      [0 0] [400 500] 1 [1 1] [201 249]
      [0 0] [400 500] 1 [-1 -1] [199 251]

      [0 0] [400 500] 2 [0 0] [200 249]
      [0 0] [400 500] 2 [1 1] [202 247]
      [0 0] [400 500] 2 [-1 -1] [198 251]

      [0 0] [400 500] 3 [0 0] [200 248]
      [0 0] [400 500] 3 [1 1] [203 245]
      [0 0] [400 500] 3 [-1 -1] [197 251]
      [0 0] [400 500] 3 [-2 -2] [194 254]

      [1 0] [400 500] 3 [0 0] [197 248]
      [0 1] [400 500] 3 [0 0] [200 251]
      [1 1] [400 500] 3 [0 0] [197 251]
      [-1 -1] [400 500] 3 [0 0] [203 245]))

  (testing "should be able to map window coordinate to (board) coordinate"
    (are [center window-size cell-size window-coords coords]
      (= (geometry/to-coords window-coords (geometry/make-geometry :center center
                                                                   :window-size window-size
                                                                   :cell-size cell-size))
         coords)
      [0 0] [400 500] 1 [200 250] [0 0]
      [0 0] [400 500] 1 [201 250] [1 0]
      [0 0] [400 500] 1 [200 249] [0 1]
      [0 0] [400 500] 1 [201 249] [1 1]
      [0 0] [400 500] 1 [199 251] [-1 -1]

      [0 0] [400 500] 2 [200 250] [0 0]
      [0 0] [400 500] 2 [201 249] [0 0]
      [0 0] [400 500] 2 [202 248] [1 1]
      [0 0] [400 500] 2 [199 251] [-1 -1]
      [0 0] [400 500] 2 [198 252] [-1 -1]

      [0 0] [400 500] 3 [200 250] [0 0]
      [0 0] [400 500] 3 [201 249] [0 0]
      [0 0] [400 500] 3 [202 248] [0 0]
      [0 0] [400 500] 3 [203 247] [1 1]
      [0 0] [400 500] 3 [199 251] [-1 -1]
      [0 0] [400 500] 3 [198 252] [-1 -1]
      [0 0] [400 500] 3 [197 253] [-1 -1]
      [0 0] [400 500] 3 [196 254] [-2 -2]
      [0 0] [400 500] 3 [196 254] [-2 -2]

      [1 0] [400 500] 3 [200 250] [1 0]
      [0 1] [400 500] 3 [200 250] [0 1]
      [1 1] [400 500] 3 [200 250] [1 1]
      [1 1] [400 500] 3 [203 247] [2 2]
      [-1 -1] [400 500] 3 [200 250] [-1 -1])))
