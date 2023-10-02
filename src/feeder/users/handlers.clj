(ns feeder.users.handlers
  (:require [feeder.users.db :as db]
            [buddy.hashers :as bh]
            [buddy.sign.jwt :as jwt]))

(defn create-token!
  [env user-payload]
  (let [clean-user (dissoc user-payload :password :uid)]
    {:token (jwt/sign clean-user (get-in env [:auth :jwt-secret]))}))

(defn hash-password! [password-input]
  (bh/encrypt password-input))

(defn compare-passwords! [password-input user-password]
  (bh/check password-input user-password))

(defn check-user-existence! [user-input db]
  (db/get-user db (:username user-input)))

(defn create-user!
  [new-user db]
  (let [id (java.util.UUID/randomUUID)
        hashed-pass (hash-password! (:password new-user))]
    (-> {:username (:username new-user)
         :password hashed-pass
         :uid id}
        (db/create-user db)
        (dissoc :password :uid))))

(defn match-user-input-password! [existing-user user-input]
  {:matches?
   (if (nil? existing-user)
     false
     (compare-passwords! (:password user-input)
                         (:password existing-user)))
   :existing-user (dissoc existing-user :password :uid)})

(defn check-credentials!
  [user-input db]
  (-> user-input
      (check-user-existence! db)
      (match-user-input-password! user-input)))

(defn gen-token!
  [{:keys [matches? existing-user]} env]
  (if matches?
    (create-token! env existing-user)
    nil))

(comment
  (require '[next.jdbc :as jdbc])
  (require '[next.jdbc.result-set :as rs])
  (let [jdbc-url "jdbc:postgresql://localhost:5432/database?password=postgres&user=postgres"
        conn (jdbc/with-options jdbc-url {:builder-fn rs/as-unqualified-maps})]
    (= jdbc-url "jdbc:postgresql://localhost:5432/?password=postgres&user=postgres")
    (create-user! {:username "arthur" :password "arthur1"} conn)))
    ;; (check-credentials!
    ;;  {:username "xiclete1234" :password "arthur1"}
    ;;  conn)))
