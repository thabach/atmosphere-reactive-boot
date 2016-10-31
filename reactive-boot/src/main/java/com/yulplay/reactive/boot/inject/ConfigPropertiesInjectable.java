
package com.yulplay.reactive.boot.inject;

import com.yulplay.reactive.boot.ConfigProperties;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;

import java.lang.reflect.Type;

@ApplicationScoped
public class ConfigPropertiesInjectable implements Injectable<ConfigProperties> {
    private final ConfigProperties configProperties = new ConfigProperties();

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && ConfigProperties.class.isAssignableFrom((Class) t);
    }

    @Override

    public ConfigProperties injectable(AtmosphereConfig config) {
        return configProperties;
    }
}
