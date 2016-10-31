package com.yulplay.reactive.boot.inject;

import com.yulplay.reactive.boot.EventBus;
import com.yulplay.reactive.boot.impl.DefaultEventBus;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;

import java.lang.reflect.Type;

@ApplicationScoped
public class EventBusInjectable implements Injectable<EventBus> {

    private EventBus eventBus;

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && EventBus.class.isAssignableFrom((Class) t);
    }

    @Override
    public EventBus injectable(AtmosphereConfig config) {
        if (eventBus == null) {
            try {
                eventBus = config.framework().newClassInstance(EventBus.class, DefaultEventBus.class);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return eventBus;
    }

}
