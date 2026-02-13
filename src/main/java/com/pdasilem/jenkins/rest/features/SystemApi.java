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
import com.pdasilem.jenkins.rest.domain.system.SystemInfo;

import java.net.http.HttpResponse;
import java.util.List;

public class SystemApi {

    private final JenkinsHttpClient client;

    public SystemApi(final JenkinsHttpClient client) {
        this.client = client;
    }

    public SystemInfo systemInfo() {
        try {
            final HttpResponse<String> resp = client.head("/");
            if (resp.statusCode() >= 400) {
                return SystemInfo.create(null, null, null, null, null, null,
                        List.of(Error.create(null, "HTTP " + resp.statusCode(), "HttpResponseException")));
            }
            final String hudsonVersion = resp.headers().firstValue("X-Hudson").orElse("");
            final String jenkinsVersion = resp.headers().firstValue("X-Jenkins").orElse("");
            final String jenkinsSession = resp.headers().firstValue("X-Jenkins-Session").orElse("");
            final String instanceIdentity = resp.headers().firstValue("X-Instance-Identity").orElse("");
            final String sshEndpoint = resp.headers().firstValue("X-SSH-Endpoint").orElse(null);
            final String server = resp.headers().firstValue("Server").orElse("");
            return SystemInfo.create(hudsonVersion, jenkinsVersion, jenkinsSession,
                    instanceIdentity, sshEndpoint, server, null);
        } catch (Exception e) {
            return SystemInfo.create(null, null, null, null, null, null,
                    List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public RequestStatus quietDown() {
        try {
            client.postRaw("/quietDown");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public RequestStatus cancelQuietDown() {
        try {
            client.postRaw("/cancelQuietDown");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }
}
