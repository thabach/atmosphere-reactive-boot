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
package com.yulplay.reactive.boot.inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulplay.protocol.Envelope;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Properties;

@ApplicationScoped
public class KafkaProducerInjectable implements Injectable<KafkaProducer> {
    private final Logger logger = LoggerFactory.getLogger(KafkaProducerInjectable.class);

    @Inject
    private ObjectMapper mapper;

    @Inject
    @Named("yulplay.kafka.config.producer.path")
    private Properties properties;

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && KafkaProducer.class.isAssignableFrom((Class) t);
    }

    @Override
    public KafkaProducer injectable(AtmosphereConfig config) {
        KafkaProducer producer = new KafkaProducer(properties, new ByteArraySerializer(), new Serializer<Envelope>() {

            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {
            }

            @Override
            public byte[] serialize(String topic, Envelope data) {
                try {
                    return mapper.writeValueAsBytes(data);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void close() {
            }
        });

        config.shutdownHook(() -> {
            if (producer != null) {
                producer.close();
            }
        });

        return producer;
    }
}
