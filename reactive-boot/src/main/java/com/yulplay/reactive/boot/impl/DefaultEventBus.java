package com.yulplay.reactive.boot.impl;

import com.yulplay.protocol.Enveloppe;
import com.yulplay.reactive.boot.EventBus;
import com.yulplay.reactive.boot.Reply;
import com.yulplay.reactive.boot.Service;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.util.DefaultEndpointMapper;
import org.atmosphere.util.EndpointMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class DefaultEventBus implements EventBus {
    private final Logger logger = LoggerFactory.getLogger(DefaultEventBus.class);

    private final EndpointMapper<Service> servicesMapper = new DefaultEndpointMapper<>();
    private final ConcurrentHashMap<String, Service> services = new ConcurrentHashMap<>();

    @Inject
    private ExecutorService service;

    @Inject
    private AtmosphereConfig config;

    public DefaultEventBus() {
    }

    @Override
    public EventBus async(final boolean async) {
        return new EventBus() {
            private Enveloppe yulplayEnveloppe;
            private Reply reply;

            @Override
            public EventBus async(boolean b) {
                return DefaultEventBus.this.async(b);
            }

            @Override
            public EventBus dispatch(Enveloppe yulplayEnveloppe) {
                this.yulplayEnveloppe = yulplayEnveloppe;
                return this;
            }

            @Override
            public EventBus replyTo(Reply<?> reply) {
                this.reply = reply;
                return this;
            }

            @Override
            public EventBus to(String... path) {

                if (reply == null) {
                    synchronized(yulplayEnveloppe) {
                        reply = Reply.VOID_REPLY;
                    }
                }
                for (final String p : path) {
                    if (async) {
                        service.submit(() -> executeDispatch(p, yulplayEnveloppe, reply));
                    } else {
                        executeDispatch(p, yulplayEnveloppe, reply);
                    }
                }
                return this;
            }

            @Override
            public EventBus on(String path, Service service) {
                return DefaultEventBus.this.on(path, service);
            }

            @Override
            public EventBus off(String path) {
                return DefaultEventBus.this.off(path);
            }

            @Override
            public void destroy() {
                DefaultEventBus.this.destroy();
            }
        };
    }

    private final void executeDispatch(String path, Enveloppe yulplayEnveloppe, Reply<?> reply) {
        Service s = servicesMapper.map(path, services);
        if (s != null) {
            try {
                logger.trace("Service found {}", s);
                s.on(yulplayEnveloppe, reply);
            } catch (Exception ex) {
                logger.error("", ex);
            }
        } else {
            throw new IllegalStateException(String.format("No mapping for %s", path));
        }
    }

    @Override
    public EventBus dispatch(Enveloppe yulplayEnveloppe) {
        throw new IllegalStateException("Must call async first");
    }

    @Override
    public EventBus replyTo(Reply<?> reply) {
        throw new IllegalStateException("Must call async first");
    }

    @Override
    public EventBus to(String... path) {
        throw new IllegalStateException("Must call async first");
    }

    @Override
    public EventBus on(String path, Service service) {
        services.put(path, service);
        return this;
    }

    @Override
    public EventBus off(String path) {
        services.remove(path);
        return this;
    }

    /**
     * For testing purpose or internal manipulation ONLY.
     *
     * @param path the path mapping a {@link Service}
     * @return the Service
     */
    public Service at(String path) {
        return services.get(path);
    }

    @Override
    public void destroy() {
        services.clear();
    }
}
