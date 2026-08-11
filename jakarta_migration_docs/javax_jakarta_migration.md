# javax → jakarta Migration Notes (JVM 11 → JVM 17)

## 1. Changed Dependencies by Module

### Root / BOM level
| Before | After |
|---|---|
| `com.sun.xml.bind:*` (JAXB RI) | `org.glassfish.jaxb:jaxb-runtime` |
| `com.fasterxml.jackson.module:jackson-module-jaxb-annotations` | `com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations` |
| `com.github.tomakehurst:wiremock-jre8` | `org.wiremock:wiremock-jetty12` 3.13.2 |

### `kie-server-controller-rest`
| Before | After |
|---|---|
| `io.swagger:swagger-annotations` | `io.swagger.core.v3:swagger-annotations` |

### `kie-server-controller-standalone`
| Before | After |
|---|---|
| `io.swagger:swagger-jaxrs` (+ exclusion of `javax.ws.rs:jsr311-api`) | `io.swagger.core.v3:swagger-jaxrs2-jakarta` |
| `io.swagger:swagger-core` | `io.swagger.core.v3:swagger-core-jakarta` |
| `io.swagger:swagger-annotations` | `io.swagger.core.v3:swagger-annotations` |

### `kie-server-rest-common`, `kie-server-rest-case-mgmt`
| Before | After |
|---|---|
| `io.swagger:swagger-annotations` | `io.swagger.core.v3:swagger-annotations` |
| *(not present)* | `org.jboss.resteasy:resteasy-core` (runtime scope — needed for `RuntimeDelegate` test) |

### `kie-server-rest-dmn`, `kie-server-rest-drools`, `kie-server-rest-jbpm`, `kie-server-rest-jbpm-ui`, `kie-server-rest-prometheus`, `kie-server-rest-scenario-simulation`
| Before | After |
|---|---|
| `io.swagger:swagger-annotations` | `io.swagger.core.v3:swagger-annotations` |

### `kie-server-rest-swagger`
| Before | After |
|---|---|
| `io.swagger:swagger-jaxrs` (+ exclusion of `jsr311-api`) | `io.swagger.core.v3:swagger-jaxrs2-jakarta` |
| `io.swagger:swagger-core` | `io.swagger.core.v3:swagger-core-jakarta` |
| `io.swagger:swagger-annotations` | `io.swagger.core.v3:swagger-annotations` |
| *(not present)* | `io.swagger.core.v3:swagger-models-jakarta` |

### `kie-server-services-swagger`
| Before | After |
|---|---|
| `io.swagger:swagger-jaxrs` (+ exclusion of `jsr311-api`) | `io.swagger.core.v3:swagger-core-jakarta` |
| `io.swagger:swagger-core` | `io.swagger.core.v3:swagger-models-jakarta` |
| `io.swagger:swagger-models` | `io.swagger.core.v3:swagger-jaxrs2-jakarta` |

### `kie-server-services-jbpm`
| Before | After |
|---|---|
| `org.hibernate:hibernate-core` (test scope only) | `org.hibernate:hibernate-core` (compile scope — needed for new XStream adapters) |
| `com.thoughtworks.xstream:xstream-hibernate` excluded `org.glassfish.jaxb:jaxb-runtime` | Exclusion removed (no longer collides) |

### `kie-server-integ-tests-all`
| Before | After |
|---|---|
| `io.swagger:swagger-parser` | `io.swagger.parser.v3:swagger-parser-v3` |

### `kie-server-integ-tests-dmn`
| Before | After |
|---|---|
| `swagger-parser` | `swagger-parser-v3` |

### `kie-spring-boot-samples` (parent + `*-integ-tests-sample`, `*-kafka-sample`, `*-sample`)
| Before | After |
|---|---|
| `io.swagger:swagger-jaxrs` 1.5.15 (+ exclusion of `jsr311-api`) | `io.swagger.core.v3:swagger-jaxrs2-jakarta` |

### WireMock consumers: `kie-camel`, `kie-karaf-itests`, `kie-server-client`, `kie-server-router-client`, `kie-server-integ-tests-router`
| Before | After |
|---|---|
| `com.github.tomakehurst:wiremock-jre8` | `org.wiremock:wiremock-jetty12` 3.13.2 |

---

## 2. Code Changes by Module

### Namespace renames (global, applied across all source files)
| Old import | New import |
|---|---|
| `javax.inject.*` | `jakarta.inject.*` |
| `javax.ws.rs.*` | `jakarta.ws.rs.*` |
| `javax.persistence.*` | `jakarta.persistence.*` (3.1) |
| `javax.validation.*` | `jakarta.validation.*` |
| `javax.transaction.*` | `jakarta.transaction.*` |
| `javax.enterprise.*` | `jakarta.enterprise.*` |
| `javax.websocket.*` | `jakarta.websocket.*` |
| `javax.mail.*` | `jakarta.mail.*` |
| `javax.xml.ws.*` / `javax.jws.*` | `jakarta.xml.ws.*` / `jakarta.jws.*` |
| `javax.security.auth.Subject.container` key (string literal) | Constant `JACCIdentityProvider.SECURITY_AUTH_SUBJECT_CONTAINER = "javax.security.auth.Subject.container"` |
| `com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule` | `com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule` |
| Removed `org.hibernate:hibernate-entitymanager` (merged into `hibernate-core`) | — |

### Swagger v2 → v3 / OpenAPI annotations (all REST resource classes)
> Affects: `RestRuntimeManagementServiceImpl`, `RestSpecManagementServiceImpl`, all `kie-server-rest-*` `*Resource.java` files (case-mgmt, common, dmn, drools, jbpm, jbpm-ui, prometheus, scenario-simulation)

| Old (`io.swagger.annotations`) | New (`io.swagger.v3.oas.annotations`) |
|---|---|
| `@Api` | `@Tag` |
| `@ApiOperation(value=…)` | `@Operation(description=…)` |
| `@ApiParam` | `@Parameter` |
| `@ApiResponse` | `@responses.ApiResponse` |
| `@ApiResponses` | `@responses.ApiResponses` |
| `@Example` / `@ExampleProperty` | `@media.ExampleObject` inside `@media.Content` |
| `responseContainer` attribute | Removed; use `@ArraySchema` |
| `allowableValues` attribute | Use `@Schema(allowableValues=…)` |
| `allowMultiple` attribute | Removed |
| `@ApiResponse(response=…, responseHeaders=…)` | `@ApiResponse(content=@Content(…))` |
| `@PathParam` with string literal | `@PathParam` with constant from `RestURI` |

### `kie-server-controller-standalone` — Swagger bootstrap
| Before | After |
|---|---|
| `KieSwaggerApiListingResource extends ApiListingResource` | `KieSwaggerApiListingResource extends OpenApiResource` |
| `StandaloneControllerApplication` used `SwaggerSerializers` / `ApiListingResource` from `io.swagger.jaxrs` | Now imports from `io.swagger.v3.jaxrs2` |
| `SwaggerAPIScanner` used `BeanConfig`, `DefaultJaxrsScanner`, `SwaggerContextService`, `SwaggerScannerLocator` | Replaced with `JaxrsOpenApiContextBuilder` + `SwaggerConfiguration` + `OpenAPI`/`Info`/`Server` model objects |

### `kie-server-rest-swagger` — Swagger listing & decoration
| Before | After |
|---|---|
| `KieApiListingResource extends ApiListingResource` | `KieApiListingResource extends OpenApiResource`; added explicit `getOpenApi()` override routing by `{type}` path param |
| `ReadOnlySwaggerDecoration extends AbstractSwaggerExtension` using `io.swagger.models.Operation` | Now `extends AbstractOpenAPIExtension` using `io.swagger.v3.oas.models.Operation` |
| `SwaggerRestApplicationComponentsService` registered old extension | Updated to register `ReadOnlySwaggerDecoration` as `OpenAPIExtension` |

### `kie-server-services-swagger` — Extension init
| Before | After |
|---|---|
| `SwaggerKieServerExtension.init()` used `DefaultJaxrsScanner`, `BeanConfig`, `SwaggerScannerLocator` | Replaced with `JaxrsOpenApiContextBuilder` + `SwaggerConfiguration` + fluent `OpenAPI` model setup |

### `kie-server-services-jbpm` — XStream + Hibernate marshalling
| Before | After |
|---|---|
| `HibernateXStreamMarshallerExtension` registered `HibernatePersistentCollectionConverter`, `HibernatePersistentMapConverter`, `HibernatePersistentSortedMapConverter`, `HibernatePersistentSortedSetConverter`, `HibernateMapper` from `com.thoughtworks.xstream.hibernate` | Replaced with custom `KieHibernatePersistentCollectionConverter`, `KieHibernatePersistentMapConverter`, `KieHibernatePersistentSortedMapConverter`, `KieHibernatePersistentSortedSetConverter`, `KieHibernateMapper` (new classes added under `xstream/kieadapters/`) |

### `kie-server-services-common` — Security
- [`JACCIdentityProvider`](kie-server-parent/kie-server-services/kie-server-services-common/src/main/java/org/kie/server/services/impl/security/JACCIdentityProvider.java): `PolicyContext.getContext("jakarta.security.auth.Subject.container")` changed to use new `SECURITY_AUTH_SUBJECT_CONTAINER` constant (`"javax.security.auth.Subject.container"` — **intentionally kept as `javax`**, matching Tomcat/WildFly's PolicyContext key).

### `kie-server-integ-tests-all` — Swagger integration test
| Before | After |
|---|---|
| `new SwaggerParser().parse(swaggerStr)` returning `Swagger` model | `new OpenAPIV3Parser().readContents(swaggerStr)` returning `SwaggerParseResult` / `OpenAPI` |
| `item.getGet()…getResponseSchema()` | `item.getGet()…` (schema now embedded in `ApiResponse.content`) |

### `kie-server-client` (tests)
- [`BaseKieServicesClientTest`](kie-server-parent/kie-server-remote/kie-server-client/src/test/java/org/kie/server/client/BaseKieServicesClientTest.java): `WireMockRule` constructor updated — added `false` as second argument (`failOnUnmatchedRequests`) to align with WireMock 3.x API change.
- [`UIServicesClientTest`](kie-server-parent/kie-server-remote/kie-server-client/src/test/java/org/kie/server/client/UIServicesClientTest.java): Reflective `Field.modifiers` access replaced with `Class.getDeclaredFields0(boolean)` via `Method` invocation (JDK 17 no longer allows direct access to `Field.modifiers`).

### `kie-server-integ-tests-router`
- [`KieServerRouterControllerRecoveryTest`](kie-server-parent/kie-server-tests/kie-server-integ-tests-router/src/test/java/org/kie/server/integrationtests/router/rest/KieServerRouterControllerRecoveryTest.java): WireMock `RequestMethod.name()` (enum) → `RequestMethod.getName()` (method on new `RequestMethod` class in WireMock 3.x).

### `kie-plugins-testing` — JDK 17 byte-code instrumentation fix
- `Child`, `School`, `TMDirectory`, `TMFileSet`, `PojoWithCollections`: collection fields changed from `private final` to `private` — JDK 17 byte-code instrumentation (`kie-maven-plugin`) cannot subclass/instrument classes with `final` collection fields.

### Module names
- `kjar-with-instrumentation-default-execmodel-wrapper`: `java.module.name` renamed from `org.drools.testcoverage.kjar.default.execmodel.wrapper` → `org.drools.testcoverage.kjar.execmodel.wrapper` (`.default.` is a reserved keyword in JPMS).
- `kie-maven-plugin-default-execmodel-test-suite`: similarly renamed to remove `.default.`.

### `kie-spring-boot` / Plexus Maven plugin
- `InjectReactiveMojo`: injection of `PlexusContainer` replaced with `Contextualizable` interface to avoid `DuplicateMemberException` under JDK 17's stricter module boundary checks.

---

## 3. Potential Breaking Changes

1. **Swagger/OpenAPI API surface** — All REST endpoints now emit OpenAPI 3.0 JSON (at `/swagger.json` and `/server/swagger.json`). Clients parsing Swagger 2.0 (`io.swagger.models.Swagger`) will break; must switch to `io.swagger.v3.oas.models.OpenAPI`.

2. **Swagger annotation attributes removed/renamed** — Any downstream code that relied on `@ApiOperation`, `@ApiParam`, `@ApiResponse(responseContainer=…)`, `@ApiResponse(allowableValues=…)`, `allowMultiple`, or `@Example`/`@ExampleProperty` annotations will fail to compile. Must migrate to OpenAPI 3 equivalents.

3. **XStream + Hibernate marshalling** — The built-in `com.thoughtworks.xstream.hibernate` converters are replaced by custom KIE equivalents. Any code that explicitly registers or depends on the old `HibernatePersistentCollectionConverter` / `HibernateMapper` classes will break at runtime.

4. **`Field.modifiers` reflection** — The pattern `Field.class.getDeclaredField("modifiers")` used to modify `final` fields is blocked in JDK 17 without `--add-opens`. It is replaced by `getDeclaredFields0` via reflection. Code in other test modules that follows the same pattern must be updated.

5. **`final` fields and instrumentation** — `kie-maven-plugin` cannot instrument classes with `final` collection fields under JDK 17. Any model class intended for XPath/reactive instrumentation must not declare those fields as `final`.

6. **WireMock 3.x API change** — `RequestMethod` is no longer an `enum`; `name()` (enum method) must be replaced by `getName()`. `WireMockRule` constructor signature changed; `failOnUnmatchedRequests` must now be explicitly passed.

7. **JPMS module names** — Module names containing the reserved word `.default.` are illegal in JPMS. Any `Automatic-Module-Name` or `module-info.java` using this pattern must be renamed.

8. **`hibernate-entitymanager` removed** — The `hibernate-entitymanager` artifact no longer exists in Hibernate 6+; all functionality is in `hibernate-core`. Any explicit dependency on `hibernate-entitymanager` must be replaced.

9. **`javax.security.auth.Subject.container` key** — Despite the global `javax` → `jakarta` rename, the JACC `PolicyContext` key string **remains `"javax.security.auth.Subject.container"`** (defined by the Jakarta EE spec for compatibility with Tomcat/WildFly containers). Changing it to `"jakarta.*"` would break security integration.

10. **ResteasyClientBuilder** — `org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder` is deprecated; usage replaced by `ResteasyClientBuilderImpl`. Code directly constructing `new ResteasyClientBuilder()` will fail at runtime in RESTEasy 6+.

11. **WireMock + Jetty 12** — The standard `org.wiremock:wiremock` artifact is built on Jetty 11. Projects standardising on Jetty 12 should depend on `org.wiremock:wiremock-jetty12` rather than the default `wiremock` artifact to avoid Jetty version conflicts. The `standalone` classifier no longer exists for `wiremock-jetty12`; drop it.

---

## 4. Jetty 12 Test Migration

### Background

The tests in [`ServerTestCase.java`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/ServerTestCase.java) and [`KieServerHttpRequestTest.java`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/KieServerHttpRequestTest.java) were originally written against the Jetty 9/11 server APIs. When the project moved to Jetty 12 those APIs were removed, and the tests stopped compiling. Separately, three tests that depend on WireMock and on precise HTTP response headers started failing at runtime once compilation was restored.

### Why things broke

**Handler API — compilation**

Jetty 12 introduces a different handler API centred around `Handler.Abstract`, replacing the older `AbstractHandler` style used by these tests. Along with that change, the APIs for reading request paths, content length, query parameters, and response headers were all revised. The existing test handlers relied on the old contracts throughout, so they did not compile against Jetty 12.

**WireMock artifact — runtime**

`org.wiremock:wiremock` bundles Jetty 11 as transitive dependencies. On a Jetty 12 classpath, WireMock's SPI implementation (`Jetty11HttpServerFactory`) cannot locate the required Jetty 11 classes and fails to start. In `KieServicesClientErrorHandlingTest.testError500Handling`, because WireMock failed to start, the test connected to another Jetty server already listening on the expected port — which returned a `WebSockets request was expected` body that `HttpAdminClient` could not parse as JSON.

**Chunked encoding — runtime**

`ServerTestCase.write()` passed `false` as the first argument to `response.write()`, telling Jetty the write was not the final one. That allows Jetty 12 to use chunked transfer encoding, omitting a `Content-Length` header. `HttpURLConnection.getContentLength()` returns `-1` when the header is absent, causing the `contentLength() == 5` assertion to fail.

### What was changed

#### Compilation fix — `ServerTestCase.java` and `KieServerHttpRequestTest.java`

**[`ServerTestCase.java`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/ServerTestCase.java)**

| Before (Jetty 11) | After (Jetty 12) |
|---|---|
| `extends AbstractHandler` | [`extends Handler.Abstract`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/ServerTestCase.java:64) |
| `handle(String target, Request baseRequest, HttpServletRequest req, HttpServletResponse res)` | [`handle(Request request, Response response, Callback callback)`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/ServerTestCase.java:104) |
| `baseRequest.getInputStream()` | [`Content.Source.asInputStream(request)`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/ServerTestCase.java:87) |
| `ProxyHandler` / old proxy setup | [`setupProxy()` rewritten with Jetty 12 `ProxyHandler.Reverse`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/ServerTestCase.java:138) |

**[`KieServerHttpRequestTest.java`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/KieServerHttpRequestTest.java)** — all anonymous handlers updated to the same `handle(Request, Response, Callback)` contract, with the following accessor replacements:

| Before (Jetty 11) | After (Jetty 12) |
|---|---|
| `request.getPathInfo()` | [`Request.getPathInContext(request)`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/KieServerHttpRequestTest.java:272) |
| `request.getContentLength()` | [`request.getLength()`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/KieServerHttpRequestTest.java:417) |
| `request.getContentType()` | [`request.getHeaders().get(HttpHeader.CONTENT_TYPE)`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/KieServerHttpRequestTest.java:443) |
| `request.getHeader(name)` | [`request.getHeaders().get(name)`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/KieServerHttpRequestTest.java:584) |
| `request.getParameterValues(name)` | [`Request.extractQueryParameters(request)`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/KieServerHttpRequestTest.java:1469) |

#### Runtime fix 1 & 2 — WireMock: `kie-parent` BOM (`kiegroup-droolsjbpm-build-bootstrap/pom.xml`)

| Before | After |
|---|---|
| `org.wiremock:wiremock` (two entries, one with `standalone` classifier) | `org.wiremock:wiremock-jetty12` at `${version.com.github.wiremock}` (single entry, no classifier) |

Added `dependencyManagement` entries to override the Jetty `12.0.30` transitive dependencies brought in by `wiremock-jetty12`, ensuring they align with the project's Jetty `12.1.10` version:

| Artifact |
|---|
| `org.eclipse.jetty:jetty-session` |
| `org.eclipse.jetty:jetty-ee` |
| `org.eclipse.jetty:jetty-alpn-server` |
| `org.eclipse.jetty:jetty-alpn-java-server` |
| `org.eclipse.jetty:jetty-alpn-java-client` |
| `org.eclipse.jetty:jetty-alpn-client` |
| `org.eclipse.jetty.ee10:jetty-ee10-servlet` |
| `org.eclipse.jetty.ee10:jetty-ee10-servlets` |
| `org.eclipse.jetty.ee10:jetty-ee10-webapp` |

Five child `pom.xml` files updated (`org.wiremock:wiremock` → `org.wiremock:wiremock-jetty12`):

| Module | File |
|---|---|
| `kie-server-router-client` | [`kie-server-parent/kie-server-router/kie-server-router-client/pom.xml`](kie-server-parent/kie-server-router/kie-server-router-client/pom.xml) |
| `kie-server-client` | [`kie-server-parent/kie-server-remote/kie-server-client/pom.xml`](kie-server-parent/kie-server-remote/kie-server-client/pom.xml) |
| `kie-server-integ-tests-router` | [`kie-server-parent/kie-server-tests/kie-server-integ-tests-router/pom.xml`](kie-server-parent/kie-server-tests/kie-server-integ-tests-router/pom.xml) |
| `kie-camel` | [`kie-camel/pom.xml`](kie-camel/pom.xml) |
| `kie-karaf-itests` | [`kie-osgi/kie-karaf-itests/pom.xml`](kie-osgi/kie-karaf-itests/pom.xml) — also removed the now-obsolete `standalone` classifier |

#### Runtime fix 3 — `Content-Length`: `ServerTestCase.write()`

In [`ServerTestCase.java`](kie-server-parent/kie-server-common/src/test/java/org/kie/server/common/rest/ServerTestCase.java):

| Before | After |
|---|---|
| `response.write(false, buffer, callback)` | `response.write(true, buffer, callback)` — marks the write as final, preventing chunked encoding |
| *(no `Content-Length` header set)* | `response.getHeaders().put(HttpHeader.CONTENT_LENGTH, bytes.length)` — sends a fixed-length response |
