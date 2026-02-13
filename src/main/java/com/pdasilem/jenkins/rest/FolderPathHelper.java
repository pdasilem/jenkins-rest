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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class FolderPathHelper {

    private static final String FOLDER_NAME_PREFIX = "job/";
    private static final char FOLDER_NAME_SEPARATOR = '/';

    private FolderPathHelper() {
        throw new UnsupportedOperationException();
    }

    public static String encode(final String folderPath) {
        if (folderPath == null || folderPath.isEmpty()) {
            return "";
        }

        final StringBuilder path = new StringBuilder(folderPath);

        if (path.charAt(0) == FOLDER_NAME_SEPARATOR) {
            path.deleteCharAt(0);
        }
        if (path.isEmpty()) {
            return "";
        }

        if (path.charAt(path.length() - 1) == FOLDER_NAME_SEPARATOR) {
            path.deleteCharAt(path.length() - 1);
        }
        if (path.isEmpty()) {
            return "";
        }

        final String[] folders = path.toString().split(Character.toString(FOLDER_NAME_SEPARATOR));
        path.setLength(0);
        for (final String folder : folders) {
            path.append(FOLDER_NAME_PREFIX).append(URLEncoder.encode(folder, StandardCharsets.UTF_8).replace("+", "%20")).append(FOLDER_NAME_SEPARATOR);
        }
        return path.toString();
    }
}
