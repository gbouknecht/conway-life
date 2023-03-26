(ns conway-life.ui.geometry)

(defn make-geometry [& {:keys [center cursor window-size cell-size]
                        :or   {center    [0 0]
                               cursor    [0 0]
                               cell-size 1}}]
  {:center      center
   :cursor      cursor
   :window-size window-size
   :cell-size   cell-size})
(defn to-window-coords [[x y] geometry]
  (let [[center-x center-y] (:center geometry)
        [width height] (:window-size geometry)
        size (:cell-size geometry)
        window-x (+ (quot width 2) (* (- x center-x) size))
        window-y (- height (quot height 2) (* (- y center-y) size) (dec size))]
    [window-x window-y]))
(defn to-coords [[window-x window-y] geometry]
  (let [[center-x center-y] (:center geometry)
        [width height] (:window-size geometry)
        size (:cell-size geometry)
        x (+ center-x (long (Math/floor (/ (- window-x (quot width 2)) size))))
        y (+ center-y (long (Math/floor (/ (- (quot height 2) window-y) size))))]
    [x y]))
