package com.yulplay.reactive.boot.inject;

import com.yulplay.reactive.boot.ConfigProperties;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.InjectIntrospectorAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class PropertyBooleanInjectable extends InjectIntrospectorAdapter<Boolean> {

    @Inject
    private ConfigProperties configProperties;

    private Boolean topic;

    @Override
    public void introspectField(Class clazz, Field field) {
        if (field.isAnnotationPresent(Named.class)) {
            String value = field.getAnnotation(Named.class).value();
            if (value.startsWith("yulplay.")) {
                // Do not override the value set by the class itself.
                if (configProperties.getString(value) != null) {
                    topic = configProperties.getBoolean(value);
                }
            }
        }
    }

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && t.equals(boolean.class);
    }

    @Override
    public Boolean injectable(AtmosphereConfig config) {
        return topic;
    }
}
