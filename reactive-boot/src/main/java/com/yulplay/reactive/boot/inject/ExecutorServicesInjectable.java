
package com.yulplay.reactive.boot.inject;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;
import org.atmosphere.util.ExecutorsFactory;

import java.lang.reflect.Type;
import java.util.concurrent.ExecutorService;

@ApplicationScoped
public class ExecutorServicesInjectable implements Injectable<ExecutorService> {
    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && ExecutorService.class.isAssignableFrom((Class) t);
    }

    @Override
    public ExecutorService injectable(AtmosphereConfig config) {
        return ExecutorsFactory.getMessageDispatcher(config, "");
    }
}
