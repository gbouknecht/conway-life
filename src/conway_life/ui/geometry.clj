(ns conway-life.ui.geometry)

(defn make-geometry [& {:keys [center window-size cell-size]
                        :or   {center [0 0] cell-size 1}}]
  {:center      center
   :window-size window-size
   :cell-size   cell-size})
(defn to-window-coords [[x y] geometry] [x y]
  (let [[center-x center-y] (:center geometry)
        [width height] (:window-size geometry)
        size (:cell-size geometry)
        window-x (+ (- (quot width 2) (quot size 2)) (* (- x center-x) size))
        window-y (- height (quot height 2) (quot size 2) (* (- y center-y) size))]
    [window-x window-y]))
