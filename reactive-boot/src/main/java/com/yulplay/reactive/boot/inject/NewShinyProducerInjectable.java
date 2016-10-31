package com.yulplay.reactive.boot.inject;

import kafka.producer.NewShinyProducer;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Type;
import java.util.Properties;

@ApplicationScoped
public class NewShinyProducerInjectable implements Injectable<NewShinyProducer> {
    private final Logger logger = LoggerFactory.getLogger(NewShinyProducerInjectable.class);

    @Inject
    @Named("yulplay.kafka.config.producer.path")
    private Properties properties;

    @Inject
    @Named("yulplay.kafka.shiny")
    private String shiny;

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && NewShinyProducer.class.isAssignableFrom((Class) t);
    }

    @Override
    public NewShinyProducer injectable(AtmosphereConfig config) {

        if (shiny == null || shiny.equalsIgnoreCase("disable")) return null;

        NewShinyProducer producer = new NewShinyProducer(properties);

        config.shutdownHook(() -> {
            if (producer != null) {
                producer.close();
            }
        });

        return producer;
    }
}
