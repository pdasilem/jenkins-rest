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
import com.pdasilem.jenkins.rest.domain.job.BuildInfo;
import com.pdasilem.jenkins.rest.domain.queue.QueueItem;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@Test(groups = "live")
public class BaseJenkinsApiLiveTest {

    protected JenkinsApi api;
    protected final JenkinsAuthentication jenkinsAuthentication;
    private JenkinsClient client;

    public BaseJenkinsApiLiveTest() {
        this.jenkinsAuthentication = TestUtilities.inferTestAuthentication();
    }

    @BeforeClass
    public void setup() {
        final JenkinsClient.Builder builder = JenkinsClient.builder();
        if (jenkinsAuthentication.authType() == AuthenticationType.UsernamePassword) {
            builder.credentials(jenkinsAuthentication.identity() + ":" + jenkinsAuthentication.authValue());
        } else if (jenkinsAuthentication.authType() == AuthenticationType.UsernameApiToken) {
            builder.apiToken(jenkinsAuthentication.identity() + ":" + jenkinsAuthentication.authValue());
        }
        this.client = builder.build();
        this.api = client.api();
    }

    @AfterClass
    public void tearDown() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    protected String randomString() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public String payloadFromResource(String resource) {
        try (InputStream is = Objects.requireNonNull(getClass().getResourceAsStream(resource))) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected QueueItem getRunningQueueItem(long queueId) throws InterruptedException {
        int max = 10;
        QueueItem queueItem = api.queueApi().queueItem(queueId);
        while (max > 0) {
            if (queueItem.cancelled()) return null;
            if (queueItem.executable() != null) {
                return queueItem;
            }
            Thread.sleep(2000);
            queueItem = api.queueApi().queueItem(queueId);
            max = max - 1;
        }
        return queueItem;
    }

    protected BuildInfo getCompletedBuild(String jobName, QueueItem queueItem) throws InterruptedException {
        int max = 10;
        BuildInfo buildInfo = api.jobsApi().buildInfo(null, jobName, queueItem.executable().number());
        while (buildInfo.result() == null) {
            Thread.sleep(2000);
            buildInfo = api.jobsApi().buildInfo(null, jobName, queueItem.executable().number());
        }
        return buildInfo;
    }
}
