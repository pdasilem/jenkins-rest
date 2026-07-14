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

import com.google.gson.Gson;
import com.pdasilem.jenkins.rest.auth.AuthenticationType;
import com.pdasilem.jenkins.rest.domain.common.Error;
import com.pdasilem.jenkins.rest.domain.crumb.Crumb;
import com.pdasilem.jenkins.rest.exception.JenkinsApiException;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class JenkinsHttpClient implements Closeable {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final JenkinsAuthentication auth;
    private final Gson gson;

    private static final String CRUMB_HEADER = "Jenkins-Crumb";
    private static final String CRUMB_ISSUER_PATH = "/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,%22:%22,//crumb)";

    private volatile CrumbData crumbData;

    public JenkinsHttpClient(final String baseUrl,
                             final JenkinsAuthentication auth,
                             final HttpClient httpClient) {
        this.baseUrl = Objects.requireNonNull(baseUrl).replaceAll("/$", "");
        this.auth = Objects.requireNonNull(auth);
        this.httpClient = Objects.requireNonNull(httpClient);
        this.gson = new Gson();
    }

    public <T> T get(final String path, final Class<T> responseType) {
        final HttpRequest request = newRequest(path).GET().build();
        return execute(request, responseType);
    }

    public <T> T get(final String path, final java.lang.reflect.Type responseType) {
        final HttpRequest request = newRequest(path).GET().build();
        return execute(request, responseType);
    }

    public String getString(final String path) {
        final HttpRequest request = newRequest(path).GET().build();
        return executeRaw(request);
    }

    public HttpResponse<String> getWithResponse(final String path) {
        final HttpRequest request = newRequest(path).GET().build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<InputStream> getStream(final String path) {
        final HttpRequest request = newRequest(path).GET().build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public HttpResponse<String> head(final String path) {
        final HttpRequest request = newRequest(path)
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public String postRaw(final String path) {
        final HttpRequest request = newRequestWithCrumb(path)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return executeRaw(request);
    }

    public HttpResponse<String> postRawWithResponse(final String path) {
        final HttpRequest request = newRequestWithCrumb(path)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public <T> T post(final String path, final String body, final String contentType, final Class<T> responseType) {
        final HttpRequest request = newRequestWithCrumb(path)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return execute(request, responseType);
    }

    public String postString(final String path, final String body, final String contentType) {
        final HttpRequest request = newRequestWithCrumb(path)
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return executeRaw(request);
    }

    public <T> T postForm(final String path, final Map<String, List<String>> formParams, final Class<T> responseType) {
        final String formBody = encodeFormParams(formParams);
        final HttpRequest request = newRequestWithCrumb(path)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();
        return execute(request, responseType);
    }

    public HttpResponse<String> postFormWithResponse(final String path, final Map<String, List<String>> formParams) {
        final String formBody = encodeFormParams(formParams);
        final HttpRequest request = newRequestWithCrumb(path)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(formBody))
                .build();
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public Gson gson() {
        return gson;
    }

    public JenkinsAuthentication auth() {
        return auth;
    }

    public Crumb fetchCrumb() {
        try {
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + CRUMB_ISSUER_PATH))
                    .GET();

            if (auth.authType() != AuthenticationType.Anonymous) {
                builder.header("Authorization", auth.authType().getAuthScheme() + " " + auth.authValue());
            }

            final HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            final int statusCode = response.statusCode();

            if (statusCode >= 200 && statusCode < 400) {
                final String body = response.body();
                final String crumbValue = body.split(":")[1];
                final String sessionIdCookie = response.headers()
                        .allValues("Set-Cookie").stream()
                        .filter(c -> c.startsWith(JenkinsConstants.JENKINS_COOKIES_JSESSIONID))
                        .findFirst()
                        .orElse(response.headers().allValues("set-cookie").stream()
                                .filter(c -> c.startsWith(JenkinsConstants.JENKINS_COOKIES_JSESSIONID))
                                .findFirst()
                                .orElse(""));
                return Crumb.create(crumbValue, sessionIdCookie);
            } else if (statusCode == 404) {
                return Crumb.create(null, List.of(Error.create(null, "crumb issuer not found (404)", "ResourceNotFoundException")));
            } else {
                return Crumb.create(null, List.of(Error.create(null, "HTTP " + statusCode, "HttpResponseException")));
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return Crumb.create(null, List.of(Error.create(null, e.getMessage(), e.getClass().getCanonicalName())));
        }
    }

    private HttpRequest.Builder newRequest(final String path) {
        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + normalizePath(path)));

        if (auth.authType() != AuthenticationType.Anonymous) {
            builder.header("Authorization", auth.authType().getAuthScheme() + " " + auth.authValue());
        }

        return builder;
    }

    private HttpRequest.Builder newRequestWithCrumb(final String path) {
        final HttpRequest.Builder builder = newRequest(path);

        if (auth.authType() == AuthenticationType.UsernamePassword
                || auth.authType() == AuthenticationType.Anonymous) {
            final CrumbData localCrumb = getCrumbData();
            if (localCrumb.crumb().value() != null) {
                builder.header(CRUMB_HEADER, localCrumb.crumb().value());
                final String sessionId = localCrumb.crumb().sessionIdCookie();
                if (sessionId != null && !sessionId.isEmpty()) {
                    builder.header("Cookie", sessionId);
                }
            } else {
                if (!localCrumb.isResourceNotFound()) {
                    throw new RuntimeException("Unexpected exception being thrown: error=" + localCrumb.crumb().errors().getFirst());
                }
            }
        }

        return builder;
    }

    private CrumbData getCrumbData() {
        CrumbData localData = this.crumbData;
        if (localData == null) {
            synchronized (this) {
                localData = this.crumbData;
                if (localData == null) {
                    final Crumb crumb = fetchCrumb();
                    final boolean isRNFE = crumb.errors().isEmpty()
                            || crumb.errors().getFirst().exceptionName().endsWith("ResourceNotFoundException");
                    this.crumbData = localData = new CrumbData(crumb, isRNFE);
                }
            }
        }
        return localData;
    }

    private <T> T execute(final HttpRequest request, final Class<T> responseType) {
        final String body = executeRaw(request);
        if (body == null || body.isEmpty()) {
            return null;
        }
        return gson.fromJson(body, responseType);
    }

    private <T> T execute(final HttpRequest request, final java.lang.reflect.Type responseType) {
        final String body = executeRaw(request);
        if (body == null || body.isEmpty()) {
            return null;
        }
        return gson.fromJson(body, responseType);
    }

    private String executeRaw(final HttpRequest request) {
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            final int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 400) {
                return response.body();
            }
            throw handleError(request, response);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private JenkinsApiException handleError(final HttpRequest request, final HttpResponse<String> response) {
        final String method = request.method();
        final String path = request.uri().getPath();
        final String uri = request.uri().toString();
        final String body = response.body();
        final int status = response.statusCode();
        final String message = body != null && !body.isEmpty()
                ? body
                : method + " " + uri + " -> " + status;

        return switch (status) {
            case 400 -> {
                if ("POST".equals(method) && path.contains("/createItem") && message.contains("A job already exists with the name")) {
                    yield new JenkinsApiException("Job already exists: " + message, status, body, method, uri);
                }
                yield new JenkinsApiException("Bad request: " + message, status, body, method, uri);
            }
            case 401 -> new JenkinsApiException("Authentication required: " + message, status, body, method, uri);
            case 403 -> new JenkinsApiException("Forbidden: " + message, status, body, method, uri);
            case 404 -> {
                if ("POST".equals(method)) {
                    if (path.endsWith("/term") || path.endsWith("/term/")) {
                        yield new JenkinsApiException("The term operation does not exist for " + uri + ", try stop instead.", status, body, method, uri);
                    } else if (path.endsWith("/kill") || path.endsWith("/kill/")) {
                        yield new JenkinsApiException("The kill operation does not exist for " + uri + ", try stop instead.", status, body, method, uri);
                    }
                }
                yield new JenkinsApiException("Resource not found: " + uri, status, body, method, uri);
            }
            case 405 -> new JenkinsApiException("Method not allowed: " + message, status, body, method, uri);
            case 409 -> new JenkinsApiException("Conflict: " + message, status, body, method, uri);
            case 415 -> new JenkinsApiException("Unsupported media type: " + message, status, body, method, uri);
            default -> new JenkinsApiException("HTTP " + status + ": " + message, status, body, method, uri);
        };
    }

    private static String normalizePath(final String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private static String encodeFormParams(final Map<String, List<String>> formParams) {
        if (formParams == null || formParams.isEmpty()) {
            return "";
        }
        return formParams.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .flatMap(entry -> entry.getValue().stream()
                        .map(value -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8)
                                + "=" + URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8)))
                .collect(Collectors.joining("&"));
    }

    @Override
    public void close() {
    }

    private record CrumbData(Crumb crumb, boolean isResourceNotFound) {
    }
}
