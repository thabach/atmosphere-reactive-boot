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
package com.yulplay.reactive.boot.inject;

import com.yulplay.reactive.boot.impl.NodeUuid;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;

import java.lang.reflect.Type;

@ApplicationScoped
public class NodeUuidInjectable implements Injectable<NodeUuid> {

    private final NodeUuid nodeUuid = new NodeUuid();

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && NodeUuid.class.isAssignableFrom((Class) t);
    }

    @Override
    public NodeUuid injectable(AtmosphereConfig config) {
        return nodeUuid;
    }

}

