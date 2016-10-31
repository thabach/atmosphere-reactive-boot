package com.yulplay.reactive.boot.impl;

import com.yulplay.reactive.boot.EventBus;
import com.yulplay.reactive.boot.Service;
import com.yulplay.reactive.boot.annotation.On;
import org.atmosphere.annotation.Processor;
import org.atmosphere.config.AtmosphereAnnotation;
import org.atmosphere.cpr.AtmosphereFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@AtmosphereAnnotation(On.class)
public class OnAnnotationProcessor implements Processor<Service> {

    @Inject
    private EventBus eventBus;
    private Logger logger = LoggerFactory.getLogger(OnAnnotationProcessor.class);

    @Override
    public void handle(AtmosphereFramework framework, Class<Service> annotatedClass) {

        On s = annotatedClass.getAnnotation(On.class);
        try {
            logger.info("Registering @On annotation {}", annotatedClass.getName());
            String[] values = s.value();
            Service service = framework.newClassInstance(Service.class, annotatedClass);
            for (String v : values) {
                eventBus.on(v, service);
            }
        } catch (Exception e) {
            logger.error("Unable to register {}", annotatedClass, e);
        }
    }
}
