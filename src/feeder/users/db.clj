(ns feeder.users.db
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]))

(defn get-user [connection username]
  (-> connection
      (sql/find-by-keys :users {:username username})
      first))

(defn create-user [{:keys [username uid password]} connection]
  (sql/insert! connection :users {:username username
                                  :uid uid
                                  :password password}))

(comment
  (let [jdbc-url "jdbc:postgresql://localhost:5432/database?password=postgres&user=postgres"
        conn (jdbc/with-options jdbc-url {:builder-fn rs/as-unqualified-maps})]
    (clojure.pprint/pprint (get-user conn "arthur1"))
    ;; (clojure.pprint/pprint (create-user {:username "test"
    ;;                                      :uid (str (shared/generate-uuid!))
    ;;                                      :password "1234"}
    ;;                                     conn))
    (clojure.pprint/pprint (get-user conn "test"))))
