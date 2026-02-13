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
import com.pdasilem.jenkins.rest.exception.UndetectableIdentityException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class JenkinsAuthentication {

    private final String identity;
    private final String authValue;
    private final AuthenticationType authType;

    private JenkinsAuthentication(final String identity, final String credential, final AuthenticationType authType) {
        this.identity = identity;
        this.authValue = credential.contains(":")
                ? Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8))
                : credential;
        this.authType = authType;
    }

    public String identity() {
        return identity;
    }

    public String authValue() {
        return authValue;
    }

    public AuthenticationType authType() {
        return authType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String identity = "anonymous";
        private String credential = identity + ":";
        private AuthenticationType authType = AuthenticationType.Anonymous;

        public Builder credentials(final String usernamePassword) {
            this.identity = Objects.requireNonNull(extractIdentity(usernamePassword));
            this.credential = Objects.requireNonNull(usernamePassword);
            this.authType = AuthenticationType.UsernamePassword;
            return this;
        }

        public Builder apiToken(final String apiTokenCredentials) {
            this.identity = Objects.requireNonNull(extractIdentity(apiTokenCredentials));
            this.credential = Objects.requireNonNull(apiTokenCredentials);
            this.authType = AuthenticationType.UsernameApiToken;
            return this;
        }

        private String extractIdentity(final String credentialString) {
            String decoded;
            if (!credentialString.contains(":")) {
                decoded = new String(Base64.getDecoder().decode(credentialString), StandardCharsets.UTF_8);
            } else {
                decoded = credentialString;
            }
            if (!decoded.contains(":")) {
                throw new UndetectableIdentityException("Unable to detect the identity being used in '" + credentialString + "'. Supported types are a user:password, or a user:apiToken, or their base64 encoded value.");
            }
            if (decoded.equals(":")) {
                return "";
            }
            return decoded.split(":")[0];
        }

        public JenkinsAuthentication build() {
            return new JenkinsAuthentication(identity, credential, authType);
        }
    }
}
