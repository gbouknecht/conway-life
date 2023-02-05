(ns conway-life.ui.input-ui-state-test
  (:require [clojure.test :refer :all]
            [conway-life.ui.input-ui-state :as input-ui-state]))

(defn- single-clicked [ui-state event]
  (update ui-state :single-clicked-calls (fnil conj []) event))

(defn- double-clicked [ui-state event]
  (update ui-state :double-clicked-calls (fnil conj []) event))

(deftest about-single-clicking

  (testing "should recognize single click event"
    (let [event {:x 200 :y 300}
          ui-state-1000 (-> (input-ui-state/make-input-ui-state :time-ms 1000
                                                                :single-clicked single-clicked)
                            (input-ui-state/add-click-event event))
          ui-state-1499 (-> ui-state-1000 (input-ui-state/update-time-ms 1499))
          ui-state-1500 (-> ui-state-1000 (input-ui-state/update-time-ms 1500))
          ui-state-1501 (-> ui-state-1000 (input-ui-state/update-time-ms 1501))]
      (is (nil? (:single-clicked-calls ui-state-1000)))
      (is (nil? (:single-clicked-calls ui-state-1499)))
      (is (nil? (:single-clicked-calls ui-state-1500)))
      (is (= (:single-clicked-calls ui-state-1501) [event]))))

  (testing "should recognize two single click events right after each other"
    (let [event1 {:x 200 :y 300}
          event2 {:x 400 :y 500}
          ui-state (-> (input-ui-state/make-input-ui-state :time-ms 1000
                                                           :single-clicked single-clicked)
                       (input-ui-state/add-click-event event1)
                       (input-ui-state/update-time-ms 1501) (input-ui-state/add-click-event event2)
                       (input-ui-state/update-time-ms 2002))]
      (is (= (:single-clicked-calls ui-state) [event1 event2]))))

  (testing "should recognize single click event only once"
    (let [event {:x 200 :y 300}
          ui-state (-> (input-ui-state/make-input-ui-state :time-ms 1000
                                                           :single-clicked single-clicked)
                       (input-ui-state/add-click-event event)
                       (input-ui-state/update-time-ms 2002))]
      (is (= (:single-clicked-calls ui-state) [event]))))

  (testing "double click event should not be recognized as single click event"
    (let [event {:x 200 :y 300}
          ui-state (-> (input-ui-state/make-input-ui-state :time-ms 1000
                                                           :single-clicked single-clicked)
                       (input-ui-state/add-click-event event)
                       (input-ui-state/update-time-ms 1100) (input-ui-state/add-click-event event)
                       (input-ui-state/update-time-ms 1501))]
      (is (nil? (:single-clicked-calls ui-state))))))

(deftest about-double-clicking

  (testing "should recognize double click event"
    (let [event {:x 200 :y 300}
          ui-state-1000 (-> (input-ui-state/make-input-ui-state :time-ms 1000
                                                                :double-clicked double-clicked)
                            (input-ui-state/add-click-event event))
          ui-state-1001 (-> ui-state-1000 (input-ui-state/update-time-ms 1001) (input-ui-state/add-click-event event))
          ui-state-1499 (-> ui-state-1000 (input-ui-state/update-time-ms 1499) (input-ui-state/add-click-event event))
          ui-state-1500 (-> ui-state-1000 (input-ui-state/update-time-ms 1500) (input-ui-state/add-click-event event))
          ui-state-1501 (-> ui-state-1000 (input-ui-state/update-time-ms 1501) (input-ui-state/add-click-event event))]
      (is (nil? (:double-clicked-calls ui-state-1000)))
      (is (= (:double-clicked-calls ui-state-1001) [event]))
      (is (= (:double-clicked-calls ui-state-1499) [event]))
      (is (= (:double-clicked-calls ui-state-1500) [event]))
      (is (nil? (:double-clicked-calls ui-state-1501)))))

  (testing "should recognize two double click events right after each other"
    (let [event1 {:x 200 :y 300}
          event2 {:x 400 :y 500}
          ui-state-1 (-> (input-ui-state/make-input-ui-state :time-ms 1000
                                                             :double-clicked double-clicked)
                         (input-ui-state/add-click-event event1)
                         (input-ui-state/update-time-ms 1100) (input-ui-state/add-click-event event1)
                         (input-ui-state/update-time-ms 1200) (input-ui-state/add-click-event event2)
                         (input-ui-state/update-time-ms 1300) (input-ui-state/add-click-event event2))]
      (is (= (:double-clicked-calls ui-state-1) [event1 event2]))))

  (testing "should recognize double click event only once"
    (let [event {:x 200 :y 300}
          ui-state (-> (input-ui-state/make-input-ui-state :time-ms 1000
                                                           :double-clicked double-clicked)
                       (input-ui-state/add-click-event event)
                       (input-ui-state/update-time-ms 1100) (input-ui-state/add-click-event event)
                       (input-ui-state/update-time-ms 1200))]
      (is (= (:double-clicked-calls ui-state) [event])))))
