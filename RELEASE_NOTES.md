### Version 3.0.0 (July 14, 2026)
* **BREAKING CHANGE**: Requires JDK 25 — the published bytecode target is now 25, so consumers on JDK 21 must stay on 2.2.0
* CHANGED: Build migrated to Gradle 9.6.1 (JDK 25 requires Gradle 9.1.0 or newer); Java toolchain set to 25
* CHANGED: Dependency versions are now declared in a Gradle version catalog (`gradle/libs.versions.toml`)
* CHANGED: Test suite migrated from the deprecated `okhttp3.mockwebserver` API to `mockwebserver3` (OkHttp 5.4.0)
* UPDATED: Gson 2.14.0, TestNG 7.12.0, AssertJ 3.27.7, Logback 1.5.38, JGit 7.7.0
* FIXED: Jenkins test image pinned to a core (2.401.3) older than its own plugins required, so `configuration-as-code` and `badge` could not load; now on 2.568.1-lts-jdk25
* FIXED: `buildWithParameters()` threw a `NullPointerException` when a parameter was mapped to a `null` value list
* FIXED: `UserApi.revoke()` reported success regardless of the HTTP status; it now returns an error for responses of 400 and above
* FIXED: `Action.iconPath()` returned `null` against modern Jenkins, because the badge plugin renamed the JSON field to `icon`; both names are now accepted
* FIXED: Replaced the deprecated `new URL(String)` constructor in the authentication live tests
* REMOVED: Deprecated `cloudbees-credentials` plugin from the test Jenkins image
* CI: Build matrix now runs on JDK 21 and 25; workflow trigger changed from `pull_request_target` to `pull_request`
* TESTS: Live tests now assert the exception-based contract introduced in 2.1.0 instead of the removed `null` returns, and the badge pipeline fixture uses the `addSummary` step required by badge plugin 3.x
* **Migration guide**:
  * Move your project to JDK 25. The public API is unchanged — 511 public members, identical signatures — so no source changes are required beyond the JDK itself.
  * If you call `UserApi.revoke()`, stop assuming it always succeeds: it now returns `RequestStatus.value() == false` with an error when Jenkins answers 400 or above.
  * If you run the integration tests against your own Jenkins, it must be on core 2.541.3 or newer and have the `badge` plugin installed.

### Version 2.2.0 (February 17, 2026)
* **BREAKING CHANGE**: Simplified exception hierarchy - removed all specialized HTTP exception classes
* CHANGED: All HTTP errors now throw `JenkinsApiException` with full error details (status code, body, method, URI)
* IMPROVED: `JenkinsApiException.getMessage()` now includes response body (truncated to 1000 chars) for better error visibility in logs
* IMPROVED: `JenkinsApiException.toString()` includes response body (truncated to 5000 chars) with total length indicator for better debugging
* FIXED: `JobsApi.artifact()` now throws `JenkinsApiException` on HTTP errors (404, 500, etc.) instead of returning error stream
* FIXED: Gradle deprecation warnings - migrated to Java toolchain API and fixed custom Test task configuration
* **Migration guide**: Replace `catch (ResourceNotFoundException e)` with `catch (JenkinsApiException e)` and check `e.statusCode() == 404`

### Version 2.1.0 (February 16, 2026)
* **BREAKING CHANGE**: Refactored error handling - API methods now throw exceptions instead of returning `null`
* ADDED: `JenkinsApiException` base exception with HTTP details (status code, body, method, URI)
* ADDED: `ResourceNotFoundException` for HTTP 404 errors
* CHANGED: `ForbiddenException`, `MethodNotAllowedException`, `RedirectTo404Exception`, `UnsupportedMediaTypeException` now extend `JenkinsApiException`
* CHANGED: `JobsApi` methods (`jobInfo`, `buildInfo`, `config`, `description`, `lastBuildNumber`, `progressiveText`, `rename`, `workflow`, `pipelineNode`, `testReport`, etc.) now throw exceptions on errors instead of returning `null`/`false`
* CHANGED: `UserApi.get()` and `generateNewToken()` now throw exceptions instead of returning `null`
* CHANGED: `QueueApi`, `StatisticsApi` methods now propagate exceptions instead of silently returning `null`
* IMPROVED: Error messages now include full HTTP context for better debugging
* **Migration guide**: Replace `if (result == null)` checks with `try-catch` blocks for `JenkinsApiException` or its subclasses

### Version 2.0.0 (February 12, 2026)
* **BREAKING CHANGE**: Complete rewrite and modernization of the library
* CHANGED: Migrated from JDK 11 to JDK 21
* CHANGED: Replaced jclouds HTTP layer with `java.net.http.HttpClient`
* CHANGED: Replaced AutoValue domain classes with Java Records (32 immutable domain classes)
* REMOVED: Guice dependency injection framework
* REMOVED: Guava utilities library
* REMOVED: javax.ws.rs and javax.inject dependencies
* CHANGED: Package namespace from `com.cdancy` to `com.pdasilem`
* CHANGED: `JenkinsApi` is now a concrete class (was interface)
* CHANGED: `JenkinsClient` uses builder pattern with `java.net.http.HttpClient`
* CHANGED: `JenkinsAuthentication` is now a concrete class with Builder pattern
* ADDED: `FolderPathHelper` for URL-encoding folder path segments
* IMPROVED: Simplified architecture with 8 concrete API classes in `features/` package
* IMPROVED: All 96 mock tests pass, full build succeeds

### Version 1.0.2 (September 29, 2022)
* ADDED: extensive build info. - [Pull Request 259](https://github.com/cdancy/jenkins-rest/pull/259)

### Version 1.0.1 (May 17, 2022)
* FIXED: Fix javadoc for all versions of Java. - [Pull Request 236](https://github.com/cdancy/jenkins-rest/pull/236)
* FIXED: Add charset to JobsApi. - [Pull Request 233](https://github.com/cdancy/jenkins-rest/pull/233)

### Version 1.0.0 (January 13, 2022)
* ADDED: `JobsApi` gained endpoints `stop`, `term`, and `kill`. - [Pull Request 207](https://github.com/cdancy/jenkins-rest/pull/207)
* ADDED: Fix `crumb` issue and create `UserApi` feature. - [Pull Request 195](https://github.com/cdancy/jenkins-rest/pull/195)

### Version 0.0.30 (November 1, 2021)
* Publish project to maven central.

### Version 0.0.29 (February 2, 2021)
* BUGFIX: Fix possible null `url` item int `Task`. - [Pull Request 144](https://github.com/cdancy/jenkins-rest/pull/144)

### Version 0.0.28 (January 1, 2021)
* BUGFIX: Fix null queue item task name. - [Pull Request 138](https://github.com/cdancy/jenkins-rest/pull/138)

### Version 0.0.27 (April 21, 2020)
* BUGFIX: Fix for crumb which was being held in a static field. - [Pull Request 88](https://github.com/cdancy/jenkins-rest/pull/88)

### Version 0.0.25 (February 11, 2020)
* ADDED: Add _color_ to Job. - [Pull Request 86](https://github.com/cdancy/jenkins-rest/pull/86)

### Version 0.0.24 (February 10, 2020)
* BUGFIX: Fix Nullable import - use jclouds version. - [Pull Request 84](https://github.com/cdancy/jenkins-rest/pull/84)

### Version 0.0.23 (January 27, 2020)
* ADDED: `JobsApi.jobList` endpoint. - [Pull Request 81](https://github.com/cdancy/jenkins-rest/pull/81)

### Version 0.0.22 (December 16, 2019)
* ADDED: Artifact `displayPath` is not mandatory anymore. - [Pull Request 77](https://github.com/cdancy/jenkins-rest/pull/77)
* ADDED: Fix crumb issue with Jenkins. - [Pull Request 70](https://github.com/cdancy/jenkins-rest/pull/70)

### Version 0.0.21 (December 2, 2019)
* ADDED: `JobsApi.progressiveText` with additional `buildNumber` param. - [Pull Request 74](https://github.com/cdancy/jenkins-rest/pull/74)

### Version 0.0.20 (October 8, 2019)
* ADDED: `PipelineApi`. - [Pull Request 64](https://github.com/cdancy/jenkins-rest/pull/64)
* ADDED: Bump various dependency versions.

### Version 0.0.19 (June 20, 2019)
* ADDED: 'message' property attached to `Error` object can now be null.

### Version 0.0.18 (April 2, 2019)
* ADDED: `SystemApi` gained endpoints `quietDown` and `cancelQuietDown`. - [Pull Request 54](https://github.com/cdancy/jenkins-rest/pull/54)

### Version 0.0.17 (March 18, 2019)
* ADDED: `JobsApi` gained endpoint `rename`. - [Pull Request 52](https://github.com/cdancy/jenkins-rest/pull/52)
* Bump `gradle` to `4.10.3`
* Bump `jclouds` to `2.1.2`

### Version 0.0.16 (January 4, 2018)
* FIX: `buildWithParameters` now supports a null parameter map for parameterized builds that do not override any params. - [Pull Request 43](https://github.com/cdancy/jenkins-rest/pull/43)
* Bump `gradle` to `4.10.2`
* Bump `shadow` plugin to `2.0.4`

### Version 0.0.15 (N/A)

### Version 0.0.14 (August 17, 2018)
* `JenkinsClient` now implements `Closeable` to better work with jdk8+ try-with-resources.
* Bump `jclouds` to `2.1.1`.
* Bump `AutoValue` to `1.6.2`
* Bump `gradle-bintray-plugin` to `1.8.4`
* Bump `gradle` to `4.9`

### Version 0.0.13 (July 26, 2018)
* ADDED: Endpoint `Jobs.buildInfo` now returns `Actions` as part of response. - [Pull Request 29](https://github.com/cdancy/jenkins-rest/pull/29)

### Version 0.0.12 (July 15, 2018)
* FIX: for when a build parameter is set to the empty string, the `QueueItem.create()` method should set the parameter value to the empty string.

### Version 0.0.11 (May 24, 2018)
* ADDED: all `JobsApi` endpoints gained the `optionalFolderPath` argument. - [Pull request 22](https://github.com/cdancy/jenkins-rest/pull/22)

### Version 0.0.10 (May 14, 2018)
* ADDED: `PluginManagerApi` with initial endpoint `plugins`. - [Pull request 17](https://github.com/cdancy/jenkins-rest/pull/17)
* REFACTOR: convert all endpoints which return an Integer into an `IntegerResposne` so that we can capture any errors. - [Pull request 18](https://github.com/cdancy/jenkins-rest/pull/18)
* ADDED: all endpoints within `JobsApi` that can take an optional folder path have been amended to provide an optional parameter to do so. - [Pull request 20](https://github.com/cdancy/jenkins-rest/pull/20)

### Version 0.0.9 (May 9, 2018)
* REFACTOR: don't assume "crumb validation" being enabled. - [Pull request 15](https://github.com/cdancy/jenkins-rest/pull/15)

### Version 0.0.8 (May 7, 2018)
* ADDED: Expose `modules` option in `JenkinsClient` builder. - [Pull request 12](https://github.com/cdancy/jenkins-rest/pull/12)

### Version 0.0.7 (April 26, 2018)
* BUG: Fix `QueueItem` incorrect parameter merging. - [Pull request 8](https://github.com/cdancy/jenkins-rest/pull/8)

### Version 0.0.6 (April 21, 2018)
* ADDED: `QueueApi` gained endpoints `cancel` and `queueItem`.

### Version 0.0.5 (April 18, 2018)
* REFACTOR: Do not throw exception when deprecated headers are missing.

### Version 0.0.4 (April 15, 2018)
* REFACTOR: various changes project wide to bring up-to-date with modern Jenkins.

### Version 0.0.3 (May 12, 2016)
* REFACTOR: JobsApi.build* endpoints will now return a 0 should a queueId not be handed back from Jenkins.

### Version 0.0.2 (April 19, 2016)
* ADDED: JobsApi.
* ADDED: QueueApi.

### Version 0.0.1 (April 14, 2016)
* init for jenkins-rest
* ADDED: SystemApi.
* ADDED: StatisticsApi.