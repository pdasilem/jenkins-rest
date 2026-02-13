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
import org.testng.annotations.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.testng.Assert.assertEquals;

public class JenkinsAuthenticationMockTest {

    private static String b64(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    public void testAnonymousAuthentication() {
        JenkinsAuthentication ja = JenkinsAuthentication.builder().build();
        assertEquals(ja.identity(), "anonymous");
        assertEquals(ja.authType(), AuthenticationType.Anonymous);
        assertEquals(ja.authValue(), b64("anonymous:"));
    }

    @Test
    public void testUsernamePasswordAuthentication() {
        JenkinsAuthentication ja = JenkinsAuthentication.builder()
            .credentials("user:password")
            .build();
        assertEquals(ja.identity(), "user");
        assertEquals(ja.authType(), AuthenticationType.UsernamePassword);
        assertEquals(ja.authValue(), b64("user:password"));
    }

    @Test
    public void testUsernameApiTokenAuthentication() {
        JenkinsAuthentication ja = JenkinsAuthentication.builder()
            .apiToken("user:token")
            .build();
        assertEquals(ja.identity(), "user");
        assertEquals(ja.authType(), AuthenticationType.UsernameApiToken);
        assertEquals(ja.authValue(), b64("user:token"));
    }

    @Test
    public void testEncodedUsernamePasswordAuthentication() {
        String encoded = b64("user:password");
        JenkinsAuthentication ja = JenkinsAuthentication.builder()
            .credentials(encoded)
            .build();
        assertEquals(ja.identity(), "user");
        assertEquals(ja.authType(), AuthenticationType.UsernamePassword);
        assertEquals(ja.authValue(), encoded);
    }

    @Test
    public void testEncodedUsernameApiTokenAuthentication() {
        String encoded = b64("user:token");
        JenkinsAuthentication ja = JenkinsAuthentication.builder()
            .apiToken(encoded)
            .build();
        assertEquals(ja.identity(), "user");
        assertEquals(ja.authType(), AuthenticationType.UsernameApiToken);
        assertEquals(ja.authValue(), encoded);
    }

    @Test
    public void testEmptyUsernamePassword() {
        JenkinsAuthentication ja = JenkinsAuthentication.builder()
            .credentials(":")
            .build();
        assertEquals(ja.identity(), "");
        assertEquals(ja.authType(), AuthenticationType.UsernamePassword);
        assertEquals(ja.authValue(), b64(":"));
    }

    @Test
    public void testEmptyUsernameApiToken() {
        JenkinsAuthentication ja = JenkinsAuthentication.builder()
            .apiToken(":")
            .build();
        assertEquals(ja.identity(), "");
        assertEquals(ja.authType(), AuthenticationType.UsernameApiToken);
        assertEquals(ja.authValue(), b64(":"));
    }

    @Test
    public void testUndetectableCredential() {
        String invalid = b64("no_colon_here");
        try {
            JenkinsAuthentication.builder()
                .apiToken(invalid)
                .build();
        } catch (UndetectableIdentityException ex) {
          assertEquals(ex.getMessage(),
                "Unable to detect the identity being used in '" + invalid + "'. Supported types are a user:password, or a user:apiToken, or their base64 encoded value.");
        }
    }
}
