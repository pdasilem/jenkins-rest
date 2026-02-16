# jenkins-rest

Java client for working with Jenkins REST API, built on `java.net.http.HttpClient` (JDK 21+).

## Acknowledgment

This project is a fork of [cdancy/jenkins-rest](https://github.com/cdancy/jenkins-rest) v1.0.2
by [Christopher Dancy](https://github.com/cdancy), licensed under the
[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).

Changes from the original version (2.0.0+):
- Migrated from JDK 11 to JDK 21
- Replaced jclouds HTTP layer with `java.net.http.HttpClient`
- Replaced AutoValue domain classes with Java Records
- Removed Guice, Guava, javax.ws.rs, javax.inject dependencies
- Changed package namespace from `com.cdancy` to `com.pdasilem`
- **Version 2.1.0**: Refactored error handling — API methods now throw exceptions instead of returning `null`

## Setup

Client's can be built like so:
```java
// Using username:password authentication
JenkinsClient client = JenkinsClient.builder()
    .endPoint("http://127.0.0.1:8080") // Optional. Defaults to http://127.0.0.1:7990
    .credentials("admin:password")
    .build();

// Or using API token authentication (recommended)
JenkinsClient client = JenkinsClient.builder()
    .endPoint("http://127.0.0.1:8080")
    .apiToken("admin:your-api-token")
    .build();

SystemInfo systemInfo = client.api().systemApi().systemInfo();
System.out.println(systemInfo.jenkinsVersion());
```

## Latest release

Can be found in maven like so:
```xml
<dependency>
  <groupId>io.github.pdasilem</groupId>
  <artifactId>jenkins-rest</artifactId>
  <version>{version}</version>
</dependency>
```

## Property based setup

You can create a client without passing `endPoint` or `credentials` explicitly.
In this case the library will try to resolve them from **system properties** first, then from **environment variables**.

This is useful when you don't want to hardcode connection details in your code — for example, in CI/CD pipelines or Docker containers.

**Endpoint** (searched in order):

| Source | Key |
|---|---|
| System property | `jenkins.rest.endpoint` |
| Environment variable | `JENKINS_REST_ENDPOINT` |

Default (if none found): `http://127.0.0.1:7990`

**Authentication** (searched in order):

| Source | Key | Auth type |
|---|---|---|
| System property | `jenkins.rest.api.token` | API token |
| Environment variable | `JENKINS_REST_API_TOKEN` | API token |
| System property | `jenkins.rest.credentials` | username:password |
| Environment variable | `JENKINS_REST_CREDENTIALS` | username:password |

If none found, anonymous access is used.

**Example:**
```bash
# Via environment variables
export JENKINS_REST_ENDPOINT=http://my-jenkins:8080
export JENKINS_REST_API_TOKEN=admin:your-api-token

# Via JVM system properties
java -Djenkins.rest.endpoint=http://my-jenkins:8080 \
     -Djenkins.rest.api.token=admin:your-api-token \
     -jar my-app.jar
```

Then in code — no configuration needed:
```java
JenkinsClient client = JenkinsClient.builder().build();
// endpoint and credentials are resolved automatically
```

## Credentials

jenkins-rest credentials can take 1 of 3 forms:

- Colon delimited username and api token: __admin:apiToken__
  - use `JenkinsClient.builder().apiToken("admin:apiToken")`
- Colon delimited username and password: __admin:password__
  - use `JenkinsClient.builder().credentials("admin:password")`
- Base64 encoded username followed by password __YWRtaW46cGFzc3dvcmQ=__ or api token __YWRtaW46YXBpVG9rZW4=__
  - use `JenkinsClient.builder().credentials("YWRtaW46cGFzc3dvcmQ=")`
  - use `JenkinsClient.builder().apiToken("YWRtaW46YXBpVG9rZW4=")`

The Jenkins crumb is automatically requested when POSTing using the anonymous and the username:password authentication methods.
It is not requested when you use the apiToken as it is not needed in this case.
For more details, see [CSRF Protection on jenkins.io](https://www.jenkins.io/doc/book/security/csrf-protection/).

## Examples

The [mock](src/test/java/com/pdasilem/jenkins/rest/features) and [live](src/test/java/com/pdasilem/jenkins/rest/features) tests provide many examples
that you can use in your own code.

## Components

- **java.net.http.HttpClient** (JDK 21) — HTTP layer
- **Gson** — JSON serialization/deserialization
- **Java Records** — immutable domain classes

## Testing

Running mock tests can be done like so:

	./gradlew clean build mockTest

Running integration tests require an existing jenkins instance which can be obtained with docker:

	docker build -t jenkins-rest/jenkins src/main/docker
	docker run -d --rm -p 8080:8080 --name jenkins-rest jenkins-rest/jenkins
	./gradlew clean build integTest

### Integration tests settings

If you use the provided docker instance, there is no other preparation necessary.
If you wish to run integration tests against your own Jenkins server, the requirements are outlined in the next section.

#### Jenkins instance requirements

- a running instance accessible on http://127.0.0.1:8080 (can be changed in the gradle.properties file)
- Jenkins security
  - Authorization: Anyone can do anything (to be able to test the crumb with the anonymous account)
  - an `admin` user (credentials used by the tests can be changed in the gradle.properties file) with `ADMIN` role (required as the tests install plugins)
  - [CSRF protection enabled](https://www.jenkins.io/doc/book/security/csrf-protection/). Not mandatory but recommended by the Jenkins documentation. The lib supports Jenkins instances with or without this protection.
- Plugins
  - [CloudBees Credentials](https://plugins.jenkins.io/cloudbees-credentials)
  - [CloudBees Folder](https://plugins.jenkins.io/cloudbees-folder) plugin installed
  - [OWASP Markup Formatter](https://plugins.jenkins.io/antisamy-markup-formatter) configured to use `Safe HTML`
  - [Configuration As Code](https://plugins.jenkins.io/configuration-as-code) plugin installed
  - [Pipeline](https://plugins.jenkins.io/workflow-aggregator) plugin installed

This project provides instructions to setup a [pre-configured Docker container](src/main/docker/README.md)

#### Integration tests configuration

- jenkins url and authentication method used by the tests are defined in the `gradle.properties` file
- by default, tests use the `credentials` (username:password) authentication method but this can be changed to use the API Token. See the `gradle.properties` file.

#### Running integration tests from within your IDE

- the `integTest` gradle task sets various System Properties
- if you don't want to use gradle as tests runner in your IDE, configure the tests with the same kind of System Properties

# Additional Resources

* [Jenkins REST API](https://www.jenkins.io/doc/book/using/remote-access-api/)