package com.yulplay.atmosphere.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;
import com.ning.http.client.ws.WebSocket;
import com.ning.http.client.ws.WebSocketByteListener;
import com.ning.http.client.ws.WebSocketUpgradeHandler;
import com.yulplay.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncHttpClientDriver {

    private static final Logger logger = LoggerFactory.getLogger(AsyncHttpClientDriver.class);
    private AsyncHttpClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private final static String LOCAL_ADDRESS = "127.0.0.1";
    protected int threads;
    protected final int requestsPerThreadPerBatch;
    protected final int batches;
    protected final String url;
    protected int warmupRequests;
    protected final int threadIncrementPerBatch;
    private final Message payload;
    private final int errorAllowed;
    private final int thinkTime;
    private final int inBetweenOpenThinkTime;
    private final int inBetweenRequestThinkTime;
    private final AtomicBoolean onError = new AtomicBoolean();
    private final ForkJoinPool forkJoinPool;
    private static final ScheduledExecutorService debugScheduler = Executors.newScheduledThreadPool(1);
    private final boolean observerMode;
    private final int connectWaitingTime;
    private final int testWaitingTime;
    private final int handshakeConnectTime;
    private final int nettyConnectionTimeout;

    public AsyncHttpClientDriver(int threads,
                                 int requestsPerThreadPerBatch,
                                 int batches,
                                 int warmUpRequests,
                                 int payloadSize,
                                 int threadIncrementPerBatch,
                                 int errorAllowed,
                                 int thinkTime,
                                 int inBetweenOpenThinkTime,
                                 int inBetweenRequestThinkTime,
                                 String url,
                                 boolean observerMode,
                                 int connectWaitingTime,
                                 String to,
                                 String from,
                                 String transport,
                                 String homeIp,
                                 String appIp,
                                 int testWaitingTime,
                                 int handshakeConnectTime,
                                 int nettyConnectionTimeout,
                                 int forkJoinThread) {
        if (threads < 1) {
            throw new IllegalArgumentException("Thread count must be > 1");
        }

        this.threads = threads;
        this.requestsPerThreadPerBatch = requestsPerThreadPerBatch;
        this.batches = batches;
        this.url = url.replace("http", "ws");
        this.warmupRequests = warmUpRequests;
        this.threadIncrementPerBatch = threadIncrementPerBatch;
        this.nettyConnectionTimeout = nettyConnectionTimeout * 60 * 1000;

        StringBuilder b = new StringBuilder();
        for (int i = 0; i < payloadSize; i++) {
            b.append(".");
        }

        StringBuilder path = new StringBuilder();
        path.append("/").append(from).append("/").append(to).append("/").append(transport);

        /*
         * To make it easier to test round robin messages, we do a small trick here
         */
        if (homeIp.equalsIgnoreCase(LOCAL_ADDRESS)) {
            try {
                homeIp = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.error("", e);
            }
        }

        if (appIp.equalsIgnoreCase(LOCAL_ADDRESS)) {
            try {
                appIp = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException e) {
                logger.error("", e);
            }
        }

        this.payload = new Message(path.toString(), homeIp, appIp, b.toString().getBytes());
        this.errorAllowed = errorAllowed;
        this.thinkTime = thinkTime;
        this.inBetweenOpenThinkTime = inBetweenOpenThinkTime;
        this.inBetweenRequestThinkTime = inBetweenRequestThinkTime;
        this.observerMode = observerMode;
        this.connectWaitingTime = connectWaitingTime;
        this.testWaitingTime = testWaitingTime;
        this.handshakeConnectTime = handshakeConnectTime;
        this.forkJoinPool = new ForkJoinPool(forkJoinThread == -1 ? Runtime.getRuntime().availableProcessors() : forkJoinThread);
    }

    public AsyncHttpClientDriver(Properties p) {
        this(Integer.valueOf(p.getProperty("threads")),
                Integer.valueOf(p.getProperty("requestsPerThreadPerBatch", "1")),
                Integer.valueOf(p.getProperty("batches", "1")),
                Integer.valueOf(p.getProperty("warmUpRequests", "1")),
                Integer.valueOf(p.getProperty("payloadSize", "1")),
                Integer.valueOf(p.getProperty("threadIncrementPerBatch", "1")),
                Integer.valueOf(p.getProperty("errorAllowed", "1")),
                Integer.valueOf(p.getProperty("thinkTime", "5")),
                Integer.valueOf(p.getProperty("inBetweenOpenThinkTime", "1")),
                Integer.valueOf(p.getProperty("inBetweenRequestThinkTime", "1")),
                p.getProperty("url"),
                Boolean.valueOf(p.getProperty("observerMode")),
                Integer.valueOf(p.getProperty("connectWaitingTime", "5")),
                p.getProperty("to"),
                p.getProperty("from"),
                p.getProperty("transport"),
                p.getProperty("homeIp"),
                p.getProperty("appIp"),
                Integer.valueOf(p.getProperty("testWaitingTime", "20")),
                Integer.valueOf(p.getProperty("handshakeConnectTime", "10")),
                Integer.valueOf(p.getProperty("nettyConnectionTimeout", "5")),
                Integer.valueOf(p.getProperty("forkJoinThread", "-1")));
        ;
        logger.info("{}", p);
    }

    public BenchmarkResult run() throws InterruptedException {
        setup();

        logger.info("Beginning warmUp...");
        warmup();

        logger.info("WarmUp complete, running {} batches...", batches);

        List<BatchResult> results = new ArrayList<>(batches);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                // Allow CTRL-C
                logger.info("{}", new BenchmarkResult(threads, batches, results));
            }
        });

        for (int i = 0; i < batches; i++) {
            BatchResult result = runBatch();

            if (i != 0) {
                logger.info("Batch completed, resting for {} seconds.", thinkTime);
                TimeUnit.SECONDS.sleep(thinkTime);
            }

            logger.info("Batch {} using {} threads finished: {}", new String[]{String.valueOf(i), String.valueOf(threads), String.valueOf(result)});

            if (onError.getAndSet(false)) {
                if (errorAllowed != -1 && (threads - results.size()) > errorAllowed) {
                    logger.info("An error occurred. Ending the benchmark");
                    break;
                }

                if (i != 0) {
                    threads -= threadIncrementPerBatch;
                }
            }
            results.add(result);

            if (i != batches - 1) {
                threads += threadIncrementPerBatch;
            }
        }

        logger.info("Test finished, shutting down and calculating results...");
        tearDown();
        return new BenchmarkResult(threads, batches, results);
    }

    protected void setup() {
        NettyAsyncHttpProviderConfig nettyConfig = new NettyAsyncHttpProviderConfig();

        nettyConfig.addProperty("tcpNoDelay", "true");
        nettyConfig.addProperty("keepAlive", "true");
        nettyConfig.addProperty("reuseAddress", true);
        nettyConfig.addProperty("connectTimeoutMillis", nettyConnectionTimeout);
        nettyConfig.setWebSocketMaxFrameSize(65536);

        AsyncHttpClientConfig config = new AsyncHttpClientConfig.Builder()
                .setMaxConnections(-1)
                .setMaxConnectionsPerHost(-1)
                .setRequestTimeout(-1)
                .setConnectTimeout(nettyConnectionTimeout)
                .setWebSocketTimeout(-1)
                .setReadTimeout(-1)
                .setPooledConnectionIdleTimeout(-1)
                .setAllowPoolingConnections(false)
                .setWebSocketTimeout(-1)
                .setMaxRequestRetry(0)
                .setAcceptAnyCertificate(true)
                .setAsyncHttpClientProviderConfig(nettyConfig)
                .build();

        client = new AsyncHttpClient(config);
    }

    protected void tearDown() {
        forkJoinPool.shutdown();
        client.closeAsynchronously();
    }

    protected void warmup() {
        for (int i = 0; i < warmupRequests; i++) {
            try {
                client.prepareGet(url)
                        .addHeader("Sec-WebSocket-Protocol", "rpm-protocol")
                        .execute(new DriverHandler.Builder().build()).get(handshakeConnectTime, TimeUnit.MINUTES).close();
            } catch (Exception e) {
                extremeDebug("Failed warmup at iteration #" + i);
            }
        }
    }

    private void extremeDebug(Message m) {
        if (logger.isTraceEnabled()) {
            try {
                logger.trace("{}", mapper.writeValueAsString(m));
            } catch (JsonProcessingException e) {
                logger.error("", e);
            }
        }
    }

    private void extremeDebug(String m) {
        if (logger.isTraceEnabled()) {
            logger.trace("{}", m);
        }
    }

    protected BatchResult runBatch() {
        final CountDownLatch openLatch = new CountDownLatch(threads);
        final ConcurrentLinkedQueue<ThreadResult> threadResults = new ConcurrentLinkedQueue<>();
        long batchStart = System.nanoTime();

        final WebSocket[] sockets = new WebSocket[threads];
        long t = System.nanoTime();
        AtomicInteger connected = new AtomicInteger();
        final AtomicInteger failed = new AtomicInteger();
        logger.info("Connecting {} websockets to send {} requests", threads, requestsPerThreadPerBatch);

        for (int i = 0; i < threads; i++) {
            final AtomicInteger pos = new AtomicInteger(i);
            forkJoinPool.submit(() -> {
                try {
                    if (inBetweenOpenThinkTime > 0) {
                        extremeDebug("Posing before opening WebSocket " + pos.get());
                        TimeUnit.SECONDS.sleep(inBetweenOpenThinkTime);
                    }
                    sockets[pos.get()] = (client.prepareGet(url)
                            .addHeader("Sec-WebSocket-Protocol", "rpm-protocol")
                            .execute(new DriverHandler.Builder().build()).get(handshakeConnectTime, TimeUnit.MINUTES));
                    connected.incrementAndGet();
                    extremeDebug(pos.get() + " Opened WebSocket " + sockets[pos.get()]);
                } catch (Throwable e) {
                    failed.incrementAndGet();
                    logger.error("", e);
                } finally {
                    openLatch.countDown();
                }
            });
        }

        try {
            openLatch.await(connectWaitingTime, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.interrupted();
            onError.set(true);
            logger.error("", e);
        }

        if (failed.get() == threads) {
            logger.info("Unable to connect, server down or unreachable.");
            System.exit(-1);
        }

        logger.info("{} WebSockets connected in: {}", connected.get(), TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - t));

        final AtomicInteger count = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(threads);
        Future<?> f = debugScheduler.scheduleAtFixedRate(() -> {
            logger.info("Waiting for {} to complete", connected.get());
        }, 0, 1, TimeUnit.MINUTES);

        for (int i = 0; i < threads; i++) {

            // Failed to connect.
            if (sockets[i] == null) {
                count.getAndIncrement();
                extremeDebug("WebSocket is null: " + i);
                extremeDebug("Broken WebSocket: " + count.getAndIncrement());
                threadResults.add(new ThreadResult(requestsPerThreadPerBatch, 0, 0));
                latch.countDown();
                continue;
            }

            final AtomicInteger pos = new AtomicInteger(i);
            extremeDebug("Submitting " + pos.get() + " for execution");
            forkJoinPool.submit(() -> {
                        final AtomicInteger successful = new AtomicInteger();
                        long start = System.nanoTime();
                        try {
                            final WebSocket webSocket = sockets[pos.get()];
                            if (webSocket == null) {
                                count.getAndIncrement();
                                extremeDebug("WebSocket is null: " + pos.get());
                                extremeDebug("Broken WebSocket: " + count.getAndIncrement());
                                threadResults.add(new ThreadResult(requestsPerThreadPerBatch, 0, 0));
                                latch.countDown();
                                return;
                            }
                            final CountDownLatch allRequestDone = new CountDownLatch(requestsPerThreadPerBatch);
                            extremeDebug("Executing " + webSocket);

                            webSocket.addWebSocketListener(new WebSocketByteListener() {

                                private AtomicInteger extra = new AtomicInteger();

                                @Override
                                public void onMessage(byte[] bytes) {
                                    extremeDebug("Receiving message for " + webSocket);

                                    boolean success = true;
                                    Message message = null;
                                    try {
                                        message = mapper.readValue(bytes, Message.class);
                                    } catch (IOException e) {
                                        logger.error("", e);
                                        success = false;
                                    }
                                    extremeDebug(message);

                                    if (success) {
                                        successful.incrementAndGet();
                                    }
                                    extremeDebug("Received " + (threads - latch.getCount()) + " messages");
                                    allRequestDone.countDown();
                                    if (allRequestDone.getCount() == 0) {
                                        long totalTime = System.nanoTime() - start;
                                        threadResults.add(new ThreadResult(requestsPerThreadPerBatch, successful.get(), totalTime));
                                        latch.countDown();
                                    } else if (extra.incrementAndGet() > requestsPerThreadPerBatch) {
                                        logger.error("Received more request than requested!");
                                    }
                                }

                                @Override
                                public void onOpen(WebSocket websocket) {
                                }

                                @Override
                                public void onClose(WebSocket websocket) {
                                    extremeDebug("WebSocket closed " + pos.get());
                                    if (allRequestDone.getCount() != 0) {
                                        latch.countDown();
                                    }
                                }

                                @Override
                                public void onError(Throwable t) {
                                    logger.error("Error: websocket timed out by the server", t);
                                    latch.countDown();
                                }
                            });

                            if (!observerMode) {
                                for (int i1 = 0; i1 < requestsPerThreadPerBatch; i1++) {
                                    if (inBetweenRequestThinkTime > 0) {
                                        extremeDebug("Pausing WebSocket " + pos.get());
                                        TimeUnit.SECONDS.sleep(inBetweenRequestThinkTime);
                                    }
                                    extremeDebug("Sending message for WebSocket " + pos.get());
                                    try {
                                        webSocket.sendMessage(mapper.writeValueAsBytes(new Message(payload)));
                                    } catch (JsonProcessingException e) {
                                        logger.error("", e);
                                    }
                                }
                            }
                        } catch (Throwable e) {
                            logger.error("", e);
                            Thread.interrupted();
                            onError.set(true);
                        }
                    }
            );
        }

        try {
            if (!latch.await(testWaitingTime, TimeUnit.MINUTES)) {
                logger.error("WebSocket timed out after {} minutes", testWaitingTime);
                for (int i = 0; i < latch.getCount(); i++) {
                    threadResults.add(new ThreadResult(requestsPerThreadPerBatch, 0, TimeUnit.MINUTES.toMillis(testWaitingTime)));
                }
            }
        } catch (InterruptedException e) {
            Thread.interrupted();
            onError.set(true);
            logger.error("", e);
        }

        for (WebSocket w : sockets) {
            if (w != null) {
                w.close();
            }
        }

        f.cancel(true);

        long batchTotalTime = System.nanoTime() - batchStart;

        return new BatchResult(threadResults, batchTotalTime);
    }

    private static final class DriverHandler extends WebSocketUpgradeHandler {
        @Override
        public WebSocket onCompleted() throws Exception {
            try {
                return super.onCompleted();
            } catch (IllegalStateException ex) {
                logger.error("handshake failed, stopping", ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                return null;
            }
        }
    }
}
