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

import com.pdasilem.jenkins.rest.JenkinsHttpClient;
import com.pdasilem.jenkins.rest.domain.common.Error;
import com.pdasilem.jenkins.rest.domain.common.RequestStatus;

import java.util.List;

public class ConfigurationAsCodeApi {

    private final JenkinsHttpClient client;

    public ConfigurationAsCodeApi(final JenkinsHttpClient client) {
        this.client = client;
    }

    public RequestStatus check(final String cascYml) {
        try {
            client.postString("/configuration-as-code/check", cascYml, "application/x-yaml");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public RequestStatus apply(final String cascYml) {
        try {
            client.postString("/configuration-as-code/apply", cascYml, "application/x-yaml");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }
}
