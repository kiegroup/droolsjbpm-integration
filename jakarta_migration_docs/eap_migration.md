# EAP Migration: 7.4 → 8.1 Differences

This document records all changes found between the **reference codebase**
(`/Users/gcardosi/Developing/git/droolsjbpm-integration`, version `7.67.2-SNAPSHOT`, targeting EAP 7.4)
and the **current codebase** (this repo, version `7.81.1-SNAPSHOT`, targeting EAP 8.1).
Only differences that relate to EAP deployment are covered.

---

## 1. Project Version Bump

| | Old (EAP 7.4) | New (EAP 8.1) |
|---|---|---|
| `droolsjbpm-integration` version | `7.67.2-SNAPSHOT` | `7.81.1-SNAPSHOT` |
| `kie-server-wars` parent version | `7.67.2-SNAPSHOT` | `7.81.1-SNAPSHOT` |

The `kie-parent` BOM consumed by the root `pom.xml` also changes with this version bump; all
downstream library versions (Hibernate, RESTEasy, Jackson, …) are inherited from it.

---

## 2. Disabled Modules (`pom.xml` `<modules>`)

In the new codebase several modules that existed in the EAP 7.4 build are **temporarily
disabled** while the Jakarta EE migration is completed:

| Module | EAP 7.4 | EAP 8.1 |
|---|---|---|
| `kie-spring` | ✅ enabled | ❌ disabled (commented out) |
| `kie-osgi` | ✅ enabled | ❌ disabled (commented out) |
| `kie-camel` | ✅ enabled | ❌ disabled (commented out) |
| `kie-spring-boot` | ✅ enabled | ❌ disabled (commented out) |
| `camel-container-tests` | ✅ enabled | ❌ disabled (commented out) |

These modules depend on `kie-spring` and have not yet been migrated to Jakarta EE APIs.

---

## 3. WAR Assembly: New EE 10 Profile

The Maven Assembly Plugin in `kie-server-parent/kie-server-wars/kie-server/pom.xml` now
builds an **additional** WAR artifact for EE 10 containers (Jakarta EE 10 / EAP 8.1):

**EAP 7.4** – assembly descriptors:
```
assembly-ee8-container.xml
assembly-ee7-container.xml
assembly-servlet-container.xml
```

**EAP 8.1** – assembly descriptors:
```
assembly-ee10-container.xml   ← NEW
assembly-ee8-container.xml
assembly-ee7-container.xml
assembly-servlet-container.xml
```

The new `assembly-ee10-container.xml` produces a WAR classified as `ee10` and is the artifact
intended for deployment on EAP 8.1.

### What `assembly-ee10-container.xml` omits that `assembly-ee8-container.xml` includes

| Artefact | EE8 assembly | EE10 assembly |
|---|---|---|
| `com.sun.xml.bind:jaxb-xjc` | ✅ bundled | ❌ removed |
| `org.hibernate:hibernate-entitymanager` | ✅ bundled | ✅ bundled (kept) |
| `org.hibernate.orm:hibernate-core` | ✅ bundled | ✅ bundled (kept) |
| `org.kie.server:kie-server-services-openshift` | ✅ bundled | ✅ bundled (kept) |

`jaxb-xjc` is dropped in the EE 10 assembly because EAP 8.1 / Jakarta EE 10 ships the Jakarta
XML Binding API natively via its own modules, making the legacy Sun implementation redundant
and conflict-prone.

---

## 4. `jboss-deployment-structure.xml` Changes

File: `kie-server-parent/kie-server-wars/kie-server/src/main/ee-resources/WEB-INF/jboss-deployment-structure.xml`

### 4.1 Schema namespace version

| | Old (EAP 7.4) | New (EAP 8.1) |
|---|---|---|
| XML namespace | `urn:jboss:deployment-structure:1.1` | `urn:jboss:deployment-structure:1.3` |

EAP 8.1 requires the `1.3` version of the JBoss deployment-structure schema.

### 4.2 Excluded Jackson module names

The Jackson module names changed because EAP 8.1 ships Jackson under a different JBoss
module tree (modules renamed to drop the redundant artifact suffix):

| Purpose | Old module name | New module name |
|---|---|---|
| Jackson core | `com.fasterxml.jackson.core.jackson-core` | `com.fasterxml.jackson.core` |
| Jackson databind | `com.fasterxml.jackson.core.jackson-databind` | `com.fasterxml.jackson.databind` |
| Jackson annotations | `com.fasterxml.jackson.core.jackson-annotations` | `com.fasterxml.jackson.annotations` |
| Jackson YAML | `com.fasterxml.jackson.dataformat.jackson-dataformat-yaml` | `com.fasterxml.jackson.dataformat.yaml` |
| Jackson JAX-RS JSON | `com.fasterxml.jackson.jaxrs.jackson-jaxrs-json-provider` | `com.fasterxml.jackson.jakarta.rs.json` |

The last row is particularly important: EAP 8.1 provides the **Jakarta RS** variant of the
Jackson JAX-RS provider, replacing the old `javax.rs` / `jaxrs` one.

### 4.3 Added Hibernate exclusion

EAP 8.1 ships Hibernate 6 as a platform module. The new `jboss-deployment-structure.xml`
**excludes** the container's Hibernate module so that the version bundled in the WAR is used:

```xml
<!-- NEW in EAP 8.1 deployment-structure -->
<exclusions>
  ...
  <module name="org.hibernate" />
</exclusions>
```

This exclusion did not exist in EAP 7.4 because Hibernate 5 was bundled in the WAR and EAP
7.4 did not ship Hibernate via a conflicting public module.

### 4.4 Removed `org.apache.xerces` dependency

EAP 7.4 declared:
```xml
<module name="org.apache.xerces"/>
```
as a deployment dependency.  
EAP 8.1 removes this explicit dependency (Xerces is still available but resolved via the
bundled `xerces:xercesImpl` JAR in `WEB-INF/lib`). The comment `<!-- TEMP EXCLUSION - TO BE
RESTORED -->` around `org.apache.xerces` was retained for reference but the active declaration
was dropped.

---

## 5. `web.xml` (EE resources) Changes

File: `kie-server-parent/kie-server-wars/kie-server/src/main/ee-resources/WEB-INF/web.xml`

### 5.1 Jakarta EE 10 namespace

| | Old (EAP 7.4) | New (EAP 8.1) |
|---|---|---|
| XML namespace | `http://xmlns.jcp.org/xml/ns/javaee` | `https://jakarta.ee/xml/ns/jakartaee` |
| Schema location | `…/javaee/web-app_3_1.xsd` | `…/jakartaee/web-app_6_0.xsd` |
| Servlet version | `3.1` | `6.0` |

### 5.2 Servlet declaration style

**EAP 7.4** used a bare `<servlet>` element (relying on JAX-RS application scanning):
```xml
<servlet>
  <servlet-name>org.kie.server.remote.rest.common.KieServerApplication</servlet-name>
  <load-on-startup>1</load-on-startup>
</servlet>
```

**EAP 8.1** uses an explicit RESTEasy `HttpServletDispatcher` with a `jakarta.ws.rs.Application`
init-param:
```xml
<servlet>
  <servlet-name>org.kie.server.remote.rest.common.KieServerApplication</servlet-name>
  <servlet-class>org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher</servlet-class>
  <init-param>
    <param-name>jakarta.ws.rs.Application</param-name>
    <param-value>org.kie.server.remote.rest.common.KieServerApplication</param-value>
  </init-param>
  <init-param>
    <param-name>resteasy.servlet.mapping.prefix</param-name>
    <param-value>/rest</param-value>
  </init-param>
  <load-on-startup>1</load-on-startup>
</servlet>
```

Key points:
- The `init-param` key changed from `javax.ws.rs.Application` → `jakarta.ws.rs.Application`.
- A new `resteasy.servlet.mapping.prefix` init-param (`/rest`) replaces the old context-param.

### 5.3 REST URL pattern

| | Old (EAP 7.4) | New (EAP 8.1) |
|---|---|---|
| Servlet URL mapping | `/services/rest/*` | `/rest/*` |
| Security constraints | `/services/rest/*` | `/rest/*` |
| Health/readiness probes | `/services/rest/server/readycheck` | `/rest/server/readycheck` |
| Swagger JSON | `/services/rest/server/swagger.json/*` | `/rest/server/swagger.json/*` |

The `/services` path prefix has been removed; the REST API is now served at `/rest/*`.

### 5.4 `resteasy.preferJacksonOverJsonB` context-param (new in EAP 8.1)

```xml
<!-- NEW in EAP 8.1 -->
<context-param>
  <param-name>resteasy.preferJacksonOverJsonB</param-name>
  <param-value>true</param-value>
</context-param>
```

EAP 8.1 / Jakarta EE 10 includes a JSON-B provider by default. This parameter instructs
RESTEasy to continue using Jackson (the provider bundled in the WAR) rather than JSON-B,
preserving backward-compatible serialisation behaviour.

---

## 6. `ejb-jar.xml` Changes

File: `kie-server-parent/kie-server-wars/kie-server/src/main/ee-resources/WEB-INF/ejb-jar.xml`

The `<activation-config-property-value>` for `destinationType` in the commented-out MDB
definitions changed from the `javax.jms` package to `jakarta.jms`:

| | Old (EAP 7.4) | New (EAP 8.1) |
|---|---|---|
| JMS Queue type | `javax.jms.Queue` | `jakarta.jms.Queue` |

This change is relevant if the signal/audit MDBs are uncommented and enabled.

---

## 7. Java Source – `javax` → `jakarta` Migration in JMS / EJB Code

### 7.1 `KieServerMDB.java`

File: `kie-server-parent/kie-server-remote/kie-server-jms/src/main/java/org/kie/server/jms/KieServerMDB.java`

All `javax.*` EE imports were replaced with `jakarta.*` equivalents. `javax.naming` was kept
as-is (it is a Java SE API, not a Jakarta EE API):

| Old import | New import |
|---|---|
| `javax.annotation.PostConstruct` | `jakarta.annotation.PostConstruct` |
| `javax.annotation.PreDestroy` | `jakarta.annotation.PreDestroy` |
| `javax.annotation.Resource` | `jakarta.annotation.Resource` |
| `javax.ejb.ActivationConfigProperty` | `jakarta.ejb.ActivationConfigProperty` |
| `javax.ejb.MessageDriven` | `jakarta.ejb.MessageDriven` |
| `javax.ejb.TransactionManagement` | `jakarta.ejb.TransactionManagement` |
| `javax.ejb.TransactionManagementType` | `jakarta.ejb.TransactionManagementType` |
| `javax.jms.Connection` | `jakarta.jms.Connection` |
| `javax.jms.ConnectionFactory` | `jakarta.jms.ConnectionFactory` |
| `javax.jms.JMSException` | `jakarta.jms.JMSException` |
| `javax.jms.Message` | `jakarta.jms.Message` |
| `javax.jms.MessageListener` | `jakarta.jms.MessageListener` |
| `javax.jms.MessageProducer` | `jakarta.jms.MessageProducer` |
| `javax.jms.Queue` | `jakarta.jms.Queue` |
| `javax.jms.Session` | `jakarta.jms.Session` |
| `javax.jms.TextMessage` | `jakarta.jms.TextMessage` |

The `@MessageDriven` `activationConfig` property value also changed:

| Property | Old value | New value |
|---|---|---|
| `destinationType` | `javax.jms.Queue` | `jakarta.jms.Queue` |

### 7.2 `persistence.xml`

File: `kie-server-parent/kie-server-services/kie-server-services-jbpm/src/main/resources/jpa/META-INF/persistence.xml`

| | Old (EAP 7.4) | New (EAP 8.1) |
|---|---|---|
| JPA version | `2.1` | `3.1` |
| XML namespace | `http://xmlns.jcp.org/xml/ns/persistence` | `https://jakarta.ee/xml/ns/persistence` |
| Schema location | `…/persistence_2_1.xsd` | `…/persistence_3_2.xsd` |

The persistence unit content (entities, mappings, properties) is identical.

---

## 8. Maven Dependency Changes in `kie-server` WAR `pom.xml`

File: `kie-server-parent/kie-server-wars/kie-server/pom.xml`

### 8.1 Hibernate dependency group change

| | Old (EAP 7.4) | New (EAP 8.1) |
|---|---|---|
| Hibernate Core artifact | `org.hibernate:hibernate-core` | `org.hibernate.orm:hibernate-core` |
| `hibernate-entitymanager` | `org.hibernate:hibernate-entitymanager` | **Removed** (merged into `hibernate-core` in Hibernate 6) |

Hibernate 6 (required for Jakarta Persistence 3.x) eliminated the separate
`hibernate-entitymanager` artifact; its classes are now part of `hibernate-core` under the
`org.hibernate.orm` groupId.

### 8.2 JAXB artifact change

| | Old (EAP 7.4) | New (EAP 8.1) |
|---|---|---|
| JAXB XJC | `com.sun.xml.bind:jaxb-xjc` | `org.glassfish.jaxb:jaxb-xjc` |

The legacy Sun JAXB implementation was replaced by the GlassFish / Eclipse Implementation
reference, which is the upstream of the Jakarta XML Binding spec.

### 8.3 `resteasy-jackson2-provider` scope change

| | Old (EAP 7.4) | New (EAP 8.1) |
|---|---|---|
| Scope | (compile, bundled in WAR) | `provided` |

EAP 8.1 ships `resteasy-jackson2-provider` as a container module (exposed as
`com.fasterxml.jackson.jakarta.rs.json`). Because the deployment exclusion in
`jboss-deployment-structure.xml` prevents the container's version from loading, and
`resteasy-jackson2-provider` is still bundled in the WAR through the assembly dependency sets,
the `provided` scope ensures it is available at compile-time but the assembly plugin controls
whether it is included at runtime.

### 8.4 Additional exclusion of `resteasy-core-spi` and `resteasy-jaxb-provider`

In the EAP 8.1 `pom.xml`, the exclusion block for `kie-server-services-common` gained two
new entries that were not present in EAP 7.4:

```xml
<exclusion>
  <groupId>org.jboss.resteasy</groupId>
  <artifactId>resteasy-core-spi</artifactId>
</exclusion>
<exclusion>
  <groupId>org.jboss.resteasy</groupId>
  <artifactId>resteasy-jaxb-provider</artifactId>
</exclusion>
```

These are provided by EAP 8.1's container and must not be bundled.

---

## 9. `jboss-all.xml` and `kie-server-jms.xml`

Both files are **identical** between EAP 7.4 and EAP 8.1:

- `META-INF/jboss-all.xml` – CDI Weld descriptor requiring `beans.xml` is unchanged.
- `META-INF/kie-server-jms.xml` – ActiveMQ Artemis JMS queue definitions are unchanged.

---

## 10. Summary of All File-Level Differences

| File | Change type | Details |
|---|---|---|
| `pom.xml` (root) | Version, disabled modules | Version `7.67.2` → `7.81.1`; 5 modules disabled |
| `kie-server-wars/kie-server/pom.xml` | Dependencies, scope | Hibernate groupId, JAXB artifact, RESTEasy scope, new exclusions |
| `kie-server-wars/kie-server/pom.xml` | New assembly | `assembly-ee10-container.xml` added |
| `assembly-ee10-container.xml` | New file | EE 10 WAR assembly for EAP 8.1 |
| `WEB-INF/jboss-deployment-structure.xml` | Schema, exclusions, module names | v1.1 → v1.3; renamed Jackson modules; new Hibernate exclusion; removed Xerces dependency |
| `WEB-INF/web.xml` (ee-resources) | Namespace, version, servlet, URL | JavaEE 3.1 → JakartaEE 6.0; explicit RESTEasy servlet; `/services/rest` → `/rest`; new `resteasy.preferJacksonOverJsonB` |
| `WEB-INF/ejb-jar.xml` | Namespace value | `javax.jms.Queue` → `jakarta.jms.Queue` in comments |
| `KieServerMDB.java` | Import migration | All `javax.*` EE imports → `jakarta.*` |
| `persistence.xml` | Namespace, version | JPA 2.1 → JPA 3.1; xmlns.jcp.org → jakarta.ee |
| `jboss-all.xml` | None | Unchanged |
| `kie-server-jms.xml` | None | Unchanged |
