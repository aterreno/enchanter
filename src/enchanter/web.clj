(ns enchanter.web
  (:gen-class)
  (:use [compojure.core]
        [hiccup.core]
        [ring.middleware.keyword-params]
        [hiccup.form]
        [ring.adapter.jetty]
        [incanter core stats charts])
  (:require [compojure.route :as route]
            [compojure.handler :as handler])
  (:import (java.io ByteArrayOutputStream
                    ByteArrayInputStream)))

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

(def sample-form
  (html-doc "sample-normal histogram"
            (form-to [:get "/sample-normal"]
                     "sample size: " (text-field {:size 4} :size)
                     "mean: " (text-field {:size 4} :mean)
                     "sd: " (text-field {:size 4} :sd)
                     (submit-button "view"))))

(defn gen-samp-hist-png
  [request size-str mean-str sd-str]
  (let [size (if (nil? size-str)
               1000
               (Integer/parseInt size-str))
        m (if (nil? mean-str)
            0
            (Double/parseDouble mean-str))
        s (if (nil? sd-str)
            1
            (Double/parseDouble sd-str))
        samp (sample-normal size
                            :mean m
                            :sd s)
        chart (histogram
               samp
               :title "Normal Sample"
               :x-label (str "sample-size = " size
                             ", mean = " m
                             ", sd = " s))
        out-stream (ByteArrayOutputStream.)
        in-stream (do
                    (save chart out-stream)
                    (ByteArrayInputStream.
                     (.toByteArray out-stream)))
        ]
    {:status 200
     :body in-stream
     :headers {"Content-Type" "image/png"}}))

(defroutes enchanter-routes
  (GET "/sample-normal" request []
       (gen-samp-hist-png request
                          (-> request :params :size)
                          (-> request :params :mean)
                          (-> request :params :sd))))

(def app
  (handler/site enchanter-routes))

(defn -main [& args]
  (run-jetty enchanter-routes {:port 8080}))
