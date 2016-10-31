package com.yulplay.reactive.boot;

import com.yulplay.protocol.Message;
import com.yulplay.reactive.boot.annotation.On;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.assertNotNull;

public class EventBusTest {

    private ReactiveBootstrap boostrap;
    private EventBus eventBus;

    @BeforeMethod
    public void setUp() throws Exception {
        TestUtil.configurePaths();
        boostrap = new ReactiveBootstrap().listenTo(URI.create("http://127.0.0.1:" + TestUtil.findFreePort())).on();
        eventBus = boostrap.eventBus();
    }

    @AfterMethod
    public void off() {
        if (boostrap != null) boostrap.off();
    }

    @On("/detectAndInvokeService")
    public final static class EchoService implements Service<byte[]> {

        @Override
        public void on(Message payload, Reply<byte[]> reply) {
            reply.ok(payload.body());
        }
    }

    @Test
    public void detectAndInvokeServiceAsync() throws InterruptedException {
        final AtomicReference<byte[]> resource = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Message yulyMessage = new Message("/HOST/APP/CLOUD", "127.0.0.1", "0.0.0.0", new byte[0]);

        eventBus.async(true).dispatch(yulyMessage).replyTo(new Reply<byte[]>() {
            @Override
            public void ok(byte[] response) {
                resource.set(response);
                latch.countDown();
            }

            @Override
            public void fail(ReplyException replyException) {
            }
        }).to("/detectAndInvokeService");

        latch.await(5, TimeUnit.SECONDS);

        assertNotNull(resource.get());
    }

    @Test
    public void detectAndInvokeServiceSync() throws InterruptedException {
        final AtomicReference<byte[]> resource = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        Message yulyMessage = new Message("/HOST/APP/CLOUD", "127.0.0.1", "0.0.0.0", new byte[0]);

        eventBus.async(true).dispatch(yulyMessage).replyTo(new Reply<byte[]>() {
            @Override
            public void ok(byte[] response) {
                resource.set(response);
                latch.countDown();
            }

            @Override
            public void fail(ReplyException replyException) {
            }
        }).to("/detectAndInvokeService");

        latch.await(5, TimeUnit.SECONDS);

        assertNotNull(resource.get());
    }

}
