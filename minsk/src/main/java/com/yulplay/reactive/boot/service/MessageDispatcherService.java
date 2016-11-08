/**
 * Copyright 2015 Yulplay.com
 */
package com.yulplay.reactive.boot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulplay.protocol.Enveloppe;
import com.yulplay.reactive.boot.ReactiveWebSocketFactory;
import com.yulplay.reactive.boot.Reply;
import com.yulplay.reactive.boot.Service;
import com.yulplay.reactive.boot.annotation.On;
import com.yulplay.reactive.boot.schema.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.time.LocalDateTime;

@On("/dispatch")
public class MessageDispatcherService implements Service<Enveloppe> {
    private final Logger logger = LoggerFactory.getLogger(MessageDispatcherService.class);

    @Inject
    private ReactiveWebSocketFactory webSocketFactory;

    @Inject
    private ObjectMapper mapper;

    @Override
    public void on(Enveloppe enveloppe, Reply<Enveloppe> reply) throws IOException {
        Message message = mapper.readValue(enveloppe.body(), Message.class);

        message.setClock(LocalDateTime.now().toString());

        byte[] b64Message = mapper.writeValueAsBytes(message);

        byte[] b64Enveloppe = mapper.writeValueAsBytes(enveloppe.withBody(b64Message));
        webSocketFactory.findAll().parallelStream()
                .forEach(w -> {
                    try {
                        w.write(b64Enveloppe, 0, b64Enveloppe.length);
                    } catch (IOException e) {
                        logger.warn("", e);
                    }
                });
    }
}
