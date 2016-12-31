;; create the main project namespace
(ns modern-cljs.core
  (:require [reagent.core :as r]))

;; enable cljs to print to the JS console of the browser
(enable-console-print!)

(def number-of-players 2)
(def board-size 10)
(def empty-board (repeat (* board-size board-size) :empty))

(defn board-with-index [board] (map (fn [a b] (vector a b)) board (range (count board))))
(defn number-of-rows [] (.ceil js/Math (/ number-of-players 2)))
(defn game-cell [] [:div {:class "game-cell" :on-click #(println "clicked")}])

(defn output-game []
  (let [cells (board-with-index empty-board)]
    [:div {:class "board"}
      (doall (map (fn [cell] ^{:key (str "cell" (get cell 1))}[game-cell]) cells))]))

(defn split-screen
  []
  (let [rows (number-of-rows)]
    [:div {:style {:height "100vh"}}
      (for [index (range number-of-players)]
        [:div {:key (str "screen" index)
               :style {:height (str (/ 100 rows) "vh")}
               :class (if (= 0 (mod index 2)) "left-screen" "right-screen")}
          [output-game]
          ])]))

(defn start []
  (r/render-component
   [split-screen]
   (.getElementById js/document "content")))

;; shows
(start)
