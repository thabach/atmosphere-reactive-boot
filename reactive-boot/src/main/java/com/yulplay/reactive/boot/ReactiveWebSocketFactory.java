package com.yulplay.reactive.boot;

import org.atmosphere.nettosphere.NettyWebSocket;
import org.atmosphere.nettosphere.RuntimeEngine;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketFactory;

import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Stream;

public class ReactiveWebSocketFactory implements WebSocketFactory {

    private RuntimeEngine runtimeEngine;

    @Override
    public WebSocket find(String id) {
        return runtimeEngine.findWebSocket(Integer.valueOf(id));
    }

    public Set<WebSocket> findAll() {
        return runtimeEngine.findAllWebSockets();
    }

    public WebSocket findBasedOnSocketAddress(String destination) {
        try {
            Stream<WebSocket> s = runtimeEngine.findAllWebSockets().stream().filter(
                    webSocket ->
                    NettyWebSocket.class.cast(webSocket).address().equalsIgnoreCase(destination));

            return s.reduce((webSocket1, webSocket2) -> webSocket1).get();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    public ReactiveWebSocketFactory runtimeEngine(RuntimeEngine runtimeEngine) {
        this.runtimeEngine = runtimeEngine;
        return this;
    }

}
