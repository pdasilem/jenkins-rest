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

import com.google.gson.JsonObject;
import com.pdasilem.jenkins.rest.BaseJenkinsMockTest;
import com.pdasilem.jenkins.rest.JenkinsApi;
import com.pdasilem.jenkins.rest.domain.common.LongResponse;
import com.pdasilem.jenkins.rest.domain.common.RequestStatus;
import com.pdasilem.jenkins.rest.domain.job.BuildInfo;
import com.pdasilem.jenkins.rest.domain.job.Cause;
import com.pdasilem.jenkins.rest.domain.job.ChangeSet;
import com.pdasilem.jenkins.rest.domain.job.Job;
import com.pdasilem.jenkins.rest.domain.job.JobInfo;
import com.pdasilem.jenkins.rest.domain.job.JobList;
import com.pdasilem.jenkins.rest.domain.job.Parameter;
import com.pdasilem.jenkins.rest.domain.job.PipelineNode;
import com.pdasilem.jenkins.rest.domain.job.PipelineNodeLog;
import com.pdasilem.jenkins.rest.domain.job.ProgressiveText;
import com.pdasilem.jenkins.rest.domain.job.Workflow;
import com.pdasilem.jenkins.rest.exception.JenkinsApiException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Mock tests for the {@link JobsApi} class.
 */
@Test(groups = "unit", testName = "JobsApiMockTest")
public class JobsApiMockTest extends BaseJenkinsMockTest {

    public void testGetInnerFolderJobList() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/jobsInJenkinsFolder.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            JobList output = api.jobList("Folder1/Folder 2");
            assertNotNull(output);
            assertNotNull(output.jobs());
            assertEquals(output.jobs().size(), 1);
            assertEquals(output.jobs().getFirst(), Job.create("hudson.model.FreeStyleProject", "Test Project", "http://localhost:8080/job/username", null));
            assertSent(server, "GET", "/job/Folder1/job/Folder%202/api/json");
        } finally {
            server.shutdown();
        }
    }

    public void testGetRootFolderJobList() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/jobsInRootFolder.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            JobList output = api.jobList("");
            assertNotNull(output);
            assertNotNull(output.jobs());
            assertEquals(output.jobs().size(), 6);
            assertSent(server, "GET", "/api/json");
        } finally {
            server.shutdown();
        }
    }

    public void testGetJobInfo() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/job-info.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            JobInfo output = api.jobInfo(null, "fish");
            assertNotNull(output);
            assertEquals(output.name(), "fish");
            assertEquals(output.builds().size(), 7);
            assertSent(server, "GET", "/job/fish/api/json");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testGetJobInfoNotFound() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.jobInfo(null, "fish");
        } finally {
            server.shutdown();
        }
    }

    public void testGetBuildInfo() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/build-info.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            BuildInfo output = api.buildInfo(null, "fish", 10);
            assertNotNull(output);
            assertEquals(output.fullDisplayName(), "fish #10");
            assertEquals(output.artifacts().size(), 1);
            assertEquals(output.actions().size(), 5);
            assertEquals(output.actions().get(2).text(), "<strong>There could be HTML text here</strong>");
            assertEquals(output.actions().get(2).iconPath(), "clipboard.png");
            assertEquals(output.actions().get(2).clazz(), "com.jenkinsci.plugins.badge.action.BadgeSummaryAction");
            assertNull(output.actions().get(3).text());
            assertEquals(output.actions().get(4).clazz(), "org.jenkinsci.plugins.displayurlapi.actions.RunDisplayAction");
            assertSent(server, "GET", "/job/fish/10/api/json");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testGetBuildInfoNotFound() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.buildInfo(null, "fish", 10);
        } finally {
            server.shutdown();
        }
    }

    public void testGetArtifact() throws Exception {
        MockWebServer server = mockWebServer();

        String artifactContent = "artifact binary content";
        server.enqueue(new MockResponse().setBody(artifactContent).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            java.io.InputStream artifact = api.artifact(null, "fish", 10, "path/to/artifact.jar");
            assertNotNull(artifact);
            String content = new String(artifact.readAllBytes());
            assertEquals(content, artifactContent);
            assertSent(server, "GET", "/job/fish/10/artifact/path/to/artifact.jar");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testGetArtifactNotFound() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.artifact(null, "fish", 10, "path/to/missing.jar");
        } finally {
            server.shutdown();
        }
    }

    public void testCreateJob() throws Exception {
        MockWebServer server = mockWebServer();

        String configXML = payloadFromResource("/freestyle-project.xml");
        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus success = api.create(null, "DevTest", configXML);
            assertNotNull(success);
            assertTrue(success.value());
            assertTrue(success.errors().isEmpty());
            assertSentWithXMLFormDataAccept(server, "POST", "/createItem?name=DevTest", configXML, "*/*");
        } finally {
            server.shutdown();
        }
    }

    public void testCreateJobInFolder() throws Exception {
        MockWebServer server = mockWebServer();

        String configXML = payloadFromResource("/freestyle-project.xml");
        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus success = api.create("test-folder", "JobInFolder", configXML);
            assertNotNull(success);
            assertTrue(success.value());
            assertTrue(success.errors().isEmpty());
            assertSentWithXMLFormDataAccept(server, "POST", "/job/test-folder/createItem?name=JobInFolder", configXML, "*/*");
        } finally {
            server.shutdown();
        }
    }

    public void testSimpleFolderPathWithLeadingAndTrailingForwardSlashes() throws Exception {
        MockWebServer server = mockWebServer();

        String configXML = payloadFromResource("/freestyle-project.xml");
        server.enqueue(new MockResponse().setResponseCode(200));

        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus success = api.create("/test-folder/test-folder-1/", "JobInFolder", configXML);
            assertNotNull(success);
            assertTrue(success.value());
            assertTrue(success.errors().isEmpty());
            assertSentWithXMLFormDataAccept(server, "POST", "/job/test-folder/job/test-folder-1/createItem?name=JobInFolder", configXML, "*/*");
        } finally {
            server.shutdown();
        }
    }

    public void testCreateJobAlreadyExists() throws Exception {
        MockWebServer server = mockWebServer();

        String configXML = payloadFromResource("/freestyle-project.xml");
        server.enqueue(new MockResponse().setHeader("X-Error", "A job already exists with the name ?DevTest?")
            .setResponseCode(400));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus success = api.create(null, "DevTest", configXML);
            assertNotNull(success);
            assertFalse(success.value());
            assertFalse(success.errors().isEmpty());
            assertSentWithXMLFormDataAccept(server, "POST", "/createItem?name=DevTest", configXML, "*/*");
        } finally {
            server.shutdown();
        }
    }

    public void testGetDescription() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setBody("whatever").setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            String output = api.description(null, "DevTest");
            assertNotNull(output);
            assertEquals(output, "whatever");
            assertSentAcceptText(server, "GET", "/job/DevTest/description");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testGetDescriptionNonExistentJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.description(null, "DevTest");
        } finally {
            server.shutdown();
        }
    }

    public void testUpdateDescription() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            boolean success = api.description(null, "DevTest", "whatever");
            assertTrue(success);
            assertSentWithFormData(server, "POST", "/job/DevTest/description", "description=whatever",
                "text/html");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testUpdateDescriptionNonExistentJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.description(null, "DevTest", "whatever");
        } finally {
            server.shutdown();
        }
    }

    public void testGetConfig() throws Exception {
        MockWebServer server = mockWebServer();

        String configXML = payloadFromResource("/freestyle-project.xml");
        server.enqueue(new MockResponse().setBody(configXML).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            String output = api.config(null, "DevTest");
            assertNotNull(output);
            assertEquals(output, configXML);
            assertSentAcceptText(server, "GET", "/job/DevTest/config.xml");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testGetConfigNonExistentJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.config(null, "DevTest");
        } finally {
            server.shutdown();
        }
    }

    public void testUpdateConfig() throws Exception {
        MockWebServer server = mockWebServer();

        String configXML = payloadFromResource("/freestyle-project.xml");
        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            boolean success = api.config(null, "DevTest", configXML);
            assertTrue(success);
            assertSentAccept(server, "POST", "/job/DevTest/config.xml", "text/html");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testUpdateConfigNonExistentJob() throws Exception {
        MockWebServer server = mockWebServer();

        String configXML = payloadFromResource("/freestyle-project.xml");
        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.config(null, "DevTest", configXML);
        } finally {
            server.shutdown();
        }
    }

    public void testDeleteJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus success = api.delete(null, "DevTest");
            assertNotNull(success);
            assertTrue(success.value());
            assertTrue(success.errors().isEmpty());
            assertSentAccept(server, "POST", "/job/DevTest/doDelete", "text/html");
        } finally {
            server.shutdown();
        }
    }

    public void testDeleteJobNonExistent() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(400));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus success = api.delete(null, "DevTest");
            assertNotNull(success);
            assertFalse(success.value());
            assertFalse(success.errors().isEmpty());
            assertSentAccept(server, "POST", "/job/DevTest/doDelete", "text/html");
        } finally {
            server.shutdown();
        }
    }

    public void testEnableJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            boolean success = api.enable(null, "DevTest");
            assertTrue(success);
            assertSentAccept(server, "POST", "/job/DevTest/enable", "text/html");
        } finally {
            server.shutdown();
        }
    }

    public void testEnableJobAlreadyEnabled() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            boolean success = api.enable(null, "DevTest");
            assertTrue(success);
            assertSentAccept(server, "POST", "/job/DevTest/enable", "text/html");
        } finally {
            server.shutdown();
        }
    }

    public void testDisableJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            boolean success = api.disable(null, "DevTest");
            assertTrue(success);
            assertSentAccept(server, "POST", "/job/DevTest/disable", "text/html");
        } finally {
            server.shutdown();
        }
    }

    public void testDisableJobAlreadyEnabled() throws Exception {
        try (MockWebServer server = mockWebServer();
             JenkinsApi jenkinsApi = api(server.url("/").url())) {

            server.enqueue(new MockResponse().setResponseCode(200));
            JobsApi api = jenkinsApi.jobsApi();
            boolean success = api.disable(null, "DevTest");
            assertTrue(success);
            assertSentAccept(server, "POST", "/job/DevTest/disable", "text/html");
        }
    }

    public void testBuildJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(
            new MockResponse().setHeader("Location", "http://127.0.1.1:8080/queue/item/1/").setResponseCode(201));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            LongResponse output = api.build(null, "DevTest");
            assertNotNull(output);
            assertEquals((long) output.value(), 1);
            assertEquals(output.errors().size(), 0);
            assertSentAccept(server, "POST", "/job/DevTest/build", "application/unknown");
        } finally {
            server.shutdown();
        }
    }

    public void testBuildJobWithNoLocationReturned() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(
            new MockResponse().setResponseCode(201));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            LongResponse output = api.build(null, "DevTest");
            assertNotNull(output);
            assertNull(output.value());
            assertEquals(output.errors().size(), 1);
            assertNull(output.errors().getFirst().context());
            assertEquals(output.errors().getFirst().message(), "No queue item Location header could be found despite getting a valid HTTP response.");
            assertEquals(NumberFormatException.class.getCanonicalName(), output.errors().getFirst().exceptionName());
            assertSentAccept(server, "POST", "/job/DevTest/build", "application/unknown");
        } finally {
            server.shutdown();
        }
    }

    public void testBuildJobNonExistentJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            LongResponse output = api.build(null, "DevTest");
            assertNotNull(output);
            assertNull(output.value());
            assertEquals(output.errors().size(), 1);
            assertEquals(output.errors().getFirst().message(), "");
            assertNotNull(output.errors().getFirst().exceptionName());
            assertNotNull(output.errors().getFirst().context());
            assertSentAccept(server, "POST", "/job/DevTest/build", "application/unknown");
        } finally {
            server.shutdown();
        }
    }

    public void testBuildJobWithParams() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(
            new MockResponse().setHeader("Location", "http://127.0.1.1:8080/queue/item/1/").setResponseCode(201));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            Map<String, List<String>> params = new HashMap<>();
            params.put("SomeKey", List.of("SomeVeryNewValue"));
            LongResponse output = api.buildWithParameters(null, "DevTest", params);
            assertNotNull(output);
            assertEquals((long) output.value(), 1);
            assertEquals(output.errors().size(), 0);
            assertSentAccept(server, "POST", "/job/DevTest/buildWithParameters", "application/unknown");
        } finally {
            server.shutdown();
        }
    }

    public void testBuildJobWithNullParamsMap() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(
            new MockResponse().setHeader("Location", "http://127.0.1.1:8080/queue/item/1/").setResponseCode(201));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            LongResponse output = api.buildWithParameters(null, "DevTest", null);
            assertNotNull(output);
            assertEquals((long) output.value(), 1);
            assertEquals(output.errors().size(), 0);
            assertSentAccept(server, "POST", "/job/DevTest/buildWithParameters", "application/unknown");
        } finally {
            server.shutdown();
        }
    }

    public void testBuildJobWithEmptyParamsMap() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(
            new MockResponse().setHeader("Location", "http://127.0.1.1:8080/queue/item/1/").setResponseCode(201));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            LongResponse output = api.buildWithParameters(null, "DevTest", new HashMap<>());
            assertNotNull(output);
            assertEquals((long) output.value(), 1);
            assertEquals(output.errors().size(), 0);
            assertSentAccept(server, "POST", "/job/DevTest/buildWithParameters", "application/unknown");
        } finally {
            server.shutdown();
        }
    }

    public void testBuildJobWithParamsNonExistentJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            Map<String, List<String>> params = new HashMap<>();
            params.put("SomeKey", List.of("SomeVeryNewValue"));
            LongResponse output = api.buildWithParameters(null, "DevTest", params);
            assertNotNull(output);
            assertNull(output.value());
            assertEquals(output.errors().size(), 1);
            assertEquals(output.errors().getFirst().message(), "");
            assertNotNull(output.errors().getFirst().exceptionName());
            assertNotNull(output.errors().getFirst().context());
            assertSentAccept(server, "POST", "/job/DevTest/buildWithParameters", "application/unknown");
        } finally {
            server.shutdown();
        }
    }

    public void testGetParams() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/build-info.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            List<Parameter> output = api.buildInfo(null, "fish", 10).actions().getFirst().parameters();
            assertNotNull(output);
            assertEquals(output.get(0).name(), "bear");
            assertEquals(output.get(0).value(), "true");
            assertEquals(output.get(1).name(), "fish");
            assertEquals(output.get(1).value(), "salmon");
            assertSent(server, "GET", "/job/fish/10/api/json");
        } finally {
            server.shutdown();
        }
    }

    public void testGetGitCommitInfo() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/build-info-git-commit.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            List<ChangeSet> changeSets = api.buildInfo(null, "fish", 10).changeSets().getFirst().items();
            assertNotNull(changeSets);
            assertEquals(changeSets.getFirst().affectedPaths().getFirst(), "some/path/in/the/repository");
            assertEquals(changeSets.getFirst().commitId(), "d27afa0805201322d846d7defc29b82c88d9b5ce");
            assertEquals(changeSets.getFirst().timestamp(), 1461091892486L);
            assertEquals(changeSets.getFirst().author().absoluteUrl(), "http://localhost:8080/user/username");
            assertEquals(changeSets.getFirst().author().fullName(), "username");
            assertEquals(changeSets.getFirst().authorEmail(), "username@localhost");
            assertEquals(changeSets.getFirst().comment(), "Commit comment\n");
        } finally {
            server.shutdown();
        }
    }

    public void testGetParamsWhenNoBuildParams() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/build-info-no-params.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            List<Parameter> output = api.buildInfo(null, "fish", 10).actions().getFirst().parameters();
            assertEquals(output.size(), 0);
            assertSent(server, "GET", "/job/fish/10/api/json");
        } finally {
            server.shutdown();
        }
    }

    public void testGetParamsWhenEmptyorNullParams() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/build-info-empty-and-null-params.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            List<Parameter> output = api.buildInfo(null, "fish", 10).actions().getFirst().parameters();
            assertNotNull(output);
            assertEquals(output.get(0).name(), "bear");
            assertEquals(output.get(0).value(), "null");
            assertEquals(output.get(1).name(), "fish");
            assertTrue(output.get(1).value().isEmpty());
            assertSent(server, "GET", "/job/fish/10/api/json");
        } finally {
            server.shutdown();
        }
    }

    public void testGetCause() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/build-info-no-params.json");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            List<Cause> output = api.buildInfo(null, "fish", 10).actions().getFirst().causes();
            assertNotNull(output);
            assertEquals(output.getFirst().shortDescription(), "Started by user anonymous");
            assertNull(output.getFirst().userId());
            assertEquals(output.getFirst().userName(), "anonymous");
            assertSent(server, "GET", "/job/fish/10/api/json");
        } finally {
            server.shutdown();
        }
    }

    public void testGetLastBuildNumber() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/build-number.txt");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            Integer output = api.lastBuildNumber(null, "DevTest");
            assertNotNull(output);
            assertEquals((int) output, 123);
            assertSentAcceptText(server, "GET", "/job/DevTest/lastBuild/buildNumber");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testGetLastBuildNumberJobNotExist() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.lastBuildNumber(null, "DevTest");
        } finally {
            server.shutdown();
        }
    }

    public void testGetLastBuildTimeStamp() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/build-timestamp.txt");
        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            String output = api.lastBuildTimestamp(null, "DevTest");
            assertNotNull(output);
            assertEquals(body, output);
            assertSentAcceptText(server, "GET", "/job/DevTest/lastBuild/buildTimestamp");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testGetLastBuildTimeStampJobNotExist() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.lastBuildTimestamp(null, "DevTest");
        } finally {
            server.shutdown();
        }
    }

    public void testGetProgressiveText() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/progressive-text.txt");
        server.enqueue(new MockResponse().setHeader("X-Text-Size", "123").setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            ProgressiveText output = api.progressiveText(null, "DevTest", 0);
            assertNotNull(output);
            assertEquals(output.size(), 123);
            assertFalse(output.hasMoreData());
            assertSentAcceptText(server, "GET", "/job/DevTest/lastBuild/logText/progressiveText?start=0");
        } finally {
            server.shutdown();
        }
    }

    public void testGetProgressiveTextOfBuildNumber() throws Exception {
        MockWebServer server = mockWebServer();

        String body = payloadFromResource("/progressive-text.txt");
        server.enqueue(new MockResponse().setHeader("X-Text-Size", "123").setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            ProgressiveText output = api.progressiveText(null, "DevTest", 1, 0);
            assertNotNull(output);
            assertEquals(output.size(), 123);
            assertFalse(output.hasMoreData());
            assertSentAcceptText(server, "GET", "/job/DevTest/1/logText/progressiveText?start=0");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testGetProgressiveTextJobNotExist() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.progressiveText(null, "DevTest", 0);
        } finally {
            server.shutdown();
        }
    }

    public void testRenameJob() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            boolean success = api.rename(null, "DevTest", "NewDevTest");
            assertTrue(success);
            assertSentAccept(server, "POST", "/job/DevTest/doRename?newName=NewDevTest", "text/html");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testRenameJobNotExist() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.rename(null, "DevTest", "NewDevTest");
        } finally {
            server.shutdown();
        }
    }

    public void testRunHistory() throws Exception {
        MockWebServer server = mockWebServer();
        String body = payloadFromResource("/runHistory.json");

        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            List<Workflow> workflows = api.runHistory(null, "MockJob");
            assertNotNull(workflows);
            assertSent(server, "GET", "/job/MockJob/wfapi/runs");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testRunHistoryNotExist() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.runHistory(null, "MockJob");
        } finally {
            server.shutdown();
        }
    }

    public void testWorkflow() throws Exception {
        MockWebServer server = mockWebServer();
        String body = payloadFromResource("/workflow.json");

        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            Workflow success = api.workflow(null, "DevTest", 16);
            assertNotNull(success);
            assertSent(server, "GET", "/job/DevTest/16/wfapi/describe");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testWorkflowNotExist() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.workflow(null, "DevTest", 16);
        } finally {
            server.shutdown();
        }
    }

    public void testPipelineNode() throws Exception {
        MockWebServer server = mockWebServer();
        String body = payloadFromResource("/pipeline-node.json");

        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            PipelineNode success = api.pipelineNode(null, "DevTest", 16, 17);
            assertNotNull(success);
            assertSent(server, "GET", "/job/DevTest/16/execution/node/17/wfapi/describe");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testPipelineNodeNotExist() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.pipelineNode(null, "DevTest", 16, 17);
        } finally {
            server.shutdown();
        }
    }

    public void testJobTestReportExists() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setHeader("Content-Type", "application/json").setBody("{ \"empty\": false }").setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            JsonObject testReport = api.testReport(null, "DevTest", 16);
            assertNotNull(testReport);
            assertFalse(testReport.get("empty").getAsBoolean());
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testJobTestReportNotExists() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.testReport(null, "DevTest", 16);
        } finally {
            server.shutdown();
        }
    }

    public void testPipelineNodeLog() throws Exception {
        MockWebServer server = mockWebServer();
        String body = payloadFromResource("/pipelineNodeLog.json");

        server.enqueue(new MockResponse().setBody(body).setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            PipelineNodeLog pipelineNodeLog = api.pipelineNodeLog(null, "MockJob", 16, 17);
            assertNotNull(pipelineNodeLog);
            assertSent(server, "GET", "/job/MockJob/16/execution/node/17/wfapi/log");
        } finally {
            server.shutdown();
        }
    }

    @Test(expectedExceptions = JenkinsApiException.class)
    public void testPipelineNodeLogNotExist() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            api.pipelineNodeLog(null, "MockJob", 16, 17);
        } finally {
            server.shutdown();
        }
    }

    public void testStopBuild() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus status = api.stop(null, "fish", 99);
            assertNotNull(status);
            assertTrue(status.value());
            assertTrue(status.errors().isEmpty());
            assertSent(server, "POST", "/job/fish/99/stop");
        } finally {
            server.shutdown();
        }
    }

    public void testTermBuild() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus status = api.term(null, "fish", 99);
            assertNotNull(status);
            assertTrue(status.value());
            assertTrue(status.errors().isEmpty());
            assertSent(server, "POST", "/job/fish/99/term");
        } finally {
            server.shutdown();
        }
    }

    public void testKillBuild() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(200));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus status = api.kill(null, "fish", 99);
            assertNotNull(status);
            assertTrue(status.value());
            assertTrue(status.errors().isEmpty());
            assertSent(server, "POST", "/job/fish/99/kill");
        } finally {
            server.shutdown();
        }
    }

    public void testTermBuildReturns404() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus status = api.term(null, "fish", 99);
            assertSent(server, "POST", "/job/fish/99/term");
            assertNotNull(status);
            assertFalse(status.value());
            assertFalse(status.errors().isEmpty());
            assertEquals(status.errors().size(), 1);
            assertTrue(status.errors().getFirst().message().contains("term"));
            assertTrue(status.errors().getFirst().message().contains("try stop instead"));
        } finally {
            server.shutdown();
        }
    }

    public void testKillBuildReturns404() throws Exception {
        MockWebServer server = mockWebServer();

        server.enqueue(new MockResponse().setResponseCode(404));
        JenkinsApi jenkinsApi = api(server.url("/").url());
        try (jenkinsApi) {
            JobsApi api = jenkinsApi.jobsApi();
            RequestStatus status = api.kill(null, "fish", 99);
            assertSent(server, "POST", "/job/fish/99/kill");
            assertNotNull(status);
            assertFalse(status.value());
            assertFalse(status.errors().isEmpty());
            assertEquals(status.errors().size(), 1);
            assertTrue(status.errors().getFirst().message().contains("kill"));
            assertTrue(status.errors().getFirst().message().contains("try stop instead"));
        } finally {
            server.shutdown();
        }
    }
}
