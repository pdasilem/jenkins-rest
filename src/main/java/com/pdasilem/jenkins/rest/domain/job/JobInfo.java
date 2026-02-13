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

import com.pdasilem.jenkins.rest.domain.queue.QueueItem;

public record JobInfo(String description, String displayName, String displayNameOrNull,
                      String name, String url, boolean buildable, List<BuildInfo> builds,
                      String color, BuildInfo firstBuild, boolean inQueue, boolean keepDependencies,
                      BuildInfo lastBuild, BuildInfo lastCompleteBuild, BuildInfo lastFailedBuild,
                      BuildInfo lastStableBuild, BuildInfo lastSuccessfulBuild,
                      BuildInfo lastUnstableBuild, BuildInfo lastUnsuccessfulBuild,
                      int nextBuildNumber, QueueItem queueItem, boolean concurrentBuild) {

    public JobInfo {
        builds = builds != null ? List.copyOf(builds) : List.of();
    }

    public static JobInfo create(String description, String displayName, String displayNameOrNull, String name,
                                 String url, boolean buildable, List<BuildInfo> builds, String color, BuildInfo firstBuild, boolean inQueue,
                                 boolean keepDependencies, BuildInfo lastBuild, BuildInfo lastCompleteBuild, BuildInfo lastFailedBuild,
                                 BuildInfo lastStableBuild, BuildInfo lastSuccessfulBuild, BuildInfo lastUnstableBuild, BuildInfo lastUnsuccessfulBuild,
                                 int nextBuildNumber, QueueItem queueItem, boolean concurrentBuild) {
        return new JobInfo(description, displayName, displayNameOrNull, name, url, buildable,
                builds, color, firstBuild, inQueue, keepDependencies, lastBuild, lastCompleteBuild, lastFailedBuild,
                lastStableBuild, lastSuccessfulBuild, lastUnstableBuild, lastUnsuccessfulBuild, nextBuildNumber, queueItem, concurrentBuild);
    }
}
