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
package com.yulplay.reactive.boot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulplay.protocol.Envelope;
import com.yulplay.reactive.boot.ReactiveWebSocketFactory;
import com.yulplay.reactive.boot.Reply;
import com.yulplay.reactive.boot.Service;
import com.yulplay.reactive.boot.annotation.On;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;

@On("/typing")
public class ClientTypingService implements Service<Envelope> {
    private final Logger logger = LoggerFactory.getLogger(MessageDispatcherService.class);

    @Inject
    private ReactiveWebSocketFactory webSocketFactory;

    @Inject
    private ObjectMapper mapper;

    @Override
    public void on(Envelope envelope, Reply<Envelope> reply) throws IOException {
        byte[] b64Enveloppe = mapper.writeValueAsBytes(envelope);
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
