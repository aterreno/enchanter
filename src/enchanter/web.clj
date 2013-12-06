(ns enchanter.web
  (:gen-class)
  (:use [compojure.core]
        [hiccup.core]
        [ring.middleware.keyword-params]
        [hiccup.form]
        [ring.adapter.jetty]
        [incanter core stats charts datasets io])
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:import (java.io ByteArrayOutputStream
                    ByteArrayInputStream)))

(def data (read-dataset "data/listings.csv" :header true))

(def sqfeet (sel data :cols :SquareFeet))

(def bathrooms (sel data :cols :TotalBathrooms))

(def corr (correlation (to-matrix data)))

(def columnnames (:column-names data))

(def price-column (sel corr :cols 14))

(defn to-png [chart]
  (let [out-stream (ByteArrayOutputStream.)
        in-stream (do
                    (save chart out-stream)
                    (ByteArrayInputStream.
                     (.toByteArray out-stream)))]
    in-stream))

(defn interesting [request]
  {:status 200
   :body in-stream
   :headers {"Content-Type" "image/png"}}
  (to-png (bar-chart columnnames price-column :legend true :vertical false)))

(defn html-doc
  [title & body]
  (html
   [:html
    [:head
     [:title title]]
    [:body
     [:div
      [:h2
       [:a {:href "/"}
        "Generate a normal sample"]]]
     body]]))

(defn sample-form []
  {:status 200
     :body (html-doc "sample-normal histogram"
            (form-to [:get "/sample-normal"]
                     "sample size: " (text-field {:size 4} :size)
                     "mean: " (text-field {:size 4} :mean)
                     "sd: " (text-field {:size 4} :sd)
                     (submit-button "view")))})

(defn gen-samp-hist-png
  [request size-str mean-str sd-str]
  (let [size (Integer/parseInt size-str)
        m (Double/parseDouble mean-str)
        s (Double/parseDouble sd-str)
        samp (sample-normal size :mean m :sd s)
        chart (histogram
               samp
               :title "Normal Sample"
               :x-label (str "sample-size = " size
                             ", mean = " m
                             ", sd = " s))]
    {:status 200
     :body (to-png chart)
     :headers {"Content-Type" "image/png"}}))

(defroutes enchanter-routes
  (GET "/" []
    (sample-form))
  (GET "/interesting" request []
       (interesting request))
  (GET "/sample-normal" request []
       (gen-samp-hist-png request
                          (get-in request [:params :size] "1000")
                          (get-in request [:params :mean] "0")
                          (get-in request [:params :sd] "1"))))
(def app
  (handler/site enchanter-routes))

(defn -main [& [port]]
  (let [port (Integer. (or port (System/getenv "PORT")))]
    (run-jetty #'app {:port port})))
