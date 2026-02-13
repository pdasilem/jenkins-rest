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

import static com.pdasilem.jenkins.rest.JenkinsConstants.CREDENTIALS_ENVIRONMENT_VARIABLE;
import static com.pdasilem.jenkins.rest.JenkinsConstants.CREDENTIALS_SYSTEM_PROPERTY;
import static com.pdasilem.jenkins.rest.JenkinsConstants.DEFAULT_ENDPOINT;
import static com.pdasilem.jenkins.rest.JenkinsConstants.ENDPOINT_ENVIRONMENT_VARIABLE;
import static com.pdasilem.jenkins.rest.JenkinsConstants.ENDPOINT_SYSTEM_PROPERTY;
import static com.pdasilem.jenkins.rest.JenkinsConstants.API_TOKEN_ENVIRONMENT_VARIABLE;
import static com.pdasilem.jenkins.rest.JenkinsConstants.API_TOKEN_SYSTEM_PROPERTY;

public class JenkinsUtils {

    public static String retriveExternalValue(final String systemProperty,
            final String environmentVariable) {

        if (systemProperty != null) {
            final String value = System.getProperty(systemProperty);
            if (value != null) {
                return value;
            }
        }

        if (environmentVariable != null) {
            final String value = System.getenv().get(environmentVariable);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    public static String inferEndpoint() {
        final String possibleValue = JenkinsUtils
                .retriveExternalValue(ENDPOINT_SYSTEM_PROPERTY,
                        ENDPOINT_ENVIRONMENT_VARIABLE);
        return possibleValue != null ? possibleValue : DEFAULT_ENDPOINT;
    }

    public static JenkinsAuthentication inferAuthentication() {

        final JenkinsAuthentication.Builder inferAuth = JenkinsAuthentication.builder();
        String authValue = JenkinsUtils
                .retriveExternalValue(API_TOKEN_SYSTEM_PROPERTY,
                        API_TOKEN_ENVIRONMENT_VARIABLE);
        if (authValue != null) {
            inferAuth.apiToken(authValue);
            return inferAuth.build();
        }

        authValue = JenkinsUtils
                .retriveExternalValue(CREDENTIALS_SYSTEM_PROPERTY,
                        CREDENTIALS_ENVIRONMENT_VARIABLE);
        if (authValue != null) {
            inferAuth.credentials(authValue);
            return inferAuth.build();
        }

        return inferAuth.build();
    }

    protected JenkinsUtils() {
        throw new UnsupportedOperationException("Purposefully not implemented");
    }
}
