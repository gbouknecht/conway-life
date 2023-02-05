(ns conway-life.ui.input-ui-state)

(def ^:private max-double-click-delay-ms 500)

(defn make-input-ui-state [& {:keys [time-ms single-clicked double-clicked]}]
  {:time-ms            time-ms
   :timed-click-events []
   :single-clicked     single-clicked
   :double-clicked     double-clicked})
(defn- exceeds-double-click-delay? [m1 m2]
  (> (- (:time-ms m2) (:time-ms m1)) max-double-click-delay-ms))
(defn- click-count [ui-state]
  (let [[event1 event2] (:timed-click-events ui-state)]
    (cond
      (and event1 (not event2) (exceeds-double-click-delay? event1 ui-state)) 1
      (and event1 event2 (exceeds-double-click-delay? event1 event2)) 1
      (and event1 event2 (not (exceeds-double-click-delay? event1 event2))) 2
      :else 0)))
(defn- call [callback-key ui-state & args]
  (if-let [callback (ui-state callback-key)]
    (apply callback ui-state args)
    ui-state))
(defn- process-click-events [ui-state]
  (let [click-count (click-count ui-state)
        click-event (-> ui-state :timed-click-events first :event)
        ui-state (update ui-state :timed-click-events subvec click-count)]
    (case click-count
      0 ui-state
      1 (call :single-clicked ui-state click-event)
      2 (call :double-clicked ui-state click-event))))
(defn update-time-ms [ui-state time-ms]
  (-> ui-state
      (assoc :time-ms time-ms)
      (process-click-events)))
(defn add-click-event [input-ui-state event]
  (-> input-ui-state
      (update :timed-click-events conj {:time-ms (:time-ms input-ui-state) :event event})
      (process-click-events)))
