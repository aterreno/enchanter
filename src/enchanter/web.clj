(ns enchanter.web
  (:gen-class)
  (:use [compojure.core]
        [hiccup.core]
        [ring.middleware.keyword-params]
        [hiccup.form]
        [ring.adapter.jetty]
        [incanter core stats charts datasets])
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:import (java.io ByteArrayOutputStream
                    ByteArrayInputStream)))

(def data (to-matrix (get-dataset :us-arrests)))
(def assault (sel data :cols 2))
(def urban-pop (sel data :cols 3))

(def permuted-assault (sample-permutations 5000 assault))
(def permuted-urban-pop (sample-permutations 5000 urban-pop))

(def permuted-corrs (map correlation permuted-assault permuted-urban-pop))


(defn to-png [chart]
  (let [out-stream (ByteArrayOutputStream.)
        in-stream (do
                    (save chart out-stream)
                    (ByteArrayInputStream.
                     (.toByteArray out-stream)))]
    in-stream))

(defn whatever [request]
  {:status 200
   :body in-stream
   :headers {"Content-Type" "image/png"}}
  (to-png (histogram permuted-corrs)))

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
  (GET "/whatever" request []
       (whatever request))
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
