(ns feeder.server
  (:require [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]
            [aero.core :as aero]
            [clojure.java.io :as io]
            [feeder.database :as database]
            [feeder.router :as router]
            [feeder.mosquitto :as mosquitto]
            [feeder.users.handlers :as user-handlers]
            [feeder.migrations :as migrations]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn app [environment]
  (router/routes environment))

(defmethod ig/prep-key :server/jetty
  [_ config]
  (merge config {:port 4000}))

(defmethod ig/init-key :server/jetty
  [_ {:keys [handler port]}]
  (timbre/info (str "\nserver running on port: " port))
  (jetty/run-jetty handler {:port port :join? false}))

(defmethod ig/init-key :feed/app
  [_ config]
  (timbre/info "\nstarting application")
  (app config))

(defmethod ig/init-key :db/postgres
  [_ config]
  (timbre/info "\nconfigured db")
  (let [database-connection (database/create-connection)]
    (migrations/migrate)
    (timbre/info "\nRan migrations")
    (try
      (user-handlers/create-user! {:username "arthur" :password "hotel2202"}
                                  database-connection)
      (catch Exception _e
        (timbre/error "would throw")))

    (timbre/info "\nCreated user")
    database-connection))

(defmethod ig/init-key :mqtt/client
  [_ config]
  (timbre/info "\nconfiguring mqtt client")
  (let [mqtt-conn (mosquitto/create-connection config)]
    mqtt-conn))

(defmethod ig/init-key :feed/consumers
  [_ conn]
  (timbre/info "\nconfiguring mqtt consumer")
  (try
    (let [mqtt-conn (mosquitto/subscribe-to-client-answer conn)]
      (timbre/info "\nmqttt-conn")
      (timbre/info mqtt-conn)
      mqtt-conn)
    (catch Exception e
      (timbre/info "\nmosqitto-con exception")
      (timbre/error e)
      (timbre/info {:consumer-exception e}))))

(defmethod ig/halt-key! :mqtt/client
  [_ mqtt-conn]
  (mosquitto/close-connection mqtt-conn))

(defmethod ig/halt-key! :server/jetty
  [_ jetty]
  (.stop jetty))

(defn -main []
  (let [env-vars (aero/read-config (io/resource "config.edn"))
        config-map
        {:server/jetty {:handler (ig/ref :feed/app)
                        :port (:port env-vars)}
         :feed/app {:database (ig/ref :db/postgres)
                    :auth {:jwt-secret (:jwt_secret env-vars)}
                    :mqtt (ig/ref :mqtt/client)}
         :db/postgres {:host (:database_host env-vars)
                       :port (:database_port env-vars)
                       :user (:database_user env-vars)
                       :backend (:database_backend env-vars)
                       :id (:database_id env-vars)
                       :password (:database_password env-vars)
                       :dbname (:database_name env-vars)}
         :mqtt/client {:host (:mosquitto_host env-vars)
                       :user (:mosquitto_user env-vars)
                       :pass (:mosquitto_pass env-vars)}
         :feed/consumers {:conn (ig/ref :mqtt/client)
                          :database (ig/ref :db/postgres)}}]
    (timbre/info "\nCONFIG_MAP:\n")
    (timbre/info {:config-map config-map})
    (-> config-map ig/prep ig/init)))
