package com.yulplay.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class Enveloppe implements Serializable {

    private String path;
    private String origin;
    private String destination;
    private byte[] body;
    private String uuid = UUID.randomUUID().toString();
    private String nodeUuid = "0";
    private String webSocketUUID = "0";

    public Enveloppe() {
    }

    @JsonProperty("path")
    public String path() {
        return path;
    }

    @JsonProperty("path")
    public Enveloppe withPath(String path) {
        this.path = path;
        return this;
    }

    @JsonProperty("origin")
    public String origin() {
        return origin;
    }

    @JsonProperty("origin")
    public Enveloppe withOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    @JsonProperty("destination")
    public String destination() {
        return destination;
    }

    @JsonProperty("destination")
    public Enveloppe withDestination(String destination) {
        this.destination = destination;
        return this;
    }

    @JsonProperty("body")
    public byte[] body() {
        return body;
    }

    @JsonProperty("body")
    public Enveloppe withBody(byte[] body) {
        this.body = body;
        return this;
    }

    @JsonProperty("webSocketUUID")
    public Enveloppe webSocketUUID(String webSocketUUID) {
        this.webSocketUUID = webSocketUUID;
        return this;
    }

    @JsonProperty("webSocketUUID")
    public String webSocketUUID() {
        return webSocketUUID;
    }

    @JsonProperty("uuid")
    public Enveloppe uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @JsonProperty("uuid")
    public String uuid() {
        return uuid;
    }

    @JsonProperty("nodeUuid")
    public Enveloppe nodeUuid(String nodeUuid) {
        this.nodeUuid = nodeUuid;
        return this;
    }

    @JsonProperty("nodeUuid")
    public String nodeUuid() {
        return nodeUuid;
    }

}
