package com.yulplay.reactive.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulplay.protocol.Message;
import com.yulplay.reactive.boot.service.KafkaCustomerService;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketProcessorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.yulplay.reactive.boot.ServiceRegistration.KAFKA_PRODUCER;

/**
 * This is the lowest entry point in Atmosphere to write WebSocket application.
 */
public class ReactivesWebSocketProcessor extends WebSocketProcessorAdapter {
    private final static Logger logger = LoggerFactory.getLogger(ReactivesWebSocketProcessor.class);

    private enum ReplyTo {ALL_WEBSOCKETS, WEBSOCKET}

    public final static String PROTOCOL = "rpm-protocol";
    public final static String WEBSOCKET_SUB_PROTOCOL = "Sec-WebSocket-Protocol";

    @Inject
    private EventBus eventBus;

    @Inject
    private AtmosphereFramework framework;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private ReactiveWebSocketFactory webSocketFactory;

    @Inject
    @Named("yulplay.nettosphere.noInternalAlloc")
    private boolean noInternalAlloc = true;

    @Inject
    @Named("yulplay.service.default")
    private String defaultService = KAFKA_PRODUCER;

    @Inject
    @Named("yulplay.service.async")
    private boolean executeAsync = false;

    @Inject
    @Named("yulplay.kafka.websocket.lookup")
    private String lookupWebSocket = KafkaCustomerService.FIND_BY_ORIGIN; // ""realIp";

    @Inject
    @Named("yulplay.kafka.websocket.uniqueReply")
    private boolean uniqueReply = true;

    // For TRACE logging. It max throw an exception if the max is reached, but production must not run with TRACE enabled!
    private static final AtomicInteger connected = new AtomicInteger();

    public ReactivesWebSocketProcessor() {
    }

    @Override
    public boolean handshake(HttpServletRequest request) {
        // config.noInternalAlloc() == true
        if (noInternalAlloc) return true;

        // With Netty this will always pass as the check is done in the library itself. Not all server
        // supports it, so just do a quick check here.
        String protocol = request.getHeader(WEBSOCKET_SUB_PROTOCOL);
        if (protocol == null || !protocol.equalsIgnoreCase(PROTOCOL)) {
            return false;
        }
        return true;
    }

    @Override
    public void open(WebSocket webSocket, AtmosphereRequest request, AtmosphereResponse response) throws IOException {

        webSocket.attachment(new MessageReceiver(objectMapper, webSocket));
        if (logger.isTraceEnabled()) {
            logger.trace("WebSocket {} opened: {}", webSocket, connected.incrementAndGet());
        }

        if (noInternalAlloc) return;

        // We use this code only when noInternalAlloc
        // is set to false. But using this code will significantly increase the amount of objects created on the heap.
        // TODO: throw new RuntimeException instead?
        AtmosphereResource r = framework.atmosphereFactory().create(framework.getAtmosphereConfig(),
                response,
                framework.getAsyncSupport());
        AtmosphereResourceImpl.class.cast(r).webSocket(webSocket);
        webSocket.resource(r);
    }

    @Override
    public void invokeWebSocketProtocol(WebSocket webSocket, String webSocketMessage) {
        logger.error("Dispatching webSocket String payload");
        webSocket.close();
    }

    @Override
    public void invokeWebSocketProtocol(final WebSocket webSocket, byte[] data, int offset, int length) {
        logger.debug("Dispatching webSocket payload");

        // (1) Read Message
        Message message = null;
        try {
            message = objectMapper.readValue(data, 0, data.length, Message.class);
        } catch (IOException e) {
            logger.error("", e);
            webSocket.close();
        }

        // (3) Bind the message to the websocket and to this node.
        message.webSocketUUID(webSocket.uuid());

        // Will increase memory usage (see the TubesWebSocketFactory)
        if (lookupWebSocket.equals(KafkaCustomerService.FIND_BY_IP)) {
            webSocketFactory.bind(message.origin(), webSocket.uuid());
        }

        Reply<byte[]> reply;
        if (!uniqueReply) {
            // (4) Prepare Reply
            reply = new Reply<byte[]>() {
                @Override
                public void ok(byte[] bytes) {
                    try {
                        webSocket.write(bytes, 0, bytes.length);
                    } catch (IOException e) {
                        logger.warn("", e);
                    }
                }

                @Override
                public void fail(ReplyException replyException) {
                    MessageReceiver.class.cast(webSocket.attachment()).discard(replyException.message().uuid());
                }
            };

            // (5) Attach message to the Reply for async
            MessageReceiver.class.cast(webSocket.attachment()).receiveWith(message, reply);
        } else {
            reply = MessageReceiver.class.cast(webSocket.attachment()).receiveWith();
        }

        // (6) Dispatch the message to the proper service. If Kafka is the only Service defined, event bus is not needed.
        eventBus.async(executeAsync).dispatch(message).replyTo(reply).to(message.transport());
    }

    @Override
    public void close(WebSocket webSocket, int closeCode) {
        logger.trace("WebSocket {} closed with {}", webSocket, closeCode);

        if (logger.isTraceEnabled()) {
            connected.decrementAndGet();
        }

        if (lookupWebSocket.equals(KafkaCustomerService.FIND_BY_IP)) {
            webSocketFactory.unbind(webSocket.uuid());
        }

        MessageReceiver r = (MessageReceiver) webSocket.attachment();
        if (r != null) r.destroy();

        webSocket.attachment(null);
    }

}
