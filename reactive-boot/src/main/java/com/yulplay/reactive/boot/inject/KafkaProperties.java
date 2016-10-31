package com.yulplay.reactive.boot.inject;

import com.yulplay.reactive.boot.ConfigProperties;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.InjectIntrospectorAdapter;
import org.atmosphere.inject.annotation.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Properties;

@ApplicationScoped
public class KafkaProperties extends InjectIntrospectorAdapter<Properties> {
    private final Logger logger = LoggerFactory.getLogger(KafkaProperties.class);

    @Inject
    private ConfigProperties configProperties;

    private Properties properties;

    String propertyName = "/";

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && Properties.class.isAssignableFrom((Class) t);
    }

    @Override
    public void introspectField(Class clazz, Field f) {
        if (f.isAnnotationPresent(Named.class)) {
            propertyName = f.getAnnotation(Named.class).value();
        }
    }

    @Override
    public Properties injectable(AtmosphereConfig config) {
        String propertiesFile = configProperties.getString(propertyName, null);
        properties = new Properties();
        if (propertiesFile == null) {
            throw new RuntimeException("Unable to read properties file from " + propertiesFile);
        } else {
            logger.info("Loading Kafka.producer.properties file from {}", propertiesFile);
            try {
                properties.load(new FileInputStream(new File(propertiesFile)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return properties;
    }
}
