package com.yulplay.reactive.boot;


import com.yulplay.protocol.Message;

public interface Service<U> {

    default void on(Message message) {
        on(message, (Reply<U>) Reply.VOID_REPLY);
    }

    void on(Message message, Reply<U> reply);
}
