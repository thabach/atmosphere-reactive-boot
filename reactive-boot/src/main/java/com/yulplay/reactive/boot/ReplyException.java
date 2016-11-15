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

import com.yulplay.protocol.Enveloppe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public final class ReplyException implements Serializable{

    private final Logger logger = LoggerFactory.getLogger(ReplyException.class);
    private Throwable throwable;
    private String errorMessage = "";
    private Enveloppe yulplayEnveloppe;

    public ReplyException() {
        this(null, null);
    }

    public ReplyException(Throwable throwable) {
        this.throwable = throwable;
    }

    public ReplyException(Throwable throwable, String error) {
        this.throwable = throwable;
        this.errorMessage = error;
    }

    public Throwable throwable() {
        return throwable;
    }

    public String error() {
        return errorMessage;
    }

    public ReplyException throwable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    public ReplyException error(String error) {
        this.errorMessage = error;
        return this;
    }

    public ReplyException message(Enveloppe yulplayEnveloppe) {
        this.yulplayEnveloppe = yulplayEnveloppe;
        return this;
    }

    public Enveloppe message(){
        return yulplayEnveloppe;
    }
}

