(ns feeder.mosquitto
  (:require [clojurewerkz.machine-head.client :as mh]
            [feeder.time.db :as feed-time-db]))

(def ^:const topic "feeder/feed")

(defn create-connection [{:keys [host user pass]}]
  (mh/connect host {:opts {:username user :password pass :keep-alive-interval 65535}}))

(defn close-connection [conn]
  (mh/disconnect conn))

(def ^:const client-answer-topic "feeder/client-fed")

  ;; [client topics-and-qos handler-fn])
(defn subscribe-to-client-answer [{:keys [conn database]}]
  (mh/subscribe conn
                {client-answer-topic 1}
                (fn [^String topic _ ^bytes payload]
                  (println (str *ns* " received message: " (String. payload)))
                  (println (str  "....topic: " topic))
                  (try (let [stringified-payload (String. payload "UTF-8")
                             payload-uuid (java.util.UUID/fromString (subs stringified-payload 0 (- (count stringified-payload) 1)))]
                         (when-let [feed-time (feed-time-db/get-feed-time database payload-uuid)]
                           (when (= "waiting" (:status feed-time))
                             (feed-time-db/set-status-to-done database (:uid feed-time)))))
                       (catch Exception e
                         (println (str "Exception.....\n " e)))))))
