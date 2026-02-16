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

package com.pdasilem.jenkins.rest.features;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.pdasilem.jenkins.rest.JenkinsHttpClient;
import com.pdasilem.jenkins.rest.domain.common.Error;
import com.pdasilem.jenkins.rest.domain.common.RequestStatus;
import com.pdasilem.jenkins.rest.domain.queue.QueueItem;

import java.util.List;
import java.util.Map;

public class QueueApi {

    private final JenkinsHttpClient client;

    public QueueApi(final JenkinsHttpClient client) {
        this.client = client;
    }

    public List<QueueItem> queue() {
        final JsonObject json = client.get("/queue/api/json", JsonObject.class);
        if (json != null && json.has("items")) {
            return client.gson().fromJson(json.getAsJsonArray("items"),
                    new TypeToken<List<QueueItem>>() {}.getType());
        }
        return List.of();
    }

    public QueueItem queueItem(final long queueId) {
        return client.get("/queue/item/" + queueId + "/api/json", QueueItem.class);
    }

    public RequestStatus cancel(final long id) {
        try {
            var resp = client.postFormWithResponse("/queue/cancelItem",
                    Map.of("id", List.of(String.valueOf(id))));
            if (resp.statusCode() >= 500) {
                return RequestStatus.create(false, List.of(
                        Error.create(null, "HTTP " + resp.statusCode(), "HttpResponseException")));
            }
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(
                    Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }
}
