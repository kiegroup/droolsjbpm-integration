# Jackson Module JAXB Migration: `jackson-module-jaxb-annotations` → `jackson-module-jakarta-xmlbind-annotations`

## Overview

`com.fasterxml.jackson.module:jackson-module-jaxb-annotations` targets the legacy
`javax.xml.bind` namespace (JAXB 2.x / Jakarta EE 8 and earlier). Its replacement,
`com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations`, targets the
modern `jakarta.xml.bind` namespace (Jakarta EE 9+).

**Maven Central verification:** `jackson-module-jakarta-xmlbind-annotations` is present on
Maven Central (latest release: **2.19.0**) and is already declared in the `kie-parent` BOM
under `${version.com.fasterxml.jackson}` — no inline `<version>` is needed in any module pom.

> **Note:** Two files (`kie-server-services-common/pom.xml` and `kie-rest-common/pom.xml`)
> also had a stray **leading space** in the old `<artifactId>` value
> (` jackson-module-jaxb-annotations`) which was corrected as part of this change.

---

## Artifact Mapping

| Old artifact | New artifact | Maven Central |
|---|---|---|
| `com.fasterxml.jackson.module:jackson-module-jaxb-annotations` | `com.fasterxml.jackson.module:jackson-module-jakarta-xmlbind-annotations` | ✅ [2.19.0](https://search.maven.org/artifact/com.fasterxml.jackson.module/jackson-module-jakarta-xmlbind-annotations) |

---

## Modified pom.xml Files

### 1. `kie-server-parent/kie-server-api/pom.xml`

```xml
<!-- Before -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jaxb-annotations</artifactId>
</dependency>

<!-- After -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jakarta-xmlbind-annotations</artifactId>
</dependency>
```

---

### 2. `kie-server-parent/kie-server-services/kie-server-services-common/pom.xml`

> Also fixed a leading whitespace in the `<artifactId>` value.

```xml
<!-- Before (note the leading space) -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId> jackson-module-jaxb-annotations</artifactId>
</dependency>

<!-- After -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jakarta-xmlbind-annotations</artifactId>
</dependency>
```

---

### 3. `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-common/pom.xml`

> Also fixed a leading whitespace in the `<artifactId>` value.

```xml
<!-- Before (note the leading space) -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId> jackson-module-jaxb-annotations</artifactId>
</dependency>

<!-- After -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jakarta-xmlbind-annotations</artifactId>
</dependency>
```

---

### 4. `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-swagger/pom.xml`

```xml
<!-- Before -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jaxb-annotations</artifactId>
</dependency>

<!-- After -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jakarta-xmlbind-annotations</artifactId>
</dependency>
```

---

### 5. `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-prometheus/pom.xml`

```xml
<!-- Before -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jaxb-annotations</artifactId>
</dependency>

<!-- After -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jakarta-xmlbind-annotations</artifactId>
</dependency>
```

---

### 6. `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-scenario-simulation/pom.xml`

```xml
<!-- Before -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jaxb-annotations</artifactId>
</dependency>

<!-- After -->
<dependency>
  <groupId>com.fasterxml.jackson.module</groupId>
  <artifactId>jackson-module-jakarta-xmlbind-annotations</artifactId>
</dependency>
```

---

## Summary Table

| pom.xml | Leading-space fix |
|---|---|
| `kie-server-parent/kie-server-api/pom.xml` | No |
| `kie-server-parent/kie-server-services/kie-server-services-common/pom.xml` | **Yes** |
| `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-common/pom.xml` | **Yes** |
| `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-swagger/pom.xml` | No |
| `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-prometheus/pom.xml` | No |
| `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-scenario-simulation/pom.xml` | No |
