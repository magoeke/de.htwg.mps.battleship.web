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

(defn board
  [state]
  (println state)
  [:div
    (for [row state]
      [:div {:class "game-row"}
        (for [cell row] (game-cell))])])

; ^{:key cell} (game-cell)

(defn split-screen
  []
  (let [rows (.ceil js/Math (/ number-of-players 2))]
  [:div {:style {:height "100%"}}
    (repeat rows [:div {:style {:height (str (/ 100 rows) "%")}}
      [:div {:class "left-screen"} (board empty-board)]
      [:div {:class "right-screen"} (board empty-board)]])]))

(defn start []
  (r/render-component
   [split-screen]
   (.getElementById js/document "content")))


;; shows
(start)
