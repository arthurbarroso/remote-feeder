(ns feeder.migrations
  (:require [migratus.core :as migratus]
            [next.jdbc :as jdbc]))

(def config {:store :database
             :migration-dir "migrations/"
             :init-script "init.sql"
             :init-in-transaction? false
             :migration-table-name "migrations"
             :db {:dbtype "sqlite"
                  :dbname "teste.db"}})

(defn migrate []
  (migratus/migrate config))

(comment
  (migratus/init config)

  (migratus/rollback config)
  (migratus/migrate config)

  (migratus/create config "create-user")
  (migratus/create config "create-feed-times"))
