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
package com.yulplay.reactive.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yulplay.protocol.Enveloppe;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceImpl;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketProcessorAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This is the lowest entry point in Atmosphere to write WebSocket application.
 */
public class ReactivesWebSocketProcessor extends WebSocketProcessorAdapter {
    private final static Logger logger = LoggerFactory.getLogger(ReactivesWebSocketProcessor.class);

    private enum ReplyTo {ALL_WEBSOCKETS, WEBSOCKET}

    public final static String WEBSOCKET_SUB_PROTOCOL = "Sec-WebSocket-Protocol";

    @Inject
    private EventBus eventBus;

    @Inject
    private AtmosphereFramework framework;

    @Inject
    private ObjectMapper objectMapper;

    @Inject
    private ReactiveWebSocketFactory webSocketFactory;

    @Inject
    @Named("yulplay.nettosphere.noInternalAlloc")
    private boolean noInternalAlloc = true;

    @Inject
    @Named("yulplay.service.async")
    private boolean executeAsync = false;

    @Inject
    @Named("yulplay.kafka.websocket.uniqueReply")
    private boolean uniqueReply = true;

    // For TRACE logging. It max throw an exception if the max is reached, but production must not run with TRACE enabled!
    private static final AtomicInteger connected = new AtomicInteger();

    public ReactivesWebSocketProcessor() {
    }

    @Override
    public boolean handshake(HttpServletRequest request) {
        // config.noInternalAlloc() == true
        if (noInternalAlloc) return true;

        // With Netty this will always pass as the check is done in the library itself. Not all server
        // supports it, so just do a quick check here.
        String protocol = request.getHeader(WEBSOCKET_SUB_PROTOCOL);
        return true;
    }

    @Override
    public void open(WebSocket webSocket, AtmosphereRequest request, AtmosphereResponse response) throws IOException {

        webSocket.attachment(new MessageReceiver(objectMapper, webSocket));
        if (logger.isTraceEnabled()) {
            logger.trace("WebSocket {} opened: {}", webSocket, connected.incrementAndGet());
        }

        if (noInternalAlloc) return;

        // We use this code only when noInternalAlloc
        // is set to false. But using this code will significantly increase the amount of objects created on the heap.
        // TODO: throw new RuntimeException instead?
        AtmosphereResource r = framework.atmosphereFactory().create(framework.getAtmosphereConfig(),
                response,
                framework.getAsyncSupport());
        AtmosphereResourceImpl.class.cast(r).webSocket(webSocket);
        webSocket.resource(r);
    }

    @Override
    public void invokeWebSocketProtocol(WebSocket webSocket, String webSocketMessage) {
        logger.error("Dispatching webSocket String payload");
        webSocket.close();
    }

    @Override
    public void invokeWebSocketProtocol(final WebSocket webSocket, byte[] data, int offset, int length) {
        logger.debug("Dispatching webSocket payload");

        // (1) Read Message
        Enveloppe enveloppe = null;
        try {
            enveloppe = objectMapper.readValue(data, 0, data.length, Enveloppe.class);
        } catch (IOException e) {
            logger.error("", e);
            webSocket.close();
        }

        // (32) Bind the message to the websocket and to this node.
        enveloppe.webSocketUUID(webSocket.uuid());

        // (3) Reply
        Reply<byte[]> reply;
        if (!uniqueReply) {
            // (4) Prepare Reply
            reply = new Reply<byte[]>() {
                @Override
                public void ok(byte[] bytes) {
                    try {
                        webSocket.write(bytes, 0, bytes.length);
                    } catch (IOException e) {
                        logger.warn("", e);
                    }
                }

                @Override
                public void fail(ReplyException replyException) {
                    MessageReceiver.class.cast(webSocket.attachment()).discard(replyException.message().uuid());
                }
            };

            // (5) Attach message to the Reply for async
            MessageReceiver.class.cast(webSocket.attachment()).receiveWith(enveloppe, reply);
        } else {
            reply = MessageReceiver.class.cast(webSocket.attachment()).receiveWith();
        }

        // (4) Dispatch the message to the proper service. If Kafka is the only Service defined, event bus is not needed.
        eventBus.async(executeAsync).dispatch(enveloppe).replyTo(reply).to(enveloppe.path());
    }

    @Override
    public void close(WebSocket webSocket, int closeCode) {
        logger.trace("WebSocket {} closed with {}", webSocket, closeCode);

        if (logger.isTraceEnabled()) {
            connected.decrementAndGet();
        }

        MessageReceiver r = (MessageReceiver) webSocket.attachment();
        if (r != null) r.destroy();

        webSocket.attachment(null);
    }

}
