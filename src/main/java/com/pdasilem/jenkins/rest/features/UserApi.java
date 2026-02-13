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
import com.pdasilem.jenkins.rest.domain.user.ApiToken;
import com.pdasilem.jenkins.rest.domain.user.User;

import java.util.List;
import java.util.Map;

public class UserApi {

    private final JenkinsHttpClient client;

    public UserApi(final JenkinsHttpClient client) {
        this.client = client;
    }

    public User get() {
        try {
            final String userId = client.auth().identity();
            return client.get("/user/" + userId + "/api/json", User.class);
        } catch (Exception e) {
            return null;
        }
    }

    public ApiToken generateNewToken(final String tokenName) {
        try {
            final String userId = client.auth().identity();
            return client.postForm("/user/" + userId + "/descriptorByName/jenkins.security.ApiTokenProperty/generateNewToken",
                    Map.of("newTokenName", List.of(tokenName)), ApiToken.class);
        } catch (Exception e) {
            return null;
        }
    }

    public RequestStatus revoke(final String tokenUuid) {
        try {
            final String userId = client.auth().identity();
            client.postFormWithResponse("/user/" + userId + "/descriptorByName/jenkins.security.ApiTokenProperty/revoke",
                    Map.of("tokenUuid", List.of(tokenUuid)));
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }
}
