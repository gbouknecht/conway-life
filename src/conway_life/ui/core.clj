(ns conway-life.ui.core
  (:require [quil.core :as q]
            [quil.middleware :as m]))

(defn- setup []
  {})

(defn- draw [_]
  (q/background 255)
  (q/fill 0)
  (q/text "Hello, World!" 20 20))

(declare conway-life)

(defn start []
  (q/defsketch
    conway-life
    :title "Conway's Game of Life"
    :size [(/ (q/screen-width) 2) (/ (q/screen-height) 2)]
    :features [:resizable]
    :setup setup
    :draw draw
    :middleware [m/fun-mode]))

(defn -main [& _] (start))
