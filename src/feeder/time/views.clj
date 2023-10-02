(ns feeder.time.views
  (:require [hiccup.core :as h]
            [hiccup.form :as f]
            [feeder.view-template :as t]))

(defn status-element [status]
  (if (= "done" status)
    [:td {:class "status status_done"}
     status]
    [:td {:class "status status_not_done"}
     status]))

(defn- feed-view-v2 [feed-times]
  [:div {:class "feed"}
   [:h1 "Events (feed)"]
   [:table
    [:thead
     [:tr
      [:th "Time"]
      [:th "Status"]]]
    [:tbody
     (for [feed-time feed-times]
       [:tr
        [:td {:class "time"} (str (:time feed-time))]
        (status-element (:status feed-time))])]]
   (f/form-to
    [:post "/"]
                 ;;(anti-forgery/anti-forgery-field)
    (f/submit-button "Post event (feed)"))])

(defn feed-v2 [feed-times]
  (h/html {:mode :html}
          (t/base-template {:title "Feed"
                            :body (feed-view-v2 feed-times)})))
