/*
 * Copyright 2015-2016 Yulplay.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.yulplay.reactive.boot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulplay.protocol.Envelope;
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
    public final static String ALL= "all";

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
    @Named("yulplay.kafka.websocket.lookup")
    private String lookupWebSocket = ALL;

    @PostConstruct
    private void register() {
        consumer.receiver(new ReactiveReceiver());
    }

    @Override
    public void on(Envelope envelope, Reply<Void> reply) {
        // NO OPS.
    }

    private final class ReactiveReceiver implements KafkaConsumer.Receiver {

        @Override
        public void message(MessageAndMetadata<byte[], byte[]> messageAndMetadata) {
            try {
                byte[] m = messageAndMetadata.message();

                if (logger.isTraceEnabled()) {
                    logger.trace("{} {}", messageAndMetadata, new String(m, "UTF-8"));
                }

                Envelope envelope = mapper.readValue(m, Envelope.class);
                if (envelope == null) {
                    logger.warn("Invalid message (null) for key {}", new String(messageAndMetadata.key()));
                    return;
                }

                // If we are on the same node, this will return the faster than the next call
                WebSocket webSocket;
                if (!lookupWebSocket.equalsIgnoreCase(ALL)) {
                    webSocket = webSocketFactory.findBasedOnSocketAddress(envelope.destination());
                    if (webSocket != null) {
                        logger.debug("Message {} Received for {}", envelope.uuid(), envelope.webSocketUUID());
                        logger.debug("WebSocket {} found on node {}", webSocket.uuid(), nodeUuid.uuidString());
                        if (webSocket.attachment() != null) {
                            MessageReceiver.class.cast(webSocket.attachment()).apply(envelope.uuid(), m);
                        } else {
                            logger.error("Message will be lost for webSocket {}, no MessageReceiver", envelope.webSocketUUID());
                        }
                    } else {
                        logger.trace("Unable to find the webSocket associated with {}", envelope.webSocketUUID());
                    }

                } else {
                    webSocketFactory.findAll().parallelStream()
                            .forEach(f -> {
                                try {
                                    MessageReceiver.class.cast(f.attachment()).apply(envelope.uuid(), m);
                                } catch (JsonProcessingException e) {
                                    logger.warn("", e);
                                }
                            });
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
