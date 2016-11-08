package com.yulplay.reactive.boot.inject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulplay.protocol.Enveloppe;
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
        KafkaProducer producer = new KafkaProducer(properties, new ByteArraySerializer(), new Serializer<Enveloppe>() {

            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {
            }

            @Override
            public byte[] serialize(String topic, Enveloppe data) {
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
