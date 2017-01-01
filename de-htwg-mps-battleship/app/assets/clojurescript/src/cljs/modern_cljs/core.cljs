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
(def game-state-buf (r/atom (initial-game)))
(def ship-setting-start (r/atom -1))
(def settable-ships (r/atom '(5 4 4 3 3 3 2 2 2 2)))

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
  (if (= (mod start board-size) (mod current board-size)) board-size 1))

(defn range-valid? [range]
  (let [max (inc (apply max @settable-ships))
        min (dec (apply min @settable-ships))
        count (count range)]
    (if (< min count max) true false)))

(defn cell-range [current-id]
  "Returns range or an empty list."
  (let [start (int @ship-setting-start)
        current (int current-id)
        range (range
                (min start current)
                (inc (max start current))
                (cell-range-steps start current))]
    (if (range-valid? range) range '())))

(defn change-board [board board_idx indexes value]
    (if (not (empty? indexes))
      (change-board
        (change-cell board board_idx (first indexes) value)
        board_idx
        (rest indexes)
        value)
      board))

(defn set-cell [range state] (change-board @game-state-buf 0 range state))

(defn remove-one [input tmp element]
  (if (empty? input)
    tmp
    (let [first (first input)
          rest (rest input)]
      (if (= first element)
        (concat tmp rest)
        (remove-one rest (conj tmp first) element)))))

(defn save-ship [range]
  (if (not (empty? range)) (reset! game-state (set-cell range :set)))
  (reset! settable-ships (remove-one @settable-ships '() (count range)))
  (println @settable-ships))

(defn start-moving [id]
  (reset! ship-setting-start id)
  (reset! game-state-buf @game-state))

(defn stop-moving []
  (reset! game-state @game-state-buf)
  (reset! ship-setting-start -1))

(defn moving [id]
  (if (= @ship-setting-start -1) (start-moving id))
  (reset! game-state (set-cell (cell-range id) :set)))

(defn cell-mouse-move [evt]
  (if (= (-> evt .-buttons) 1)
    (moving (-> evt .-target .-id))))

(defn general-mouse-up [evt]
  (.stopImmediatePropagation evt)
  (let [id (-> evt .-target .-id)]
    (save-ship (cell-range id))
    (reset! ship-setting-start -1)))

(.addEventListener (.querySelector js/document "body") "mouseup" general-mouse-up)

(defn cell-class [state]
  (cond
    (= state :empty) "empty"
    (= state :set) "set"
    (= state :hit) "hit"))

(defn game-cell [id state] [:div {:class (str "game-cell" " " (cell-class state))
                                  :key (str "game-cell" id)
                                  :id id
                                  :on-mouse-move cell-mouse-move
                                  :draggable false}])

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

;; shows
(start)
