(ns conway-life.ui.core
  (:require [conway-life.logic.board :as board]
            [conway-life.logic.simulator :as simulator]
            [conway-life.ui.geometry :as geometry]
            [conway-life.ui.input-ui-state :as input-ui-state]
            [conway-life.ui.keyboard :as keyboard]
            [conway-life.ui.mouse :as mouse]
            [conway-life.ui.ui-state :as ui-state]
            [quil.core :as q]
            [quil.middleware :as m]))

(defn time-ms [] (System/currentTimeMillis))
(defn- setup-ui-state []
  (let [window-size [(q/width) (q/height)]
        cell-size 2
        fill-size (mapv #(quot % cell-size) window-size)
        fill-percentage 15
        geometry (geometry/make-geometry :window-size window-size
                                         :cell-size cell-size)]
    (merge (ui-state/make-ui-state fill-size fill-percentage geometry :mode :running)
           (input-ui-state/make-input-ui-state :time-ms (time-ms)
                                               :single-clicked mouse/single-clicked
                                               :double-clicked mouse/double-clicked))))
(defn update-ui-state [ui-state]
  (letfn [(match-mode? [& modes] (contains? (set modes) (:mode ui-state)))]
    (cond-> ui-state
            (match-mode? :running :step) (update :board simulator/next-generation)
            (match-mode? :step) (assoc :mode :stopped)
            :always (assoc-in [:geometry :window-size] [(q/width) (q/height)])
            :always (input-ui-state/update-time-ms (time-ms)))))
(defn- draw-ui-state [ui-state]
  (let [board (:board ui-state)
        geometry (:geometry ui-state)
        [center-x center-y] (:center geometry)
        [window-width window-height] (:window-size geometry)
        header-height 30
        cell-size (:cell-size geometry)]
    (q/background 255)
    (q/stroke 0)
    (q/fill 0)
    (q/text (format "Generation %d, Number of cells %d, Center (%d, %d), Window size %dx%d, Cell size %dx%d, Mode %s"
                    (:generation-count board)
                    (board/number-of-on-cells board)
                    center-x center-y
                    window-width window-height
                    cell-size cell-size
                    (:mode ui-state))
            20 20)
    (q/line 0 header-height (q/width) header-height)
    (doseq [coords (board/all-on-cell-coords board)]
      (let [[window-x window-y] (geometry/to-window-coords coords geometry)]
        (if (> window-y header-height)
          (if (= cell-size 1)
            (q/point window-x window-y)
            (q/rect window-x window-y (dec cell-size) (dec cell-size))))))
    (if (and (:show-raster ui-state) (> cell-size 1))
      (do
        (q/stroke 182)
        (doseq [window-x (range (mod (quot window-width 2) cell-size) window-width cell-size)]
          (q/line window-x header-height window-x window-height))
        (doseq [window-y (range (mod (- window-height (quot window-height 2)) cell-size) window-height cell-size)]
          (let [window-y (max header-height window-y)]
            (q/line 0 window-y window-width window-y)))))))
(defn- mouse-clicked [ui-state event] (input-ui-state/add-click-event ui-state event))
(declare conway-life)
(defn start []
  (q/defsketch
    conway-life
    :title "Conway's Game of Life"
    :size [(/ (q/screen-width) 2) (/ (q/screen-height) 2)]
    :features [:resizable]
    :setup setup-ui-state
    :update update-ui-state
    :draw draw-ui-state
    :key-typed keyboard/key-typed
    :mouse-clicked mouse-clicked
    :middleware [m/fun-mode]))
(defn -main [& _] (start))
