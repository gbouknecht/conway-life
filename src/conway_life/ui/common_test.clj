(ns conway-life.ui.common-test
  (:require [clojure.test :refer :all]
            [conway-life.ui.common :refer [time-ns timed-call]]))

(deftest about-timed-call

  (testing "should call give function and return [result duration-ns]"
    (let [time-ns-results (atom (list 100 150 300 475))]
      (with-redefs [time-ns (fn [] (peek (first (swap-vals! time-ns-results pop))))]
        (is (= (timed-call #(+ 1 2)) [3 50]))
        (is (= (timed-call #(+ 3 4)) [7 175]))))))
