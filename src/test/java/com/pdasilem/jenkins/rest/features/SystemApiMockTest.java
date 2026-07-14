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

import com.pdasilem.jenkins.rest.BaseJenkinsMockTest;
import com.pdasilem.jenkins.rest.JenkinsApi;
import com.pdasilem.jenkins.rest.domain.common.RequestStatus;
import com.pdasilem.jenkins.rest.domain.system.SystemInfo;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Mock tests for the {@link SystemApi} class.
 */
@Test(groups = "unit", testName = "SystemApiMockTest")
public class SystemApiMockTest extends BaseJenkinsMockTest {

    public void testGetSystemInfo() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(
            new MockResponse.Builder().setHeader("X-Hudson", "1.395").setHeader("X-Jenkins", "2.0")
                .setHeader("X-Jenkins-Session", "cc323b8d").setHeader("X-Hudson-CLI-Port", "50000")
                .setHeader("X-Jenkins-CLI-Port", "50000").setHeader("X-Jenkins-CLI2-Port", "50000")
                .setHeader("X-Instance-Identity", "fdsa").setHeader("X-SSH-Endpoint", "127.0.1.1:46126")
                .setHeader("Server", "Jetty(winstone-2.9)").code(200).build());
        JenkinsApi jenkinsApi = api(server.url("/").url());
        SystemApi api = jenkinsApi.systemApi();
        try {
            final SystemInfo version = api.systemInfo();
            assertNotNull(version);
            assertNotNull(version.jenkinsVersion());
            assertSent(server, "HEAD", "/");
        } finally {
            jenkinsApi.close();
            server.close();
        }
    }

    public void testGetSystemInfoOnError() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(
            new MockResponse.Builder().body("Not Authorized").code(401).build());
        JenkinsApi jenkinsApi = api(server.url("/").url());
        SystemApi api = jenkinsApi.systemApi();
        try {
            final SystemInfo version = api.systemInfo();
            assertNotNull(version);
            assertFalse(version.errors().isEmpty());
            assertSent(server, "HEAD", "/");
        } finally {
            jenkinsApi.close();
            server.close();
        }
    }

    public void testQuietDown() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse.Builder().code(200).build());
        JenkinsApi jenkinsApi = api(server.url("/").url());
        SystemApi api = jenkinsApi.systemApi();
        try {
            RequestStatus success = api.quietDown();
            assertNotNull(success);
            assertTrue(success.value());
            assertSentAccept(server, "POST", "/quietDown", "text/html");
        } finally {
            jenkinsApi.close();
            server.close();
        }
    }

    public void testQuietDownOnAuthException() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse.Builder().code(401).build());
        JenkinsApi jenkinsApi = api(server.url("/").url());
        SystemApi api = jenkinsApi.systemApi();
        try {
            RequestStatus status = api.quietDown();
            assertFalse(status.value());
            assertFalse(status.errors().isEmpty());
            assertSentAccept(server, "POST", "/quietDown", "text/html");
        } finally {
            jenkinsApi.close();
            server.close();
        }
    }

    public void testCancelQuietDown() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse.Builder().code(200).build());
        JenkinsApi jenkinsApi = api(server.url("/").url());
        SystemApi api = jenkinsApi.systemApi();
        try {
            RequestStatus success = api.cancelQuietDown();
            assertNotNull(success);
            assertTrue(success.value());
            assertSentAccept(server, "POST", "/cancelQuietDown", "text/html");
        } finally {
            jenkinsApi.close();
            server.close();
        }
    }

    public void testCancelQuietDownOnAuthException() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse.Builder().code(401).build());
        JenkinsApi jenkinsApi = api(server.url("/").url());
        SystemApi api = jenkinsApi.systemApi();
        try {
            RequestStatus status = api.cancelQuietDown();
            assertFalse(status.value());
            assertFalse(status.errors().isEmpty());
            assertSentAccept(server, "POST", "/cancelQuietDown", "text/html");
        } finally {
            jenkinsApi.close();
            server.close();
        }
    }
}
