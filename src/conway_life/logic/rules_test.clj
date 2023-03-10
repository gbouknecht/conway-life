(ns conway-life.logic.rules-test
  (:require [clojure.test :refer :all]
            [conway-life.logic.rules :as rules]
            [conway-life.logic.common :refer [off on]]))

(deftest about-rules

  (letfn [(check [number-of-on-neighbors & {:keys [current-state next-state]}]
            (is (= (rules/next-state current-state number-of-on-neighbors) next-state)
                (str "number-of-on-neighbors=" number-of-on-neighbors)))]

    (testing "'off' cell"
      (are [number-of-on-neighbors next-state] (check number-of-on-neighbors :current-state off :next-state next-state)
                                               0 off
                                               1 off
                                               2 off
                                               3 on
                                               4 off
                                               5 off
                                               6 off
                                               7 off
                                               8 off))

    (testing "'on' cell"
      (are [number-of-on-neighbors next-state] (check number-of-on-neighbors :current-state on :next-state next-state)
                                               0 off
                                               1 off
                                               2 on
                                               3 on
                                               4 off
                                               5 off
                                               6 off
                                               7 off
                                               8 off))))
