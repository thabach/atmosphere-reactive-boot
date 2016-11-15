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
package com.yulplay.reactive.boot.impl;

import com.yulplay.reactive.boot.EventBus;
import com.yulplay.reactive.boot.Service;
import com.yulplay.reactive.boot.annotation.On;
import org.atmosphere.annotation.Processor;
import org.atmosphere.config.AtmosphereAnnotation;
import org.atmosphere.cpr.AtmosphereFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

@AtmosphereAnnotation(On.class)
public class OnAnnotationProcessor implements Processor<Service> {

    @Inject
    private EventBus eventBus;
    private Logger logger = LoggerFactory.getLogger(OnAnnotationProcessor.class);

    @Override
    public void handle(AtmosphereFramework framework, Class<Service> annotatedClass) {

        On s = annotatedClass.getAnnotation(On.class);
        try {
            logger.info("Registering @On annotation {}", annotatedClass.getName());
            String[] values = s.value();
            Service service = framework.newClassInstance(Service.class, annotatedClass);
            for (String v : values) {
                eventBus.on(v, service);
            }
        } catch (Exception e) {
            logger.error("Unable to register {}", annotatedClass, e);
        }
    }
}
