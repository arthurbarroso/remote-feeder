(ns feeder.router
  (:require [reitit.ring.middleware.exception :as exception]
            [buddy.auth :refer [authenticated?]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth.backends :as backends]
            [buddy.sign.jwt :as jwt]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.cookies :as cookies]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as session-cookie]
            [ring.middleware.session.memory :as session-memory]
            [ring.middleware.params :as params]
            [reitit.coercion.malli :as coercion-malli]
            [reitit.dev.pretty :as pretty]
            [reitit.swagger :as swagger]
            [reitit.swagger-ui :as swagger-ui]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as coercion]
            [reitit.ring.middleware.muuntaja :as muuntaja]
            [reitit.ring.middleware.parameters :as parameters]
            [muuntaja.core :as m]
            [feeder.users.routes :as users]
            [feeder.time.routes :as time]
            [feeder.middlewares :as mi]))

(defn router-config [_environment]
  {:data {:coercion coercion-malli/coercion
          :exception pretty/exception
          :muuntaja m/instance
          :middleware [cookies/wrap-cookies
                       params/wrap-params
                       parameters/parameters-middleware
                       muuntaja/format-middleware
                       exception/exception-middleware
                       coercion/coerce-request-middleware
                       coercion/coerce-response-middleware
                       coercion/coerce-exceptions-middleware
                       mi/exception-middleware]}})

(def swagger-docs
  ["/swagger.json"
   {:get {:no-doc true
          :swagger {:basePath "/"
                    :info
                    {:title "Feeder API documentation"
                     :description "Feeder"
                     :version "0.1.0"}
                    :securityDefinitions {:bearer {:type "apiKey"
                                                   :name "token"
                                                   :in "cookies"}}}
          :handler (swagger/create-swagger-handler)}}])

(def session-store (session-memory/memory-store))

(defn api-router [environment]
  [swagger-docs
   ["" {:middleware [swagger/swagger-feature
                     [session/wrap-session {:store session-store}]]}
    (users/routes environment)
    (time/routes environment)]])

(def assets-router
  ["" {:no-doc true}
   ["/assets/*" (ring/create-resource-handler)]
   ["/favicon.ico" (ring/create-resource-handler)]])

(defn router [environment]
  (wrap-cors
   (ring/ring-handler
    (ring/router
     [""
      (api-router environment)
      assets-router]
     (router-config environment))
    (ring/routes
     (swagger-ui/create-swagger-ui-handler {:path "/swagger"}))
    (ring/create-default-handler))
   :access-control-allow-origin [#".*"]
   :access-control-allow-methods [:get :put :post :delete]
   :access-control-allow-credentials "true"))

(defn routes [environment]
  (router environment))
