package com.yulplay.reactive.boot;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public interface Reply<T> extends Serializable {

    Reply<byte[]> VOID_REPLY = new Reply<byte[]>() {
        @Override
        public void ok(byte[] bytes) {
        }

        @Override
        public void fail(ReplyException replyException) {
        }
    };

    void ok(T response);

    default void fail(ReplyException replyException) {}

}
