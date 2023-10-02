(ns feeder.view-template
  (:require [hiccup.page :as p]))

(def font-url
  "https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;700&family=Roboto:wght@300;400;500;700;900&display=swap")

(defn base-template [{:keys [body title]}]
  [:html
   [:head
    [:title title]
    (p/include-css "/assets/stylesheet.css")
    (p/include-css font-url)]
   [:body
    [:div {:class "app"}
     body]]])
