(ns user
  (:require [feeder.server]
            [integrant.core :as ig]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as state]
            [aero.core :as aero]
            [clojure.java.io :as io]
            [tick.core :as t]))

(def env-vars
  (try (aero/read-config (io/resource "config.edn")
                         {:profile :dev})
       (catch Exception e
         (clojure.pprint/pprint e))))

(def config-map
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
                    :database (ig/ref :db/postgres)}})

(ig-repl/set-prep!
 (fn [] config-map))

(def db (-> state/system :db/postgres))

(def go ig-repl/go)
(def stop ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(comment
  (reset-all)
  (stop)
  (go)
  (clojure.pprint/pprint env-vars)
  (add-tap (bound-fn* clojure.pprint/pprint))

  (clojure.pprint/pprint {:db db})
  (require '[feeder.time.db :as dash-db])
  (dash-db/create-feed-time
   db
   (t/offset-date-time (t/zoned-date-time (t/date-time)))
   (java.util.UUID/randomUUID))
  (require '[feeder.users.handlers :as users-handlers])
  (users-handlers/create-user! {:password "hotel2202"
                                :username "arthur"} db)

  ;; (dash-h/get-last-feed-times db)

  ;; (->> (dash-db/list-last-feed-times db)
  ;;      first
  ;;      :time
  ;;      t/zoned-date-time
  ;;      t/minute)

  (t/offset-date-time (t/zoned-date-time (t/date-time))))
