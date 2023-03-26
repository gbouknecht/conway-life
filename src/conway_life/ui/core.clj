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
(defn- setup []
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
(defn- draw [ui-state]
  (let [board (:board ui-state)
        geometry (:geometry ui-state)
        [cursor-x cursor-y] (:cursor geometry)
        [center-x center-y] (:center geometry)
        [window-width window-height] (:window-size geometry)
        cell-size (:cell-size geometry)
        draw-board #(do (q/fill 0)
                        (doseq [coords (board/on-cells board)]
                          (let [[window-x window-y] (geometry/to-window-coords coords geometry)]
                            (if (= cell-size 1)
                              (q/point window-x window-y)
                              (q/rect window-x window-y (dec cell-size) (dec cell-size))))))
        draw-raster #(if (and (:show-raster ui-state) (> cell-size 1))
                       (do (q/stroke 182)
                           (doseq [window-x (range (mod (quot window-width 2) cell-size) window-width cell-size)]
                             (q/line window-x 0 window-x window-height))
                           (doseq [window-y (range (mod (- window-height (quot window-height 2)) cell-size) window-height cell-size)]
                             (q/line 0 window-y window-width window-y))))
        draw-cursor #(if (= (:mode ui-state) :stopped)
                       (let [[window-x window-y] (geometry/to-window-coords [cursor-x cursor-y] geometry)
                             weight (condp < cell-size 25 4 15 3 10 2 1)]
                         (q/stroke-weight weight)
                         (q/stroke 255 0 0)
                         (q/no-fill)
                         (q/rect window-x window-y (dec cell-size) (dec cell-size))))
        draw-info #(let [header-height 30]
                     (q/stroke 255)
                     (q/rect 0 0 window-width header-height)
                     (q/fill 0)
                     (q/text (format "Generation %d, Number of cells %d, Cursor (%d %d), Center (%d, %d), Window size %dx%d, Cell size %dx%d, Mode %s"
                                     (:generation-count board)
                                     (board/number-of-on-cells board)
                                     cursor-x cursor-y
                                     center-x center-y
                                     window-width window-height
                                     cell-size cell-size
                                     (:mode ui-state))
                             20 20)
                     (q/stroke 0)
                     (q/line 0 header-height window-width header-height))]
    (q/background 255)
    (doseq [draw-fn [draw-board draw-raster draw-cursor draw-info]]
      (q/push-style) (draw-fn) (q/pop-style))))
(defn- mouse-clicked [ui-state event] (input-ui-state/add-click-event ui-state event))
(declare conway-life)
(defn start []
  (q/defsketch
    conway-life
    :title "Conway's Game of Life"
    :size [(/ (q/screen-width) 2) (/ (q/screen-height) 2)]
    :features [:resizable]
    :setup setup
    :update update-ui-state
    :draw draw
    :key-pressed keyboard/key-pressed
    :mouse-clicked mouse-clicked
    :middleware [m/fun-mode]))
(defn -main [& _] (start))
