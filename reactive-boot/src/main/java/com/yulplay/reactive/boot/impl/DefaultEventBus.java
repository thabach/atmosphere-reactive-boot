/*
 * Copyright 2015-2016 Yulplay.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.yulplay.reactive.boot.impl;

import com.yulplay.protocol.Envelope;
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
            private Envelope yulplayEnvelope;
            private Reply reply;

            @Override
            public EventBus async(boolean b) {
                return DefaultEventBus.this.async(b);
            }

            @Override
            public EventBus dispatch(Envelope yulplayEnvelope) {
                this.yulplayEnvelope = yulplayEnvelope;
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
                    synchronized(yulplayEnvelope) {
                        reply = Reply.VOID_REPLY;
                    }
                }
                for (final String p : path) {
                    if (async) {
                        service.submit(() -> executeDispatch(p, yulplayEnvelope, reply));
                    } else {
                        executeDispatch(p, yulplayEnvelope, reply);
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

    private final void executeDispatch(String path, Envelope yulplayEnvelope, Reply<?> reply) {
        Service s = servicesMapper.map(path, services);
        if (s != null) {
            try {
                logger.trace("Service found {}", s);
                s.on(yulplayEnvelope, reply);
            } catch (Exception ex) {
                logger.error("", ex);
            }
        } else {
            throw new IllegalStateException(String.format("No mapping for %s", path));
        }
    }

    @Override
    public EventBus dispatch(Envelope yulplayEnvelope) {
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
