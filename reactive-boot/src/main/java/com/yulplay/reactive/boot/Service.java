package com.yulplay.reactive.boot;


import com.yulplay.protocol.Enveloppe;

import java.io.IOException;

public interface Service<U> {

    void on(Enveloppe enveloppe, Reply<U> reply) throws IOException;
}
