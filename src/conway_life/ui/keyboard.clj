(ns conway-life.ui.keyboard
  (:require [conway-life.ui.actions :as actions]))

(defn key-pressed [ui-state event]
  (let [match-mode? (fn [mode] (= (:mode ui-state) mode))
        match-keys? (fn [& keys] (contains? (set keys) (:key event)))
        key-pressed
        (fn [ui-state]
          (cond-> ui-state
                  (match-keys? :-) (actions/dispatch :zoom-out)
                  (match-keys? := :+) (actions/dispatch :zoom-in)
                  (match-keys? :0) (actions/dispatch :set-cell-size 1)
                  (match-keys? :r) (actions/dispatch :show-raster)
                  (match-keys? :s) (actions/dispatch :start-stop)
                  (match-keys? :n) (actions/dispatch :step)
                  (match-keys? :C) (actions/dispatch :clear)
                  (match-keys? :c) (actions/dispatch :move-to-origin)))
        key-pressed-in-running-mode
        (fn [ui-state]
          (cond-> ui-state
                  (match-keys? :left) (actions/dispatch :move-board-left)
                  (match-keys? :right) (actions/dispatch :move-board-right)
                  (match-keys? :up) (actions/dispatch :move-board-up)
                  (match-keys? :down) (actions/dispatch :move-board-down)))
        key-pressed-in-stopped-mode
        (fn [ui-state]
          (cond-> ui-state
                  (match-keys? :left) (actions/dispatch :move-cursor-left)
                  (match-keys? :right) (actions/dispatch :move-cursor-right)
                  (match-keys? :up) (actions/dispatch :move-cursor-up)
                  (match-keys? :down) (actions/dispatch :move-cursor-down)
                  (match-keys? :space) (actions/dispatch :toggle-cell-state-at-cursor)
                  (match-keys? :R) (actions/dispatch :fill-board-randomly)
                  (match-keys? :u) (actions/dispatch :undo)
                  (match-keys? :U) (actions/dispatch :redo)))]
    (cond-> ui-state
            (match-mode? :running) (key-pressed-in-running-mode)
            (match-mode? :stopped) (key-pressed-in-stopped-mode)
            :always (key-pressed))))
