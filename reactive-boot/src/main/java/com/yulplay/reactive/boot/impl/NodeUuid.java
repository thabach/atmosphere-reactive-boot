package com.yulplay.reactive.boot.impl;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class NodeUuid {

    private final String uuidString;
    private final byte[] nodeUuid;

    public NodeUuid() {
        uuidString = UUID.randomUUID().toString();
        try {
            nodeUuid = uuidString.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String uuidString(){
        return uuidString;
    }

    public byte[] nodeUuid(){
        return nodeUuid;
    }
}
