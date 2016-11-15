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

import com.yulplay.protocol.Enveloppe;
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
    public void on(Enveloppe enveloppe, Reply<Void> reply) {
        logger.debug("{} producer.send {}", enveloppe.webSocketUUID(), enveloppe.uuid());

        // For tracing purpose.
        enveloppe.nodeUuid(nodeUuid.uuidString());
        try {
            producer.send(new ProducerRecord(topic, null, enveloppe), (metadata, exception) -> {
                logger.debug("Message {} delivered to Kafka Cluster", enveloppe.uuid());
                // reply.ok()
            });
        } catch (Exception e) {
            logger.error("", e);
            reply.fail(new ReplyException(e, "Unable to send message to the Kafka Cluster").message(enveloppe));
        }
    }

}



