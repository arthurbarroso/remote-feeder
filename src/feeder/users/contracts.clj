(ns feeder.users.contracts)

(def UserData
  [:map
   [:username string?]])

(def UserOut
  [:maybe
   [:map
    [:username string?]
    [:password string?]]])

(def UserLoginInput
  [:map
   [:username string?]
   [:password string?]])

(def TokenOutResponse
  [:map [:token string?]])
