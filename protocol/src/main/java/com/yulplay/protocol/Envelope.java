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
package com.yulplay.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.UUID;

public class Envelope implements Serializable {

    private String path;
    private String origin;
    private String destination;
    private byte[] body;
    private String uuid = UUID.randomUUID().toString();
    private String nodeUuid = "0";
    private String webSocketUUID = "0";

    public Envelope() {
    }

    @JsonProperty("path")
    public String path() {
        return path;
    }

    @JsonProperty("path")
    public Envelope withPath(String path) {
        this.path = path;
        return this;
    }

    @JsonProperty("origin")
    public String origin() {
        return origin;
    }

    @JsonProperty("origin")
    public Envelope withOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    @JsonProperty("destination")
    public String destination() {
        return destination;
    }

    @JsonProperty("destination")
    public Envelope withDestination(String destination) {
        this.destination = destination;
        return this;
    }

    @JsonProperty("body")
    public byte[] body() {
        return body;
    }

    @JsonProperty("body")
    public Envelope withBody(byte[] body) {
        this.body = body;
        return this;
    }

    @JsonProperty("webSocketUUID")
    public Envelope webSocketUUID(String webSocketUUID) {
        this.webSocketUUID = webSocketUUID;
        return this;
    }

    @JsonProperty("webSocketUUID")
    public String webSocketUUID() {
        return webSocketUUID;
    }

    @JsonProperty("uuid")
    public Envelope uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @JsonProperty("uuid")
    public String uuid() {
        return uuid;
    }

    @JsonProperty("nodeUuid")
    public Envelope nodeUuid(String nodeUuid) {
        this.nodeUuid = nodeUuid;
        return this;
    }

    @JsonProperty("nodeUuid")
    public String nodeUuid() {
        return nodeUuid;
    }

}
