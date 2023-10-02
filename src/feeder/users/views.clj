(ns feeder.users.views
  (:require [hiccup.core :as h]
            [hiccup.form :as f]
            [feeder.view-template :as t]))

(defn- login-view []
  [:div {:class "login"}
   [:h1 "Login"]
   (f/form-to
    [:post "/login"]
              ;;(anti-forgery/anti-forgery-field)
    (f/label "username" "Username")
    (f/text-field "username" "username")
    (f/label "password" "Password")
    (f/password-field "password")
    (f/submit-button "Login"))])

(defn login []
  (let [body (login-view)
        view (t/base-template {:title "Login"
                               :body body})]
    (h/html {:mode :html} view)))
