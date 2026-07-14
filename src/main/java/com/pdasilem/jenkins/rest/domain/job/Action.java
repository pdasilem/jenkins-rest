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

package com.pdasilem.jenkins.rest.domain.job;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record Action(List<Cause> causes,
                     List<Parameter> parameters,
                     String text,
                     @SerializedName(value = "iconPath", alternate = {"icon"}) String iconPath,
                     @SerializedName("_class") String clazz) {

    public Action {
        causes = causes != null ? List.copyOf(causes) : List.of();
        parameters = parameters != null ? List.copyOf(parameters) : List.of();
    }

    public static Action create(final List<Cause> causes, final List<Parameter> parameters, final String text, final String iconPath, final String clazz) {
        return new Action(causes, parameters, text, iconPath, clazz);
    }
}
