/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pdasilem.jenkins.rest.domain.plugins;

import com.pdasilem.jenkins.rest.domain.common.ErrorsHolder;
import com.pdasilem.jenkins.rest.domain.common.Error;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public record Plugins(@SerializedName("_class") String clazz,
                      List<Plugin> plugins,
                      List<Error> errors) implements ErrorsHolder {

    public Plugins {
        plugins = plugins != null ? List.copyOf(plugins) : List.of();
        errors = errors != null ? List.copyOf(errors) : List.of();
    }

    public static Plugins create(final String clazz,
                                 final List<Plugin> plugins,
                                 final List<Error> errors) {
        return new Plugins(clazz, plugins, errors);
    }
}
