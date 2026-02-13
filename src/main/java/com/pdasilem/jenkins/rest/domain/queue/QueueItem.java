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

package com.pdasilem.jenkins.rest.domain.queue;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public record QueueItem(boolean blocked, boolean buildable, int id, long inQueueSince,
                        @SerializedName("params") String rawParams, boolean stuck, Task task, String url,
                        String why, long buildableStartMilliseconds, boolean cancelled,
                        Executable executable, Long timestamp) {

    public Map<String, String> params() {
        Map<String, String> parameters = new HashMap<>();
        if (rawParams != null) {
            String trimmed = rawParams.trim();
            if (!trimmed.isEmpty()) {
                for (String keyValue : trimmed.split("\n")) {
                    String[] pair = keyValue.split("=", 2);
                    parameters.put(pair[0], pair.length > 1 ? pair[1] : "");
                }
            }
        }
        return parameters;
    }

    public static QueueItem create(boolean blocked, boolean buildable, int id, long inQueueSince, String params,
                                   boolean stuck, Task task, String url, String why, long buildableStartMilliseconds,
                                   boolean cancelled, Executable executable, Long timestamp) {
        return new QueueItem(blocked, buildable, id, inQueueSince, params, stuck, task, url, why,
                buildableStartMilliseconds, cancelled, executable, timestamp);
    }
}
