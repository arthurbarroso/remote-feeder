(ns feeder.time.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]))

(defn list-last-feed-times [connection]
  (sql/query connection ["SELECT * FROM feed_times ft ORDER BY ft.time DESC LIMIT 10"]))

(defn create-feed-time [connection time uid]
  ;; TODO: this doesnt return, which fucks the controller (see controller code)
  (jdbc/execute-one!
   connection
   ["INSERT INTO feed_times(uid, time, status) VALUES (?, ?, ?)"
    uid time "waiting"]
   {:return-keys true :builder-fn rs/as-unqualified-lower-maps}))

(defn get-feed-time [connection uid]
  (-> connection
      (sql/find-by-keys :feed_times {:uid (str uid)})
      first))

(defn set-status-to-done [connection uid]
  (jdbc/execute-one! connection
                     ["UPDATE feed_times SET status = 'done' WHERE uid = ?"
                      uid]))
                     ;; :feed_times {:status "done"} {:uid uid}))
