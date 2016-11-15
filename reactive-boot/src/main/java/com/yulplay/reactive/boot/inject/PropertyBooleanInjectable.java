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

import com.yulplay.reactive.boot.ConfigProperties;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.inject.InjectIntrospectorAdapter;

import javax.inject.Inject;
import javax.inject.Named;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

public class PropertyBooleanInjectable extends InjectIntrospectorAdapter<Boolean> {

    @Inject
    private ConfigProperties configProperties;

    private Boolean topic;

    @Override
    public void introspectField(Class clazz, Field field) {
        if (field.isAnnotationPresent(Named.class)) {
            String value = field.getAnnotation(Named.class).value();
            if (value.startsWith("yulplay.")) {
                // Do not override the value set by the class itself.
                if (configProperties.getString(value) != null) {
                    topic = configProperties.getBoolean(value);
                }
            }
        }
    }

    @Override
    public boolean supportedType(Type t) {
        return (t instanceof Class) && t.equals(boolean.class);
    }

    @Override
    public Boolean injectable(AtmosphereConfig config) {
        return topic;
    }
}
