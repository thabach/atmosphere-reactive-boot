package com.yulplay.reactive.boot;

import com.yulplay.protocol.Message;
import org.atmosphere.nettosphere.NettyWebSocket;
import org.atmosphere.nettosphere.RuntimeEngine;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketFactory;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ReactiveWebSocketFactory implements WebSocketFactory {

    private RuntimeEngine runtimeEngine;

    // Dangerous for memory leak if a Proxy prevent the closing of the websocket or doesn't detect it. The value
    // here may stay forever in that scenario.
    // TODO: add heartbeat support and clear this Map is the heartbeat fail.
    private final Map<String, String> socketsBinded = new ConcurrentHashMap<>();

    @Override
    public WebSocket find(String id) {
        return runtimeEngine.findWebSocket(Integer.valueOf(id));
    }

    /**
     * Use the {@Link Message#origin()} to locate a real WebSocket instance.
     *
     * This method will use the {@link WebSocket#uuid()} to ret
     *
     * @param destination the {@link Message#destination()}
     * @return null if not found, th {@link WebSocket if found}
     */
    public WebSocket findBasedOnOrigin(String destination) {
        Map.Entry<String,String> s = socketsBinded.entrySet().parallelStream().filter(s1 -> s1.getValue().equalsIgnoreCase(destination))
                .reduce((socketIp, socketIp2) -> socketIp).get();

        return s == null ? null : runtimeEngine.findWebSocket(Integer.valueOf(s.getKey()));
    }

    /**
     * Use the {@Link Message#destination()} value to locate a real WebSocket instance.
     *
     * This method might now work when a proxy use it own address instead of the origin address. The Client must set the
     * IP address. A Host or App with multiple IP addresses will not properly work.
     *
     * @param destination the {@link Message#destination()}
     * @return null if not found, th {@link WebSocket if found}
     */
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

    /**
     * Return the {@link RuntimeEngine}
     * @param runtimeEngine a Nettosphere's RuntimEngine
     * @return this
     */
    public ReactiveWebSocketFactory runtimeEngine(RuntimeEngine runtimeEngine) {
        this.runtimeEngine = runtimeEngine;
        return this;
    }

    /**
     * Bind a connected websocket to it's {@link Message#origin()} so it can be retrieved when a {@link Message#destination()}
     * match the value.
     *
     * @param origin the {@link Message#origin()}
     * @param uuid the active {@link WebSocket#uuid()}
     * @return this
     */
    public ReactiveWebSocketFactory bind(String origin, String uuid) {
        socketsBinded.put(uuid, origin);
        return this;
    }

    /**
     * Undind the websocket.
     * @param uuid the active {@link WebSocket#uuid()}
     */
    public void unbind(String uuid) {
        socketsBinded.remove(uuid);
    }
}
