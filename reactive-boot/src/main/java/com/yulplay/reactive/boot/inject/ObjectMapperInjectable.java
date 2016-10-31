
package com.yulplay.reactive.boot.inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;

import java.lang.reflect.Type;

@ApplicationScoped
public class ObjectMapperInjectable implements Injectable<ObjectMapper> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && ObjectMapper.class.equals((Class) t);
    }

    @Override

    public ObjectMapper injectable(AtmosphereConfig config) {
        return mapper;
    }
}