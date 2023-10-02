(ns feeder.time.controllers
  (:require [feeder.time.views :as views]
            [ring.util.response :as rr]
            [feeder.time.handlers :as h]))

(defn render-time-controller! [environment]
  (fn [_request]
    (let [feed-times (h/get-last-feed-times (:database environment))
          view (views/feed-v2 feed-times)]
      (rr/response view))))

(defn time-controller-ssr-v2! [environment]
  (fn [_request]
    (try
      (if-let [feed-time (h/create-feed-time! (:database environment))]
        (do (println (str "\n feed-time: " feed-time))
            (h/publish-feed-message! (:mqtt environment) (str (:uid feed-time)))
            (rr/redirect "/"))
        (rr/bad-request {:error "Something went wrong"}))
      (catch Exception e
        (clojure.pprint/pprint {:Exception e})
        (rr/bad-request {:error "Something went wrong"})))))
