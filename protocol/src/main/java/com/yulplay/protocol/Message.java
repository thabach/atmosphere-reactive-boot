package com.yulplay.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class Message implements Serializable {

    private final String path;
    // home_ip
    private final String origin;
    // app_ip
    private final String destination;
    private byte[] body;

    private final String to;
    private final String from;
    private final String transport;
    private String uuid = UUID.randomUUID().toString();
    private String nodeUuid = "0";

    // Local unique ID of the websocket
    private String webSocketUUID = "0";

    public Message(@JsonProperty("path") String path,
                   @JsonProperty("origin") String origin,
                   @JsonProperty("destination") String destination,
                   @JsonProperty("body") byte[] body) {
        this.path = path;
        this.origin = origin;
        this.body = body;
        this.destination = destination;

        String[] paths = path.split("/");
        to = "/" + paths[1].toLowerCase();
        from = "/" + paths[2].toLowerCase();
        transport = "/" + paths[3].toLowerCase();
    }

    public Message(Message that) {
        this(that.path(), that.origin(), that.destination(), that.body());
    }

    public Message() {
        this("", "", "", new byte[]{});
    }

    @JsonProperty("path")
    public String path() {
        return path;
    }

    @JsonProperty("origin")
    public String origin() {
        return origin;
    }

    @JsonProperty("destination")
    public String destination() {
        return destination;
    }

    @JsonProperty("body")
    public byte[] body() {
        return body;
    }

    @JsonIgnore
    public String to() {
        return to;
    }

    @JsonIgnore
    public String from() {
        return from;
    }

    @JsonIgnore
    public String transport() {
        return transport;
    }

    @JsonProperty("webSocketUUID")
    public Message webSocketUUID(String webSocketUUID) {
        this.webSocketUUID = webSocketUUID;
        return this;
    }

    @JsonProperty("webSocketUUID")
    public String webSocketUUID() {
        return webSocketUUID;
    }

    @JsonProperty("uuid")
    public Message uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @JsonProperty("uuid")
    public String uuid() {
        return uuid;
    }

    @JsonProperty("nodeUuid")
    public Message nodeUuid(String nodeUuid) {
        this.nodeUuid = nodeUuid;
        return this;
    }

    @JsonProperty("nodeUuid")
    public String nodeUuid() {
        return nodeUuid;
    }

    public Message withBody(byte[] body) {
        this.body = body;
        return this;
    }
}
