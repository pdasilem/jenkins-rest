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

package com.pdasilem.jenkins.rest;

import java.io.Closeable;
import java.io.IOException;
import java.net.http.HttpClient;

public final class JenkinsClient implements Closeable {

    private final JenkinsApi jenkinsApi;

    private JenkinsClient(final String endPoint,
                          final JenkinsAuthentication authentication) {
        final String resolvedEndPoint = endPoint != null
                ? endPoint
                : JenkinsUtils.inferEndpoint();
        final JenkinsAuthentication resolvedAuth = authentication != null
                ? authentication
                : JenkinsUtils.inferAuthentication();

        final HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        final JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(resolvedEndPoint, resolvedAuth, httpClient);
        this.jenkinsApi = new JenkinsApi(jenkinsHttpClient);
    }

    public JenkinsApi api() {
        return this.jenkinsApi;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void close() throws IOException {
        if (this.api() != null) {
            this.api().close();
        }
    }

    public static class Builder {

        private String endPoint;
        private JenkinsAuthentication.Builder authBuilder;

        public Builder endPoint(final String endPoint) {
            this.endPoint = endPoint;
            return this;
        }

        public Builder credentials(final String optionallyBase64EncodedCredentials) {
            authBuilder = JenkinsAuthentication.builder()
                    .credentials(optionallyBase64EncodedCredentials);
            return this;
        }

        public Builder apiToken(final String apiToken) {
            authBuilder = JenkinsAuthentication.builder()
                    .apiToken(apiToken);
            return this;
        }

        public JenkinsClient build() {
            final JenkinsAuthentication authentication = authBuilder != null
                    ? authBuilder.build()
                    : null;
            return new JenkinsClient(endPoint, authentication);
        }
    }
}
