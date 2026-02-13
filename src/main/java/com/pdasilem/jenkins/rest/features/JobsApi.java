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
import com.google.gson.reflect.TypeToken;
import com.pdasilem.jenkins.rest.FolderPathHelper;
import com.pdasilem.jenkins.rest.JenkinsHttpClient;
import com.pdasilem.jenkins.rest.domain.common.Error;
import com.pdasilem.jenkins.rest.domain.common.LongResponse;
import com.pdasilem.jenkins.rest.domain.common.RequestStatus;
import com.pdasilem.jenkins.rest.domain.job.BuildInfo;
import com.pdasilem.jenkins.rest.domain.job.JobInfo;
import com.pdasilem.jenkins.rest.domain.job.JobList;
import com.pdasilem.jenkins.rest.domain.job.PipelineNode;
import com.pdasilem.jenkins.rest.domain.job.PipelineNodeLog;
import com.pdasilem.jenkins.rest.domain.job.ProgressiveText;
import com.pdasilem.jenkins.rest.domain.job.Workflow;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class JobsApi {

    private final JenkinsHttpClient client;

    public JobsApi(final JenkinsHttpClient client) {
        this.client = client;
    }

    public JobList jobList(final String folderPath) {
        try {
            return client.get("/" + FolderPathHelper.encode(folderPath) + "api/json", JobList.class);
        } catch (Exception e) {
            return null;
        }
    }

    public JobInfo jobInfo(final String optionalFolderPath, final String jobName) {
        try {
            return client.get("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/api/json", JobInfo.class);
        } catch (Exception e) {
            return null;
        }
    }

    public BuildInfo buildInfo(final String optionalFolderPath, final String jobName, final int buildNumber) {
        try {
            return client.get("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/api/json", BuildInfo.class);
        } catch (Exception e) {
            return null;
        }
    }

    public InputStream artifact(final String optionalFolderPath, final String jobName, final int buildNumber, final String relativeArtifactPath) {
        try {
            final HttpResponse<InputStream> resp = client.getStream("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/artifact/" + relativeArtifactPath);
            return resp.body();
        } catch (Exception e) {
            return null;
        }
    }

    public RequestStatus create(final String optionalFolderPath, final String jobName, final String configXML) {
        try {
            client.postString("/" + FolderPathHelper.encode(optionalFolderPath) + "createItem?name=" + jobName, configXML, "application/xml");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public String config(final String optionalFolderPath, final String jobName) {
        try {
            return client.getString("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/config.xml");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean config(final String optionalFolderPath, final String jobName, final String configXML) {
        try {
            client.postString("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/config.xml", configXML, "application/xml;charset=UTF-8");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String description(final String optionalFolderPath, final String jobName) {
        try {
            return client.getString("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/description");
        } catch (Exception e) {
            return null;
        }
    }

    public boolean description(final String optionalFolderPath, final String jobName, final String description) {
        try {
            var resp = client.postFormWithResponse("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/description",
                    Map.of("description", List.of(description)));
            return resp.statusCode() < 400;
        } catch (Exception e) {
            return false;
        }
    }

    public RequestStatus delete(final String optionalFolderPath, final String jobName) {
        try {
            client.postRaw("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/doDelete");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public boolean enable(final String optionalFolderPath, final String jobName) {
        try {
            client.postRaw("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/enable");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean disable(final String optionalFolderPath, final String jobName) {
        try {
            client.postRaw("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/disable");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public LongResponse build(final String optionalFolderPath, final String jobName) {
        try {
            final HttpResponse<String> resp = client.postRawWithResponse("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/build");
            if (resp.statusCode() >= 400) {
                return LongResponse.create(null, List.of(Error.create("HTTP " + resp.statusCode(), "", "HttpResponseException")));
            }
            return parseQueueId(resp);
        } catch (Exception e) {
            return LongResponse.create(null, List.of(Error.create(e.toString(), e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public RequestStatus stop(final String optionalFolderPath, final String jobName, final int buildNumber) {
        try {
            client.postRaw("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/stop");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public RequestStatus term(final String optionalFolderPath, final String jobName, final int buildNumber) {
        try {
            client.postRaw("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/term");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public RequestStatus kill(final String optionalFolderPath, final String jobName, final int buildNumber) {
        try {
            client.postRaw("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/kill");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public LongResponse buildWithParameters(final String optionalFolderPath, final String jobName, final Map<String, List<String>> properties) {
        try {
            final HttpResponse<String> resp = client.postFormWithResponse(
                    "/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/buildWithParameters",
                    properties);
            if (resp.statusCode() >= 400) {
                return LongResponse.create(null, List.of(Error.create("HTTP " + resp.statusCode(), "", "HttpResponseException")));
            }
            return parseQueueId(resp);
        } catch (Exception e) {
            return LongResponse.create(null, List.of(Error.create(e.toString(), e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public Integer lastBuildNumber(final String optionalFolderPath, final String jobName) {
        try {
            final String body = client.getString("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/lastBuild/buildNumber");
            return body != null ? Integer.parseInt(body.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public String lastBuildTimestamp(final String optionalFolderPath, final String jobName) {
        try {
            return client.getString("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/lastBuild/buildTimestamp");
        } catch (Exception e) {
            return null;
        }
    }

    public ProgressiveText progressiveText(final String optionalFolderPath, final String jobName, final int start) {
        try {
            final HttpResponse<String> resp = client.getWithResponse("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/lastBuild/logText/progressiveText?start=" + start);
            if (resp.statusCode() == 404) return null;
            return parseProgressiveText(resp);
        } catch (Exception e) {
            return null;
        }
    }

    public ProgressiveText progressiveText(final String optionalFolderPath, final String jobName, final int buildNumber, final int start) {
        try {
            final HttpResponse<String> resp = client.getWithResponse("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/logText/progressiveText?start=" + start);
            if (resp.statusCode() == 404) return null;
            return parseProgressiveText(resp);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean rename(final String optionalFolderPath, final String jobName, final String newName) {
        try {
            client.postRaw("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/doRename?newName=" + newName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Workflow> runHistory(final String optionalFolderPath, final String jobName) {
        try {
            return client.get("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/wfapi/runs",
                    new TypeToken<List<Workflow>>() {}.getType());
        } catch (Exception e) {
            return null;
        }
    }

    public Workflow workflow(final String optionalFolderPath, final String jobName, final int buildNumber) {
        try {
            return client.get("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/wfapi/describe", Workflow.class);
        } catch (Exception e) {
            return null;
        }
    }

    public PipelineNode pipelineNode(final String optionalFolderPath, final String jobName, final int buildNumber, final int nodeId) {
        try {
            return client.get("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/execution/node/" + nodeId + "/wfapi/describe", PipelineNode.class);
        } catch (Exception e) {
            return null;
        }
    }

    public PipelineNodeLog pipelineNodeLog(final String optionalFolderPath, final String jobName, final int buildNumber, final int nodeId) {
        try {
            return client.get("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/execution/node/" + nodeId + "/wfapi/log", PipelineNodeLog.class);
        } catch (Exception e) {
            return null;
        }
    }

    public JsonObject testReport(final String optionalFolderPath, final String jobName, final int buildNumber) {
        try {
            return client.get("/" + FolderPathHelper.encode(optionalFolderPath) + "job/" + jobName + "/" + buildNumber + "/testReport/api/json", JsonObject.class);
        } catch (Exception e) {
            return null;
        }
    }

    private LongResponse parseQueueId(final HttpResponse<String> resp) {
        final String location = resp.headers().firstValue("Location").orElse(null);
        if (location != null) {
            final String[] parts = location.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (!parts[i].isEmpty()) {
                    try {
                        return LongResponse.create(Long.parseLong(parts[i]), null);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        return LongResponse.create(null, List.of(Error.create(null,
                "No queue item Location header could be found despite getting a valid HTTP response.",
                NumberFormatException.class.getCanonicalName())));
    }

    private ProgressiveText parseProgressiveText(final HttpResponse<String> resp) {
        final String text = resp.body();
        final String textSize = resp.headers().firstValue("X-Text-Size").orElse("-1");
        final String moreData = resp.headers().firstValue("X-More-Data").orElse("false");
        return ProgressiveText.create(text, Integer.parseInt(textSize), Boolean.parseBoolean(moreData));
    }
}
