package com.yulplay.reactive.boot;

import com.yulplay.protocol.Enveloppe;

public interface EventBus {

    EventBus async(boolean b);

    EventBus dispatch(Enveloppe payload);

    EventBus replyTo(Reply<?> reply);

    EventBus to(String... path);

    EventBus on(String path, Service eventService);

    EventBus off(String path);

    void destroy();

}
