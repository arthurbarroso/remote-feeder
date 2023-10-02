(ns feeder.users.controllers
  (:require [feeder.users.handlers :as h]
            [feeder.users.views :as views]
            [ring.util.response :as rr]))

(defn login-controller! [environment]
  (fn [request]
    (let [database (:database environment)
          {:keys [username password]}
          (-> request :parameters :body)
          result (-> {:username username :password password}
                     (h/check-credentials! database)
                     (h/gen-token! environment))]
      (if (nil? (:token result))
        (rr/bad-request {:error "Something went wrong"})
        (let [res (rr/response {:token (:token result)})]
          (-> res
              (assoc :session {:something true})))))))

(defn login-controller-ssr! [environment]
  (fn [request]
    (let [database (:database environment)
          {:keys [username password]}
          (-> request :parameters :form)
          result (-> {:username username :password password}
                     (h/check-credentials! database)
                     (h/gen-token! environment))]
      (if (nil? (:token result))
        (rr/bad-request {:error "Something went wrong"})
        (let [response (rr/redirect "/")
              assoced (assoc response :session {:token (:token result)})]
          assoced)))))

(defn render-login-controller! [_database]
  (fn [_request]
    (let [view (views/login)]
      (rr/response view))))
