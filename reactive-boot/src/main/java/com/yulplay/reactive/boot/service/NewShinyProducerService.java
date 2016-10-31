package com.yulplay.reactive.boot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulplay.protocol.Message;
import com.yulplay.reactive.boot.Reply;
import com.yulplay.reactive.boot.ReplyException;
import com.yulplay.reactive.boot.Service;
import com.yulplay.reactive.boot.ServiceRegistration;
import com.yulplay.reactive.boot.annotation.On;
import com.yulplay.reactive.boot.impl.NodeUuid;
import kafka.producer.KeyedMessage;
import kafka.producer.NewShinyProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Difference with the {@link KafkaProducerService} is this producer can be fully configured via property.
 * This is the one used by bin/kafka-console-producer.sh and just a Scala Wrapped of
 * {@link org.apache.kafka.clients.producer.KafkaProducer}
 */
@On(ServiceRegistration.KAFKA_SHINY_PRODUCER)
public class NewShinyProducerService implements Service<Void> {
    private final Logger logger = LoggerFactory.getLogger(NewShinyProducerService.class);

    @Inject
    private NodeUuid nodeUuid;

    @Inject
    private NewShinyProducer producer;

    @Inject
    @Named("yulplay.kafka.topic")
    private String topic;

    @Inject
    private ObjectMapper mapper;

    @Override
    public void on(Message message, Reply<Void> reply) {
        logger.debug("{} producer.send {}", message.webSocketUUID(), message.uuid());

        // For tracing purpose.
        message.nodeUuid(nodeUuid.uuidString());
        try {
            KeyedMessage<byte[], byte[]> km = new KeyedMessage<>(topic, mapper.writeValueAsBytes(message));
            producer.send(km.topic(), km.key(), km.message());
        } catch (Exception e) {
            logger.error("", e);
            reply.fail(new ReplyException(e, "Unable to send message to the Kafka Cluster").message(message));
        }
    }

}



