(ns feeder.database
  (:require [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs]))

(defn create-connection []
  (jdbc/with-options {:dbtype "sqlite" :dbname "teste.db"} {:builder-fn rs/as-unqualified-maps}))
