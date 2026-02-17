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

package com.pdasilem.jenkins.rest.exception;

import java.io.Serial;

/**
 * Thrown when the Jenkins REST API returns an HTTP error response.
 * Contains the HTTP status code, response body, HTTP method, and request URI
 * so that callers can diagnose the problem without guessing.
 */
public class JenkinsApiException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int statusCode;
    private final String body;
    private final String httpMethod;
    private final String requestUri;

    public JenkinsApiException(final String message,
                               final int statusCode,
                               final String body,
                               final String httpMethod,
                               final String requestUri) {
        super(message);
        this.statusCode = statusCode;
        this.body = body;
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
    }

    public int statusCode() {
        return statusCode;
    }

    public String body() {
        return body;
    }

    public String httpMethod() {
        return httpMethod;
    }

    public String requestUri() {
        return requestUri;
    }

    @Override
    public String getMessage() {
        final String originalMessage = super.getMessage();
        if (body == null || body.isEmpty()) {
            return originalMessage;
        }
        final String truncatedBody = body.length() > 1000
            ? body.substring(0, 1000) + "... (truncated)"
            : body;
        return originalMessage + " | Response body: " + truncatedBody;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JenkinsApiException{");
        sb.append("statusCode=").append(statusCode);
        sb.append(", httpMethod='").append(httpMethod).append('\'');
        sb.append(", requestUri='").append(requestUri).append('\'');
        sb.append(", message='").append(super.getMessage()).append('\'');
        if (body != null && !body.isEmpty()) {
            final String truncatedBody = body.length() > 5000
                ? body.substring(0, 5000) + "... (truncated, total length: " + body.length() + ")"
                : body;
            sb.append(", body='").append(truncatedBody).append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
