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

import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.Injectable;
import org.atmosphere.inject.annotation.ApplicationScoped;

import java.lang.reflect.Type;
import java.util.concurrent.ForkJoinPool;

@ApplicationScoped
public class ForkJoinPoolInjectable implements Injectable<ForkJoinPool> {

    private final static ForkJoinPool forkJoinPool = new ForkJoinPool();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(forkJoinPool::shutdown));
    }

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && ForkJoinPool.class.isAssignableFrom((Class) t);
    }

    @Override
    public ForkJoinPool injectable(AtmosphereConfig config) {
        return forkJoinPool;
    }
}
