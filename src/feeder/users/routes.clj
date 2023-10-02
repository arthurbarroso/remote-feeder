(ns feeder.users.routes
  (:require [feeder.users.controllers :as co]
            [feeder.users.contracts :as c]))

(defn routes [environment]
  ["/login"
   ;; ["/login"
   ;;  {:post {:handler (co/login-controller! environment)
   ;;          :parameters {:body c/UserLoginInput}}}]
   [""
    {:get {:handler (co/render-login-controller! environment)}
     :post {:handler (co/login-controller-ssr! environment)
            :parameters {:form c/UserLoginInput}}}]])
