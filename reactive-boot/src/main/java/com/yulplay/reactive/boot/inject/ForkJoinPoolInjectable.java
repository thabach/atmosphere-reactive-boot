package com.yulplay.reactive.boot.inject;

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;

import java.lang.reflect.Type;
import java.util.concurrent.ForkJoinPool;

@ApplicationScoped
public class ForkJoinPoolInjectable implements Injectable<ForkJoinPool> {

    private final static ForkJoinPool forkJoinPool = new ForkJoinPool();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(forkJoinPool::shutdown));
    }

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && ForkJoinPool.class.isAssignableFrom((Class) t);
    }

    @Override
    public ForkJoinPool injectable(AtmosphereConfig config) {
        return forkJoinPool;
    }
}
