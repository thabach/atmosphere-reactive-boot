package com.yulplay.reactive.boot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.websocket.WebSocket;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;

public class TestUtil {

    public final static int findFreePort() throws IOException {
        ServerSocket socket = null;

        try {
            socket = new ServerSocket(0);

            return socket.getLocalPort();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    public final static void configurePaths(){
        File f = new File("");
        File f2 = new File(f.getAbsolutePath() + "/yulplay/src/main/config/config.properties");

        if (f2.exists()) {
            System.setProperty("ConfigProperties.path", f.getAbsolutePath() + "/yulplay/src/main/config/config.properties");
        }  else {
            System.setProperty("ConfigProperties.path", f.getAbsolutePath() + "/src/main/config/config.properties");
        }
    }

    public final static ArrayBaseWebSocket newWebSocket(OutputStream outputStream, AtmosphereFramework framework) {
        return new ArrayBaseWebSocket(outputStream, framework);
    }

    public final static class ArrayBaseWebSocket extends WebSocket {

        public final OutputStream outputStream;

        public ArrayBaseWebSocket(OutputStream outputStream, AtmosphereFramework framework) {
            super(framework.getAtmosphereConfig());
            this.outputStream = outputStream;
            this.attachment(new MessageReceiver(new ObjectMapper()));
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public WebSocket write(String s) throws IOException {
            outputStream.write(s.getBytes());
            return this;
        }

        @Override
        public WebSocket write(byte[] b, int offset, int length) throws IOException {
            outputStream.write(b, offset, length);
            return this;
        }

        @Override
        public void close() {
        }
    }
}
