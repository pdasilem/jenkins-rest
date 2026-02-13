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

package com.pdasilem.jenkins.rest.domain.job;

import java.util.List;

public record BuildInfo(List<Artifact> artifacts, List<Action> actions,
                        boolean building, String description, String displayName,
                        long duration, long estimatedDuration, String fullDisplayName,
                        String id, boolean keepLog, int number, int queueId,
                        String result, long timestamp, String url,
                        List<ChangeSetList> changeSets, String builtOn,
                        List<Culprit> culprits) {

    public BuildInfo {
        artifacts = artifacts != null ? List.copyOf(artifacts) : List.of();
        actions = actions != null ? List.copyOf(actions) : List.of();
        changeSets = changeSets != null ? List.copyOf(changeSets) : List.of();
        culprits = culprits != null ? List.copyOf(culprits) : List.of();
    }

    public static BuildInfo create(List<Artifact> artifacts, List<Action> actions, boolean building, String description, String displayName,
                                   long duration, long estimatedDuration, String fullDisplayName, String id, boolean keepLog, int number,
                                   int queueId, String result, long timestamp, String url, List<ChangeSetList> changeSets, String builtOn, List<Culprit> culprits) {
        return new BuildInfo(artifacts, actions, building, description, displayName, duration, estimatedDuration, fullDisplayName,
                id, keepLog, number, queueId, result, timestamp, url, changeSets, builtOn, culprits);
    }
}
