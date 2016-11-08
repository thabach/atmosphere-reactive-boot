package com.yulplay.reactive.boot;

import com.yulplay.reactive.boot.impl.OnAnnotationProcessor;
import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.container.NettyCometSupport;
import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.inject.InjectableObjectFactory;
import org.atmosphere.nettosphere.Config;
import org.atmosphere.nettosphere.Nettosphere;
import org.atmosphere.util.SimpleBroadcaster;
import org.jboss.netty.handler.ssl.SslContext;
import org.jboss.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.TimeZone;

public class ReactiveBootstrap {
    public final static String YULPLAY_ROOT_PATH = "yulplay.root.path";
    public final static String YULPLAY_CONFIG_PATH = "yuly.config.path";

    private final static Logger logger = LoggerFactory.getLogger(ReactiveBootstrap.class);
    private final Config.Builder builder = new Config.Builder();
    private Nettosphere server;
    private URI uri;
    private boolean sslEnabled = false;

    public final static LocalDateTime UP_SINCE = LocalDateTime.now();

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        configurePaths();
    }

    public ReactiveBootstrap listenTo(URI uri) {
        this.uri = uri;
        return this;
    }

    public ReactiveBootstrap secure(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
        return this;
    }

    public ReactiveBootstrap initParam(String name, String value) {
        builder.initParam(name, value);
        return this;
    }

    public ReactiveBootstrap on() throws SSLException, CertificateException {

        builder.broadcaster(SimpleBroadcaster.class)
                .broadcasterCache(UUIDBroadcasterCache.class)
                .noInternalAlloc(true)
//                .binaryWrite(true)
                .initParam("noInternalAlloc", "true")
                .textFrameAsBinary(true)
//                .subProtocols(ReactivesWebSocketProcessor.PROTOCOL)
                .initParam(ApplicationConfig.DISABLE_ATMOSPHEREINTERCEPTOR, "true")
                        //               .initParam(ApplicationConfig.WEBSOCKET_IDLETIME, "600000")
                .initParam(ApplicationConfig.PROPERTY_COMET_SUPPORT, NettyCometSupport.class.getName())
                .initParam(ApplicationConfig.CUSTOM_ANNOTATION_PACKAGE, OnAnnotationProcessor.class.getPackage().getName())
                .initParam(ApplicationConfig.SCAN_CLASSPATH, "true")
                .initParam(ApplicationConfig.ANNOTATION_PACKAGE, getClass().getPackage().getName());


        if (sslEnabled) {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            SslContext sslCtx = SslContext.newServerContext(ssc.certificate(), ssc.privateKey());
            builder.sslContext(sslCtx);
        }

        if (uri == null) {
            uri = URI.create("http://0.0.0.0:8080");
        }

        builder.port(uri.getPort()).host(uri.getHost()).build();
        server = new Nettosphere.Builder().config(builder.build()).build();
        server.start();

        // We cannot inject RuntimeEngine as of Nettosphere 2.3.2
        ReactiveWebSocketFactory.class.cast(server.framework().webSocketFactory()).runtimeEngine(server.runtimeEngine());

        return this;
    }

    public ReactiveBootstrap off() {
        if (server != null) server.stop();

        return this;
    }


    public static void main(String[] args) throws IOException, CertificateException, InterruptedException {
        boolean sslEnabled = false;
        if (args != null && args.length > 0 && args[0].equalsIgnoreCase("--withSSL")) {
            sslEnabled = true;
        }

        ReactiveBootstrap z = new ReactiveBootstrap().secure(sslEnabled).on();

        logger.info("\n\n{}\n\n\tZone: {}\n\tRunning since: {}\n\tCopyright 2015-2016 Yulplay.com", YULY, TimeZone.getDefault().getID(), UP_SINCE);
        Thread.currentThread().join();
        // For Kafka to prevent key corruption
        z.off();

        Thread.sleep(1000);
        System.exit(-1);
    }

    /**
     * For testing purpose only.
     *
     * @return {@link EventBus}
     */
    protected EventBus eventBus() {
        return (InjectableObjectFactory.class.cast(server.framework().objectFactory())).getInjectable(EventBus.class);
    }

    /**
     * For testing purpose only.
     *
     * @return {@link EventBus}
     */
    protected Nettosphere nettosphere() {
        return server;
    }

    public final static void configurePaths() {

            if (System.getProperty(YULPLAY_ROOT_PATH) == null) {
                File root = new File("");
                logger.debug("Launch path {}", root.getAbsolutePath());
                File defaultLocation = new File(root.getAbsolutePath() + File.separator + "config");

                if (defaultLocation.exists()) {
                    logger.info("Found config.properties file {}", defaultLocation.getAbsolutePath());
                    System.setProperty(YULPLAY_ROOT_PATH, root.getAbsolutePath());
                    System.setProperty(YULPLAY_CONFIG_PATH, defaultLocation.getAbsolutePath());
                } else {
                    /**
                     * For testing/Debug only
                     */

                    logger.warn("System property " + YULPLAY_ROOT_PATH + " is undefined. Please make sure the property is defined if you run in production");

                    File f2 = new File(root.getAbsolutePath() + File.separator + ".." + File.separator + "protocol" + File.separator);
                    File f3 = new File(root.getAbsolutePath() + File.separator + ".." + File.separator);

                    if (f2.exists()) {
                        System.setProperty(YULPLAY_ROOT_PATH, f3.getAbsolutePath());
                        System.setProperty(YULPLAY_CONFIG_PATH, f3.getAbsolutePath() + "/reactive-boot/src/main/config");
                    } else if (!root.getAbsolutePath().endsWith("protocol")) {
                        System.setProperty(YULPLAY_ROOT_PATH, root.getAbsolutePath());
                        System.setProperty(YULPLAY_CONFIG_PATH, root.getAbsolutePath() + "/reactive-boot/src/main/config");
                    } else {
                        System.setProperty(YULPLAY_ROOT_PATH, f3.getAbsolutePath());
                        System.setProperty(YULPLAY_CONFIG_PATH, f3.getAbsolutePath() + "/reactive-boot/src/main/config");
                    }
                }
            } else {
                File root = new File(System.getProperty(YULPLAY_ROOT_PATH));
                logger.debug("Launch path {}", root.getAbsolutePath());
                if (System.getProperty(YULPLAY_CONFIG_PATH) == null) {
                    File defaultLocation = new File(root.getAbsolutePath() + File.separator + "config");
                    if (defaultLocation.exists()) {
                        logger.info("Found config.properties file {}", defaultLocation.getAbsolutePath());
                        System.setProperty(YULPLAY_CONFIG_PATH, defaultLocation.getAbsolutePath());
                    }
                }

            }
        }

        private final static String YULY =
                "\n" +
                        "  /\\  |                   |                |~~\\           | '       \n" +
                        " /__\\~|~|/~\\ /~\\ /~\\(~|~~\\|/~\\ /~/|/~\\/~/  |__//~//~~|/~~~|~|\\  //~/\n" +
                        "/    \\| |   |   |\\_/_)|__/|   |\\/_|   \\/_  |  \\\\/_\\__|\\__ | | \\/ \\/_";
}
 

