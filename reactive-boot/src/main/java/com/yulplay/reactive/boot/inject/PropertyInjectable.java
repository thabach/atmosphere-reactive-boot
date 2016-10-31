package com.yulplay.reactive.boot.inject;

import com.yulplay.reactive.boot.ConfigProperties;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.InjectIntrospectorAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class PropertyInjectable extends InjectIntrospectorAdapter<String> {

    @Inject
    private ConfigProperties configProperties;

    private String topic;

    @Override
    public void introspectField(Class clazz, Field field) {
        if (field.isAnnotationPresent(Named.class)) {
            topic = field.getAnnotation(Named.class).value();
            if (topic.startsWith("yulplay.")) {
                topic = configProperties.getString(topic);
            }
        }
    }

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && String.class.equals((Class) t);
    }

    @Override
    public String injectable(AtmosphereConfig config) {
        return topic;
    }
}
