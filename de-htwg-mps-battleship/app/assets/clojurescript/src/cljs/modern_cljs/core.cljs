;; create the main project namespace
(ns modern-cljs.core
  (:require [reagent.core :as r]))

;; enable cljs to print to the JS console of the browser
(enable-console-print!)

(def number-of-players 2)
(def board-size 10)
(def empty-board (repeat (* board-size board-size) :empty))
(defn initial-game [] (map (fn [] empty-board) (range number-of-players)))

(def game-state (r/atom (initial-game)))

(defn list-with-index [board] (map (fn [a b] (vector a b)) board (range (count board))))
(defn number-of-rows [] (.ceil js/Math (/ number-of-players 2)))

(defn mousemove [evt]
    (println (.-target evt)))

(defn cell-class [state]
  (cond
    (= state :empty) "empty"
    (= state :set) "set"
    (= state :ht) "hit"))

(defn game-cell [state] [:div {:class (str "game-cell" " " (cell-class state))
                              :on-mouseMove mousemove}])

(defn output-game [board]
  (let [cells (list-with-index board)]
    [:div {:class "board"}
      (doall (map (fn [cell] ^{:key (str "cell" (get cell 1))} [game-cell (get cell 0)]) cells))]))

(defn split-screen
  []
  (let [rows (number-of-rows)]
    [:div {:style {:height "100vh"}}
      (for [board (list-with-index @game-state)]
        [:div {:key (str "screen" (get board 1))
               :style {:height (str (/ 100 rows) "vh")}
               :class (if (= 0 (mod (get board 1) 2)) "left-screen" "right-screen")}
          [output-game (get board 0)]])]))

(defn start [] (r/render-component [split-screen] (.getElementById js/document "content")))

;; shows
(start)
