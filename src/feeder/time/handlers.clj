(ns feeder.time.handlers
  (:require [clojurewerkz.machine-head.client :as mh]
            [feeder.mosquitto :as mqtt]
            [feeder.time.db :as db]
            [tick.core :as t]))

(defn publish-feed-message!
  [conn uid]
  (println (str "\nuid: " uid))
  (mh/publish conn mqtt/topic uid))

(defn create-feed-time!
  [datasource]
  (let [uuid (java.util.UUID/randomUUID)]
    (db/create-feed-time datasource
                         (t/offset-date-time (t/zoned-date-time (t/date-time)))
                         uuid)
    {:uid uuid}))

(defn stringify-time [time]
  (str (t/month time) " "
       (t/day-of-month time) " "
       (t/year time)  " at " (t/hour time) ":" (t/minute time)))

(defn get-last-feed-times [datasource]
  (let [feed-times (db/list-last-feed-times datasource)]
    (->> feed-times
         (map #(update % :time t/zoned-date-time))
         (map #(update % :time stringify-time)))))
