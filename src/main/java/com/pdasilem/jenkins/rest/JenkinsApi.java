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

import com.pdasilem.jenkins.rest.features.ConfigurationAsCodeApi;
import com.pdasilem.jenkins.rest.features.CrumbIssuerApi;
import com.pdasilem.jenkins.rest.features.JobsApi;
import com.pdasilem.jenkins.rest.features.PluginManagerApi;
import com.pdasilem.jenkins.rest.features.QueueApi;
import com.pdasilem.jenkins.rest.features.StatisticsApi;
import com.pdasilem.jenkins.rest.features.SystemApi;
import com.pdasilem.jenkins.rest.features.UserApi;

import java.io.Closeable;
import java.io.IOException;

public class JenkinsApi implements Closeable {

    private final JenkinsHttpClient client;
    private final CrumbIssuerApi crumbIssuerApi;
    private final JobsApi jobsApi;
    private final PluginManagerApi pluginManagerApi;
    private final QueueApi queueApi;
    private final StatisticsApi statisticsApi;
    private final SystemApi systemApi;
    private final ConfigurationAsCodeApi configurationAsCodeApi;
    private final UserApi userApi;

    JenkinsApi(final JenkinsHttpClient client) {
        this.client = client;
        this.crumbIssuerApi = new CrumbIssuerApi(client);
        this.jobsApi = new JobsApi(client);
        this.pluginManagerApi = new PluginManagerApi(client);
        this.queueApi = new QueueApi(client);
        this.statisticsApi = new StatisticsApi(client);
        this.systemApi = new SystemApi(client);
        this.configurationAsCodeApi = new ConfigurationAsCodeApi(client);
        this.userApi = new UserApi(client);
    }

    public CrumbIssuerApi crumbIssuerApi() {
        return crumbIssuerApi;
    }

    public JobsApi jobsApi() {
        return jobsApi;
    }

    public PluginManagerApi pluginManagerApi() {
        return pluginManagerApi;
    }

    public QueueApi queueApi() {
        return queueApi;
    }

    public StatisticsApi statisticsApi() {
        return statisticsApi;
    }

    public SystemApi systemApi() {
        return systemApi;
    }

    public ConfigurationAsCodeApi configurationAsCodeApi() {
        return configurationAsCodeApi;
    }

    public UserApi userApi() {
        return userApi;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
