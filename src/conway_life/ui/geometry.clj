(ns conway-life.ui.geometry)

(defn make-geometry [& {:keys [center cursor window-size cell-size margin-top]
                        :or   {center     [0 0]
                               cursor     [0 0]
                               cell-size  1
                               margin-top 30}}]
  {:center      center
   :cursor      cursor
   :window-size window-size
   :cell-size   cell-size
   :margin-top  margin-top})
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
(defn adjust-center-to-make-cursor-visible [geometry]
  (let [[center-x center-y] (:center geometry)
        [cursor-x cursor-y] (:cursor geometry)
        [width height] (:window-size geometry)
        size (:cell-size geometry)
        number-of-visible-x-cells (quot (quot width 2) size)
        number-of-visible-pos-y-cells (quot (- (quot height 2) (:margin-top geometry)) size)
        number-of-visible-neg-y-cells (quot (quot height 2) size)
        dx (if (> cursor-x center-x)
             (max 0 (- (inc (- cursor-x center-x)) number-of-visible-x-cells))
             (min 0 (- number-of-visible-x-cells (- center-x cursor-x))))
        dy (if (> cursor-y center-y)
             (max 0 (- (inc (- cursor-y center-y)) number-of-visible-pos-y-cells))
             (min 0 (- number-of-visible-neg-y-cells (- center-y cursor-y))))]
    (update geometry :center (fn [[x y]] [(+ x dx) (+ y dy)]))))
