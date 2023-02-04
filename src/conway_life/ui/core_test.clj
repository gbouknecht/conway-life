(ns conway-life.ui.core-test
  (:require [clojure.test :refer :all]
            [conway-life.ui.core :as core]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.ui-state :as ui-state]
            [quil.core :as q]))

(deftest about-update-ui-state

  (let [ui-state (ui-state/make-initial-ui-state [0 0] 0 (geometry/make-geometry))
        window-width 500
        window-height 600]
    (with-redefs [q/width (constantly window-width)
                  q/height (constantly window-height)]

      (testing "should go to next generation when mode is :running"
        (let [next-ui-state (core/update-ui-state (assoc ui-state :mode :running))]
          (is (= (get-in next-ui-state [:board :generation-count]) 1))
          (is (= (get-in next-ui-state [:mode]) :running))))

      (testing "should not go to next generation when mode is :stopped"
        (let [next-ui-state (core/update-ui-state (assoc ui-state :mode :stopped))]
          (is (= (get-in next-ui-state [:board :generation-count]) 0))
          (is (= (get-in next-ui-state [:mode]) :stopped))))

      (testing "should go to next generation when mode is :step and change mode to :stopped"
        (let [next-ui-state (core/update-ui-state (assoc ui-state :mode :step))]
          (is (= (get-in next-ui-state [:board :generation-count]) 1))
          (is (= (get-in next-ui-state [:mode]) :stopped))))

      (testing "should update :geometry :window-size"
        (is (= (get-in (core/update-ui-state ui-state) [:geometry :window-size]) [window-width window-height]))))))
