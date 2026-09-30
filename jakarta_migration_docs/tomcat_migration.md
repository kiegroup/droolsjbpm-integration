# Tomcat Migration: DBACLD-237945 vs 7.81.x-blue-jakarta

This document summarises the changes introduced in branch `DBACLD-237945` on top of `7.81.x-blue-jakarta`, focused on upgrading KIE Server Tomcat support from Tomcat 9 to **Tomcat 10**.

---

## 1. Tomcat Version Upgrade: 9 → 10

**File:** `kie-server-parent/kie-server-tests/pom.xml`

- The Maven/Cargo integration test profile was renamed from `tomcat9` to `tomcat10`.
- The Cargo container ID changed from `tomcat9x` to `tomcat10x`.
- The Cargo configuration directory path was updated accordingly.

---

## 2. Custom JTA Factories in `kie-tomcat-integration`

**Files added** (new classes in `kie-tomcat-integration/src/main/java/org/kie/integration/tomcat/jta/`):

| Class | Replaces |
|---|---|
| `KieNarayanaJtaServletContextListener` | `org.jboss.narayana.tomcat.jta.NarayanaJtaServletContextListener` |
| `KieTransactionManagerFactory` | `org.jboss.narayana.tomcat.jta.TransactionManagerFactory` |
| `KieTransactionSynchronizationRegistryFactory` | `org.jboss.narayana.tomcat.jta.TransactionSynchronizationRegistryFactory` |
| `KieUserTransactionFactory` | `org.jboss.narayana.tomcat.jta.UserTransactionFactory` |
| `KieTransactionalDataSourceFactory` | `org.jboss.narayana.tomcat.jta.TransactionalDataSourceFactory` |
| `KiePoolingDataSourceFactory` | *(new — pooling XA datasource support)* |

These wrappers are required because the upstream `narayana-tomcat` integration library has not been updated for Jakarta EE 10 / Tomcat 10. The KIE-owned wrappers use `jakarta.*` imports and delegate to Narayana internals directly.

**File:** `kie-tomcat-integration/pom.xml`

- Added `tomcat-dbcp` (provided) and Narayana `jta` + `jdbc` dependencies to support the new factory classes.

---

## 3. `context.xml` — JTA Resource Configuration

**File:** `kie-server-parent/kie-server-wars/kie-server/src/main/webc-resources/META-INF/context.xml`

- All three Narayana Tomcat resource factories replaced with their KIE-owned counterparts (see table above).
- Transaction resource types updated from `javax.transaction.*` to `jakarta.transaction.*`.
- Added a pooling XA DataSource resource (`poolingXaDs`) using `KieTransactionalDataSourceFactory` and a `ResourceLink` for the raw XA datasource (`xads`).

---

## 4. `web.xml` — Servlet and Listener Configuration

**Files:** `kie-server-parent/kie-server-wars/kie-server/src/main/webc-resources/WEB-INF/web.xml`  
and `kie-server-parent/kie-server-controller/kie-server-controller-standalone/src/main/webc-resources/WEB-INF/web.xml`

- Servlet context listener changed from the upstream Narayana class to `KieNarayanaJtaServletContextListener`.
- `javax.ws.rs.Application` init-param renamed to `jakarta.ws.rs.Application`.
- Added explicit `resteasy.servlet.mapping.prefix` init-param to the RESTEasy servlet.
- Added `resteasy.preferJacksonOverJsonB=true` context-param (KIE Server `web.xml`).
- Added `/services/rest/server/openapi.json` (and `/rest/openapi.json` for the controller) to the Swagger security constraint URL patterns.

---

## 5. OpenAPI / Swagger Endpoint

**Files:** `kie-server-parent/kie-server-wars/kie-server/pom.xml`, `kie-server-parent/kie-server-controller/kie-server-controller-standalone/pom.xml`, `kie-swagger-ui/src/main/resources/index.html`

- The Swagger UI URL property `org.kie.swagger.url` was changed from `swagger.json` to `openapi.json` in both the KIE Server and standalone controller POMs.
- The controller POM also gained an explicit `org.kie.swagger.base.path` property.
- `KieServerApplication.java` registers the `OpenApiResource` (currently commented out, left for reference).
- Swagger UI `index.html` updated to detect `openapi.json` instead of `swagger.json` when patching the server URL.
- Security constraints in `web.xml` (EE and servlet-container variants) extended to include the `/openapi.json` URL pattern.

---

## 6. Assembly Descriptors

**Files:** `kie-server-parent/kie-server-wars/kie-server/src/main/assembly/assembly-servlet-container.xml`  
and `kie-server-parent/kie-server-controller/kie-server-controller-standalone/src/main/assembly/assembly-servlet-container.xml`

- `jakarta.ws.rs:jakarta.ws.rs-api` excluded from the servlet-container assembly (provided by Tomcat 10 / RESTEasy on the classpath).
- `org.kie:kie-tomcat-integration` excluded from the generic servlet-container assembly for KIE Server (it is added separately into `TOMCAT_HOME/lib`).
- Removed `com.sun.xml.bind:jaxb-core` and `com.sun.xml.bind:jaxb-impl` from the controller assembly (no longer needed with Jakarta EE 10).

---

## 7. Installation README

**File:** `kie-server-parent/kie-server-wars/kie-server/src/main/webc-resources/README.md`

- Updated instructions from Tomcat 9 to **Tomcat 10**.
- Replaced `javax.security.jacc:javax.security.jacc-api` with `jakarta.security.jacc:jakarta.security.jacc-api`.
- Replaced `org.jboss.integration:narayana-tomcat` with the new KIE-owned JTA jars and additional Jakarta API jars (`jakarta.transaction-api`, `jakarta.ws.rs-api`, `jakarta.activation-api`, `reactive-streams`, `resteasy-core`, `resteasy-core-spi`).
- Replaced `org.jboss:jboss-transaction-spi` with `org.jboss:jboss-transaction-spi-jakarta`.
- Updated `TransactionalDataSourceFactory` references in `context.xml` examples to `KieTransactionalDataSourceFactory`.
- Fixed `setenv.sh` XA recovery property escaping.
