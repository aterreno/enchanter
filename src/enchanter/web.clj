(ns enchanter.web
  (:gen-class)
  (:use [compojure.core]
        [hiccup.core]
        [hiccup.form]
        [ring.adapter.jetty]
        [incanter core stats charts])
  (:require [compojure.route :as route])
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
        header {:status 200
                :headers {"Content-Type" "image/png"}}]
    ))

(comment
  (compojure.http.response/update-response request
                                           header
                                           in-stream)

  (GET "/sample-normal"
       (gen-samp-hist-png request
                          (params :size)
                          (params :mean)
                          (params :sd)))
  )

(defroutes enchanter-routes
  (GET "/" [] "<h1>Hello World</h1>")
  (route/not-found "<h1>Page not found</h1>"))

(defn -main [& args]
  (run-jetty enchanter-routes {:port 8080}))
