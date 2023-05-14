(ns conway-life.ui.common-test
  (:require [clojure.test :refer :all]
            [conway-life.ui.common :refer [time-ms timed-call]]))

(deftest about-timed-call

  (testing "should call give function and return [result duration-ms]"
    (let [time-ms-results (atom (list 100 150 300 475))]
      (with-redefs [time-ms (fn [] (peek (first (swap-vals! time-ms-results pop))))]
        (is (= (timed-call #(+ 1 2)) [3 50]))
        (is (= (timed-call #(+ 3 4)) [7 175]))))))
