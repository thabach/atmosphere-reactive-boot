package com.yulplay.reactive.boot;

import com.yulplay.protocol.Enveloppe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public final class ReplyException implements Serializable{

    private final Logger logger = LoggerFactory.getLogger(ReplyException.class);
    private Throwable throwable;
    private String errorMessage = "";
    private Enveloppe yulplayEnveloppe;

    public ReplyException() {
        this(null, null);
    }

    public ReplyException(Throwable throwable) {
        this.throwable = throwable;
    }

    public ReplyException(Throwable throwable, String error) {
        this.throwable = throwable;
        this.errorMessage = error;
    }

    public Throwable throwable() {
        return throwable;
    }

    public String error() {
        return errorMessage;
    }

    public ReplyException throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public ReplyException error(String error) {
        this.errorMessage = error;
        return this;
    }

    public ReplyException message(Enveloppe yulplayEnveloppe) {
        this.yulplayEnveloppe = yulplayEnveloppe;
        return this;
    }

    public Enveloppe message(){
        return yulplayEnveloppe;
    }
}

