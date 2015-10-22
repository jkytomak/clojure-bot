(ns codecamp2015.bot
  (:require [aleph.udp :as udp]
            [cheshire.core :as json]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [clojure.tools.logging :as log]))

(def bot-name "Planet Express")
(def bot-color "blue")
(def players [{:number 1 :name "Fry"}
              {:number 2 :name "Leela"}])

(defn server-address [host port]
  (if (= "0.0.0.0" host)
    {:host "127.0.0.1" :port port}
    {:host host :port port}))

(defn parse-udp-msg [msg]
  (assoc msg :message (json/parse-string (new String (:message msg) "UTF-8") true)))

(defn send-message [udp-stream server-address connection-id message]
  (let [udp-message (->> (assoc message :connection-id connection-id)
                         (json/generate-string)
                         (assoc server-address :message))]
    (log/debug "Sending message" udp-message)
    (s/put! udp-stream udp-message)))

(defn start-game [udp-stream connection-id game-at]
  (let [server-address (server-address (:address game-at) (:port game-at))]
    (log/info "Connecting to game:" server-address)
    (send-message
      udp-stream
      server-address
      connection-id
      {:type "join"
       :name bot-name
       :color bot-color
       :game-id (:game-id game-at)
       :players players})))

(defn handle-message [udp-stream connection-id udp-msg]
  (let [parsed-udp-msg (parse-udp-msg udp-msg)
        msg (:message parsed-udp-msg)]
    (case (:type msg)
      "ping" (send-message udp-stream parsed-udp-msg connection-id {:type "pong"})
      "game-at" (start-game udp-stream connection-id msg)
      (log/warn "Unhandled msg type" (:type msg) "in message" parsed-udp-msg))))

(defn server-msg-loop [udp-stream connection-id]
  (s/consume (partial handle-message udp-stream connection-id) udp-stream))

(defn connect-to-lobby-server [udp-stream game-id server-address]
  (send-message udp-stream server-address nil {:type "connect"
                                               :name bot-name
                                               :game-id game-id})
  (d/chain
    (s/take! udp-stream)
    parse-udp-msg
    (fn [parsed-udp-msg]
      (log/info "Connected to lobby server:" parsed-udp-msg)
      (server-msg-loop udp-stream (:connection-id (:message parsed-udp-msg))))))

(defn start-bot [game-id server-host server-port client-port]
  (-> (d/chain
        (udp/socket {:port client-port :broadcast? false})
        (fn [udp-stream]
          (log/info "UDP socket opened in port" client-port)
          (connect-to-lobby-server udp-stream game-id (server-address server-host server-port))))
      (d/catch #(log/error "Unexpected error:" %))))
