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
(def ship-setting (r/atom false))
(def ship-setting-start (r/atom -1))

(defn list-with-index [board] (map (fn [a b] (vector a b)) board (range (count board))))
(defn number-of-rows [] (.ceil js/Math (/ number-of-players 2)))

(defn change-cell [game-state player index value]
  (map (fn [board]
    (if (not= (get board 1) player)
      (get board 0)
      (map (fn [cell]
        (if (not= (get cell 1) index)
        (get cell 0)
        value)) (list-with-index (get board 0))))) (list-with-index game-state)))

(defn cell-range-steps [start current]
  (if (= (mod start board-size) (mod current board-size)) 10 1))

(defn cell-range [current-id]
  "Returns range or an empty list."
  (let [start (int @ship-setting-start)
        current (int current-id)
        range (range
                (min start current)
                (inc (max start current))
                (cell-range-steps start current))]
    (if (< (count range) (inc board-size)) range '())))

(defn change-board [board board_idx indexes value]
    (if (not (empty? indexes))
      (change-board
        (change-cell board board_idx (first indexes) value)
        board_idx
        (rest indexes)
        value)
      board))

(defn cell-mouse-move [evt]
  (if (not= @ship-setting-start -1)
    (reset! game-state (change-board @game-state 0 (cell-range (-> evt .-target .-id)) :set))))

(defn cell-mouse-down [evt]
    (reset! ship-setting-start (-> evt .-target .-id)))

(defn cell-mouse-up [evt]
    ; send coordinates to server
    (reset! ship-setting-start -1))

(defn cell-class [state]
  (cond
    (= state :empty) "empty"
    (= state :set) "set"
    (= state :hit) "hit"))

(defn game-cell [id state] [:div {:class (str "game-cell" " " (cell-class state))
                                  :key (str "game-cell" id)
                                  :id id
                                  :on-mouse-move cell-mouse-move
                                  :on-mouse-down cell-mouse-down
                                  :on-mouse-up cell-mouse-up}])

(defn output-game [board]
  (let [cells (list-with-index board)]
    [:div {:class "board"}
      (doall (map (fn [cell] (game-cell (get cell 1) (get cell 0))) cells))]))

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

(reset! game-state (change-cell @game-state 1 0 :hit))

;; shows
(start)
