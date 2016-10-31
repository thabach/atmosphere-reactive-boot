package com.yulplay.reactive.boot.service;

import com.yulplay.protocol.Message;
import com.yulplay.reactive.boot.Reply;
import com.yulplay.reactive.boot.ReplyException;
import com.yulplay.reactive.boot.Service;
import com.yulplay.reactive.boot.ServiceRegistration;
import com.yulplay.reactive.boot.annotation.On;
import com.yulplay.reactive.boot.impl.NodeUuid;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;

@On(ServiceRegistration.KAFKA_PRODUCER)
public class KafkaProducerService implements Service<Void> {
    private final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);

    @Inject
    private NodeUuid nodeUuid;

    @Inject
    private KafkaProducer producer;

    @Inject
    @Named("yulplay.kafka.topic")
    private String topic;

    @Override
    public void on(Message message, Reply<Void> reply) {
        logger.debug("{} producer.send {}", message.webSocketUUID(), message.uuid());

        // For tracing purpose.
        message.nodeUuid(nodeUuid.uuidString());
        try {
            producer.send(new ProducerRecord(topic, null, message), (metadata, exception) -> {
                logger.debug("Message {} delivered to Kafka Cluster", message.uuid());
                // reply.ok()
            });
        } catch (Exception e) {
            logger.error("", e);
            reply.fail(new ReplyException(e, "Unable to send message to the Kafka Cluster").message(message));
        }
    }

}



