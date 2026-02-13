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

import com.pdasilem.jenkins.rest.auth.AuthenticationType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class BaseJenkinsTest {

    public static final String USERNAME_APITOKEN = "user:token";

    public BaseJenkinsTest() {
    }

    public JenkinsApi api(final URL url) {
        return api(url, AuthenticationType.UsernameApiToken, USERNAME_APITOKEN);
    }

    public JenkinsApi anonymousAuthApi(final URL url) {
        return api(url, AuthenticationType.Anonymous, AuthenticationType.Anonymous.name().toLowerCase());
    }

    public JenkinsApi api(final URL url, final AuthenticationType authType, final String authString) {
        final JenkinsAuthentication creds = creds(authType, authString);
        final HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        final JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(url.toString(), creds, httpClient);
        return new JenkinsApi(jenkinsHttpClient);
    }

    public JenkinsAuthentication creds(final AuthenticationType authType, final String authString) {
        final JenkinsAuthentication.Builder authBuilder = JenkinsAuthentication.builder();
        if (authType == AuthenticationType.UsernamePassword) {
            authBuilder.credentials(authString);
        } else if (authType == AuthenticationType.UsernameApiToken) {
            authBuilder.apiToken(authString);
        }
        return authBuilder.build();
    }

    public String payloadFromResource(String resource) {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream(resource))) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
