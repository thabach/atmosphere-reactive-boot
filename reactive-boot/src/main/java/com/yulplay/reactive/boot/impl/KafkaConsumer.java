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
package com.yulplay.reactive.boot.impl;

import com.yulplay.reactive.boot.ConfigProperties;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import org.atmosphere.cpr.AtmosphereConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ForkJoinPool;

public class KafkaConsumer {
    private final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    private List<Receiver> receivers = new ArrayList<>();

    private final String topic;
    private ConsumerConnector consumer;
    private Map<String, Integer> topicCountMap;
    private Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap;

    @Inject
    private ForkJoinPool forkJoinPool;

    @Inject
    private ConfigProperties configProperties;

    @Inject
    private AtmosphereConfig config;

    @Inject
    @Named("yulplay.kafka.config.consumer.path")
    private Properties properties;

    @Inject
    private NodeUuid nodeUuid;

    @Inject
    @Named("yulplay.kafka.enabled")
    private boolean kafkaEnabled = true;

    @Inject
    @Named("yulplay.kafka.pubsub")
    private boolean pubSubMode = true;

    public KafkaConsumer() {
        this.topic = "";
    }

    public KafkaConsumer(String topic) {
        this.topic = topic;
    }

    @PostConstruct
    public void initialize() {

        if (!kafkaEnabled || topic.equalsIgnoreCase("")) {
            return;
        }

        try {
            logger.info("Kafka Customers configured as {} on topic {}", pubSubMode ? "pubsub" : "single mode", topic);

            if (pubSubMode) {
                String groupId = properties.get("group.id").toString();
                properties.put("group.id", groupId + "." + nodeUuid.uuidString().replaceAll("-", ""));
            }

            int streamNumber = configProperties.getInt("yulplay.kafka.streams", 1);

            consumer = Consumer.createJavaConsumerConnector(new ConsumerConfig(properties));
            topicCountMap = new HashMap<>();
            topicCountMap.put(topic, new Integer(streamNumber));

            config.startupHook(framework -> {
                if (config.properties().get("started") != null) return;

                config.properties().put("started", "true");
                consumerMap = consumer.createMessageStreams(topicCountMap);
                for (final String t : topicCountMap.keySet()) {
                    for (KafkaStream<byte[], byte[]> stream : consumerMap.get(t)) {
                        final ConsumerIterator<byte[], byte[]> it = stream.iterator();

                        forkJoinPool.submit(() -> {
                            while (it.hasNext()) {
                                try {
                                    receivers.parallelStream().forEach(receiver -> receiver.message(it.next()));
                                } catch (Throwable ex) {
                                    if (InterruptedException.class.isAssignableFrom(ex.getClass())) {
                                        logger.trace("", ex);
                                    } else {
                                        logger.warn("", ex);
                                    }
                                }
                            }
                        });
                    }
                }
            }).shutdownHook(() -> {
                consumer.shutdown();
            });
        } catch (Exception ex) {
            logger.error("Unable to connect to Kafka", ex);
            if (consumer != null) {
                consumer.shutdown();
            }
            throw new RuntimeException();
        }
    }

    public KafkaConsumer receiver(Receiver l) {
        receivers.add(l);
        return this;
    }

    public interface Receiver {
        void message(MessageAndMetadata<byte[], byte[]> messageAndMetadata);
    }

    public String topic() {
        return topic;
    }

    public ConsumerConnector consumer() {
        return consumer;
    }

}



