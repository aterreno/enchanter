(ns enchanter.core
  (:use [incanter core stats charts io]))

(def data (read-dataset "data/listings.csv" :header true))

(def sqfeet (sel data :cols :SquareFeet))

(def bathrooms (sel data :cols :TotalBathrooms))

(def corr (correlation (to-matrix data)))

(def columnnames (:column-names data))

(def interesting (sel corr :cols 14))

;(def the-matrix (matrix col-names interesting))
