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
    public String toString() {
        return "JenkinsApiException{" +
                "statusCode=" + statusCode +
                ", httpMethod='" + httpMethod + '\'' +
                ", requestUri='" + requestUri + '\'' +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}
