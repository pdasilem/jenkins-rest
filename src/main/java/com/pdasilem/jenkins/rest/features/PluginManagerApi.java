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
import com.pdasilem.jenkins.rest.domain.plugins.Plugins;

import java.util.List;

public class PluginManagerApi {

    private final JenkinsHttpClient client;

    public PluginManagerApi(final JenkinsHttpClient client) {
        this.client = client;
    }

    public Plugins plugins(final Integer depth, final String tree) {
        try {
            final StringBuilder path = new StringBuilder("/pluginManager/api/json");
            String sep = "?";
            if (depth != null) {
                path.append(sep).append("depth=").append(depth);
                sep = "&";
            }
            if (tree != null) {
                path.append(sep).append("tree=").append(tree);
            }
            return client.get(path.toString(), Plugins.class);
        } catch (Exception e) {
            return Plugins.create(null, null,
                    List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    public RequestStatus installNecessaryPlugins(final String pluginID) {
        try {
            final String payload = "<jenkins><install plugin=\"" + pluginID + "\"/></jenkins>";
            client.postString("/pluginManager/installNecessaryPlugins", payload, "application/xml");
            return RequestStatus.create(true, null);
        } catch (Exception e) {
            return RequestStatus.create(false, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }
}
