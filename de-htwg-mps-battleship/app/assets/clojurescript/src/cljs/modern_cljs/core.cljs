;; create the main project namespace
(ns modern-cljs.core
  (:require [reagent.core :as r]))

;; enable cljs to print to the JS console of the browser
(enable-console-print!)

;; print to the console
(println "Hello, World!")

(def number-of-players 2)
(def board-size 10)
(def empty-board
  (vec (repeat board-size
    (vec (repeat board-size :empty)))))

(defn game-cell
  []
  [:div {:class "game-cell"
         :on-click #(println "clicked")}])

(defn calculate-cell-id [multiplier index] (+ index (* (+ multiplier 1) index)))
(defn calculate-row-id [multiplier] (- (- 1 board-size) multiplier))

(defn draw-board-cell
  [cells result multiplier]
  (if (= (first cells) nil)
    [:div {:class "game-row" :key (str "row" (calculate-row-id multiplier))} result]
    (draw-board-cell
      (rest cells)
      (conj result
        [:div {:class "game-cell"
               :key (str "cell" (calculate-cell-id multiplier (count cells)))}])
      multiplier)))

(defn draw-board-row
  [rows result]
  (if (= (first rows) nil)
    [:div result]
    (draw-board-row (rest rows) (conj result (draw-board-cell (first rows) (list) (count result))))
))

(defn split-screen
  []
  (let [rows (.ceil js/Math (/ number-of-players 2))]
    [:div {:style {:height "100%"}}
      (for [index (range number-of-players)]
        [:div {:key (str "screen" index)
               :style {:height (str (/ 100 rows) "%")}
               :class (if (= 0 (mod index 2)) "left-screen" "right-screen")}
          [draw-board-row empty-board (list)]])]))

(defn start []
  (r/render-component
   [split-screen]
   (.getElementById js/document "content")))


;; shows
(start)
