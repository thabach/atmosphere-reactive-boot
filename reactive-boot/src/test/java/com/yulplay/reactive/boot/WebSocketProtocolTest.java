package com.yulplay.reactive.boot;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ws.WebSocket;
import com.ning.http.client.ws.WebSocketByteListener;
import com.ning.http.client.ws.WebSocketUpgradeHandler;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class WebSocketProtocolTest {

    private ReactiveBootstrap boostrap;
    private int port;
    private String target = "ws://127.0.0.1:";

    @BeforeMethod
    public void setUp() throws Exception {
        TestUtil.configurePaths();

        port = TestUtil.findFreePort();
        boostrap = new ReactiveBootstrap().listenTo(URI.create(target + port)).on();
    }

    @AfterMethod
    public void off() {
        if (boostrap != null) boostrap.off();
    }

    // Disabled for now by https://github.com/Atmosphere/nettosphere/issues/89. Handled internall by Netty
    @Test (enabled = false)
    public void testNoSubProtocol() throws Exception {
        final CountDownLatch l = new CountDownLatch(1);

        final AtomicBoolean OK = new AtomicBoolean();
        AsyncHttpClient c = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setMaxRequestRetry(-1).build());
        try {
            WebSocket webSocket = c.prepareGet(target + port + "/").execute(new WebSocketUpgradeHandler.Builder().build()).get();
            assertNotNull(webSocket);
            webSocket.addWebSocketListener(new WebSocketByteListener() {
                @Override
                public void onMessage(byte[] message) {
                }

                @Override
                public void onOpen(WebSocket websocket) {
                    OK.set(false);
                    l.countDown();
                }

                @Override
                public void onClose(WebSocket websocket) {
                }

                @Override
                public void onError(Throwable t) {
                }
            });
            OK.set(false);
            l.await(5, TimeUnit.SECONDS);
        } catch (Exception ex) {
            OK.set(true);
        } finally {
            assertTrue(OK.get());
            c.close();
        }
    }

}
