package com.yulplay.reactive.boot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulplay.protocol.Message;
import com.yulplay.reactive.boot.MessageReceiver;
import com.yulplay.reactive.boot.Reply;
import com.yulplay.reactive.boot.Service;
import com.yulplay.reactive.boot.ServiceRegistration;
import com.yulplay.reactive.boot.ReactiveWebSocketFactory;
import com.yulplay.reactive.boot.annotation.On;
import com.yulplay.reactive.boot.impl.KafkaConsumer;
import com.yulplay.reactive.boot.impl.NodeUuid;
import kafka.message.MessageAndMetadata;
import org.atmosphere.websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

@On(ServiceRegistration.KAFKA_CUSTOMER)
public class KafkaCustomerService implements Service<Void> {
    private final Logger logger = LoggerFactory.getLogger(KafkaCustomerService.class);
    public final static String FIND_BY_ORIGIN = "origin";
    public final static String FIND_BY_IP= "realIp";

    @Inject
    private ObjectMapper mapper;

    @Inject
    private NodeUuid nodeUuid;

    @Inject
    private ReactiveWebSocketFactory webSocketFactory;

    @Inject
    @Named("yulplay.kafka.topic")
    private KafkaConsumer consumer;

    @Inject
    @Named("yulplay.kafka.testMode")
    private boolean testMode = true;

    @Inject
    @Named("yulplay.kafka.websocket.lookup")
    private String lookupWebSocket = FIND_BY_ORIGIN; // ""realIp";

    @PostConstruct
    private void register() {
        consumer.receiver(new ReactiveReceiver());
    }

    @Override
    public void on(Message message, Reply<Void> reply) {
        // NO OPS.
    }

    private final class ReactiveReceiver implements KafkaConsumer.Receiver {

        @Override
        public void message(MessageAndMetadata<byte[], byte[]> messageAndMetadata) {
            // TODO: Should we dispatch the I/O operation to another thread using an async Event bus call.
            try {
                byte[] m = messageAndMetadata.message();

                if (logger.isTraceEnabled()) {
                    logger.trace("{} {}", messageAndMetadata, new String(m, "UTF-8"));
                }

                Message message = mapper.readValue(m, Message.class);
                if (message == null) {
                    logger.warn("Invalid message (null) for key {}", new String(messageAndMetadata.key()));
                    return;
                }

                // If we are on the same node, this will return the faster than the next call
                WebSocket webSocket;
                if (testMode) {
                    webSocket = webSocketFactory.find(message.webSocketUUID());
                } else if (lookupWebSocket.equalsIgnoreCase(FIND_BY_ORIGIN)) {
                    webSocket = webSocketFactory.findBasedOnOrigin(message.destination());
                } else {
                    webSocket = webSocketFactory.findBasedOnSocketAddress(message.destination());
                }

                if (webSocket != null) {
                    logger.debug("Message {} Received for {}", message.uuid(), message.webSocketUUID());
                    logger.debug("WebSocket {} found on node {}", webSocket.uuid(), nodeUuid.uuidString());
                    if (webSocket.attachment() != null) {
                        MessageReceiver.class.cast(webSocket.attachment()).apply(message.uuid(), m);
                    } else {
                        logger.error("Message will be lost for webSocket {}, no MessageReceiver", message.webSocketUUID());
                    }
                } else {
                    logger.trace("Unable to find the webSocket associated with {}", message.webSocketUUID());
                }

            } catch (Exception ex) {
                if (InterruptedException.class.isAssignableFrom(ex.getClass())) {
                    logger.trace("", ex);
                } else {
                    logger.warn("", ex);
                }
            }
        }

    }

}
