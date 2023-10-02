(ns feeder.time.routes
  (:require [feeder.time.controllers :as co]
            [feeder.middlewares :as mi]))

(defn routes [environment]
  ["/"
   ["" {:middleware [[mi/jws-middleware environment]]
        :get {:handler (co/render-time-controller! environment)}
        :post {:handler (co/time-controller-ssr-v2! environment)
               :parameters {:form :any}}}]
   ["/success" {:middleware [[mi/jws-middleware environment]]
                :get {:handler (co/render-time-controller! environment)}
                :post {:handler (co/time-controller-ssr-v2! environment)
                       :parameters {:form :any}}}]])
