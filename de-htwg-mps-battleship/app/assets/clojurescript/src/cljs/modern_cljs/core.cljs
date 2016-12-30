;; create the main project namespace
(ns modern-cljs.core
  (:require [reagent.core :as r]))

;; enable cljs to print to the JS console of the browser
(enable-console-print!)

;; print to the console
(println "Hello, World!")

(def number-of-players 2)
(def board-size 10)

(defn board
  []
  [:div "Gameboard"])

(defn split-screen
  []
  (let [rows (.ceil js/Math (/ number-of-players 2))]
  [:div {:style {:height "100%"}}
    (repeat rows [:div {:style {:height (str (/ 100 rows) "%")}}
      [:div {:class "left-screen"} (board)]
      [:div {:class "right-screen"} (board)]])]))

(defn start []
  (r/render-component
   [split-screen]
   (.getElementById js/document "content")))


;; shows
(start)
