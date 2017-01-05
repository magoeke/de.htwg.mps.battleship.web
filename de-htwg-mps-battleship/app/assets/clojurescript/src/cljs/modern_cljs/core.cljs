;; create the main project namespace
(ns modern-cljs.core
  (:require [reagent.core :as r]
            [ajax.core :refer [GET POST]]
            [clojure.string :as string]))

;; enable cljs to print to the JS console of the browser
(enable-console-print!)

(def number-of-players 2)
(def board-size 10)
(def empty-board (repeat (* board-size board-size) :empty))
(defn initial-boards [count] (map (fn [] empty-board) (range count)))

(def game-state (r/atom (initial-boards 1)))
(def game-state-buf (r/atom (initial-boards 1)))
(def ship-setting-start (r/atom -1))
(def settable-ships (r/atom '(5 4 4 3 3 3 2 2 2 2)))
(def websocket (atom nil))

(defn send! [map] (.send @websocket (.stringify js/JSON (clj->js map))))

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

(defn start-moving [id]
  (reset! ship-setting-start id)
  (reset! game-state-buf @game-state))

(defn moving [id]
  (if (= @ship-setting-start -1) (start-moving id))
  (reset! game-state (set-cell (cell-range id) :set)))

(defn cell-mouse-move [evt]
  (if (= (-> evt .-buttons) 1)
    (moving (-> evt .-target .-id))))

(defn general-mouse-up [evt]
  (let [id (-> evt .-target .-id)]
    (if (not= @ship-setting-start -1) (send! {:type "setShip" :start @ship-setting-start :end id}))
    (reset! ship-setting-start -1)))

(defn general-mouse-down [evt] (reset! ship-setting-start -1))
; (defn addEventListener [obj type function] (.addEventListener obj type function))
; (defn removeEventListener [obj type function] (.removeEventListener obj type function))

(defn register-general-listener []
  (.addEventListener (.querySelector js/document "body") "mouseup" general-mouse-up)
  (.addEventListener (.querySelector js/document "body") "mousedown" general-mouse-down))

(defn deregister-general-listener []
  (.removeEventListener (.querySelector js/document "body") "mouseup" general-mouse-up)
  (.removeEventListener (.querySelector js/document "body") "mousedown" general-mouse-down))


(defn cell-class [state]
  (cond
    (= state :empty) "empty"
    (= state :set) "set"
    (= state :hit) "hit"
    (= state :miss) "miss"))

(defn game-cell [id state listener] [:div {:class (str "game-cell" " " (cell-class state))
                                  :key (str "game-cell" id)
                                  :id id
                                  :on-mouse-move (get listener :on-mouse-move)
                                  :on-click (get listener :on-click)
                                  :draggable false}])

(defn output-board [board listener]
  (let [cells (list-with-index board)]
    [:div {:class "board"}
      (doall (map (fn [cell] (game-cell (get cell 1) (get cell 0) listener)) cells))]))

(defn cell-click [evt] (send! {:type "fire" :index (-> evt .-target .-id)}))

(defn split-screen []
  (let [rows (number-of-rows)]
    [:div {:style {:height "100vh"}}
      (for [board (list-with-index @game-state)]
        [:div {:key (str "screen" (get board 1))
               :style {:height (str (/ 100 rows) "vh")}
               :class (if (= 0 (mod (get board 1) 2)) "left-screen" "right-screen")}
          [output-board (get board 0) {:on-click cell-click}]])]))

(defn output-ships [ships]
  [:div {:class "ships"} (str "Ships: " (clojure.string/join " " (sort ships)))])

(defn setup-screen []
  [:div {:class "setup"}
    [output-ships @settable-ships]
    [output-board (nth @game-state 0) {:on-mouse-move cell-mouse-move}]])

(defn render [template handle-general-listener]
    (handle-general-listener)
    (r/render-component template (.getElementById js/document "content")))

(defn replace-board [boards index new-board]
  (map
    (fn [board] (if (not= (get board 1)) board new-board))
    (list-with-index boards)))

(defn cell-value [cell]
  (case cell
    "EMPTY" :empty
    "SHIP" :set
    "HIT" :hit
    "MISS" :miss))

(defn update-setup [json]
  (reset! settable-ships (into '() (get json :ships)))
  (reset! game-state (map
    (fn [board] (map cell-value board))
    (get json :board))))

(defn create-lightbox []
  [:div {:class "modal"}
    [:div {:class "modal-content"}
      [:div {:class "sk-folding-cube"}
        [:div {:class "sk-cube1 sk-cube"}]
        [:div {:class "sk-cube2 sk-cube"}]
        [:div {:class "sk-cube4 sk-cube"}]
        [:div {:class "sk-cube3 sk-cube"}]]]])

(defn end-dialog [data]
  [:div {:class "modal"
         :style (if (get data :won) {:background-color "cyan"})}
    [:div {:class "modal-content"}
      (if (get data :won) "You Won!" "Game Over")]])

(defn websocket-open [] (println "open")(render [setup-screen] register-general-listener))
(defn websocket-close [] (println "close"))
(defn websocket-error [e] (println (str "error: " e)))
(defn websocket-message [msg]
  (let [data (js->clj (.parse js/JSON (.-data msg)) :keywordize-keys true)]
    (case (get data :type)
      "update" (update-setup data)
      "waitForSecondPlayer" (render [create-lightbox] (fn [] (println "nothing")))
      "playersJoined" (render [split-screen] deregister-general-listener)
      "winner" (render [end-dialog data] (fn [] (println "nothing"))))))

(defn setup-websocket [functions]
  (reset! websocket (js/WebSocket. "ws://localhost:9000/ws"))
  (doall
    (map #(aset @websocket (first %) (second %))
      [["onopen" (get functions :onopen)]
       ["onclose" (get functions :onclose)]
       ["onerror" (get functions :onerror)]
       ["onmessage" (get functions :onmessage)]])))

(setup-websocket {:onmessage websocket-message
                  :onopen websocket-open})
