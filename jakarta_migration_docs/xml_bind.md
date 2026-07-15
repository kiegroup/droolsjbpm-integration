# JAXB Dependency Migration: `com.sun.xml.bind` → `org.glassfish.jaxb`

## Overview

All `com.sun.xml.bind` JAXB implementation dependencies have been replaced with their
`org.glassfish.jaxb` equivalents. Starting from Jakarta EE 9+, the canonical JAXB
runtime has moved from the legacy `com.sun.xml.bind` coordinates to `org.glassfish.jaxb`.
The parent BOM (`kie-parent-7.81.1-SNAPSHOT.pom`) already declares and manages
`org.glassfish.jaxb` at version **4.0.5** and explicitly notes the removal of
`version.com.sun.xml.bind`.

All three `org.glassfish.jaxb` artifacts are available on Maven Central
(latest release: **4.0.6** as of verification date).

---

## Artifact Mapping

| Old (`com.sun.xml.bind`)  | New (`org.glassfish.jaxb`) | Maven Central |
|---------------------------|----------------------------|---------------|
| `jaxb-core`               | `jaxb-core`                | ✅ [4.0.6](https://search.maven.org/artifact/org.glassfish.jaxb/jaxb-core) |
| `jaxb-impl`               | `jaxb-runtime`             | ✅ [4.0.6](https://search.maven.org/artifact/org.glassfish.jaxb/jaxb-runtime) |
| `jaxb-xjc`                | `jaxb-xjc`                 | ✅ [4.0.6](https://search.maven.org/artifact/org.glassfish.jaxb/jaxb-xjc) |

Version is managed by the `kie-parent` BOM via `${version.org.glassfish.jaxb}` (= `4.0.5`).
No inline `<version>` tags are needed in the module poms.

---

## Modified pom.xml Files

### 1. `kie-plugins-testing/pom.xml`
- **Changed:** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core` (compile)
- **Changed:** `com.sun.xml.bind:jaxb-impl` → `org.glassfish.jaxb:jaxb-runtime` (compile)

---

### 2. `kie-takari-plugin/pom.xml`
- **Changed (exclusion):** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core`
  inside `kie-pmml` exclusions
- **Changed (exclusion):** `com.sun.xml.bind:jaxb-impl` → `org.glassfish.jaxb:jaxb-runtime`
  inside `kie-pmml` exclusions

---

### 3. `kie-maven-plugin/pom.xml`
- **Changed (exclusion):** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core`
  inside `kie-pmml` exclusions
- **Changed (exclusion):** `com.sun.xml.bind:jaxb-impl` → `org.glassfish.jaxb:jaxb-runtime`
  inside `kie-pmml` exclusions

---

### 4. `kie-camel/pom.xml`
- **Changed:** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core` (compile)
- **Changed:** `com.sun.xml.bind:jaxb-impl` → `org.glassfish.jaxb:jaxb-runtime` (compile)
- **Changed:** `com.sun.xml.bind:jaxb-xjc` → `org.glassfish.jaxb:jaxb-xjc` (runtime, optional)

---

### 5. `kie-spring/pom.xml`
- **Changed:** `com.sun.xml.bind:jaxb-impl` → `org.glassfish.jaxb:jaxb-runtime` (provided)
- **Changed:** `com.sun.xml.bind:jaxb-xjc` → `org.glassfish.jaxb:jaxb-xjc` (provided)
- **Changed:** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core` (test)
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `hibernate-entitymanager` exclusion comment

---

### 6. `kie-spring-boot/kie-spring-boot-samples/jbpm-spring-boot-sample-basic/pom.xml`
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `jbpm-spring-boot-starter-basic` exclusion comment
- **Changed:** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core` (test)
- **Changed:** `com.sun.xml.bind:jaxb-impl` → `org.glassfish.jaxb:jaxb-runtime` (test)

---

### 7. `kie-server-parent/kie-server-api/pom.xml`
- **Changed:** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core` (test)
- **Changed:** `com.sun.xml.bind:jaxb-impl` → `org.glassfish.jaxb:jaxb-runtime` (test)

---

### 8. `kie-server-parent/kie-server-services/kie-server-services-common/pom.xml`
- **Changed:** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core` (test)

---

### 9. `kie-server-parent/kie-server-remote/kie-server-client/pom.xml`
- **Changed:** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core` (compile)
- **Changed:** `com.sun.xml.bind:jaxb-impl` → `org.glassfish.jaxb:jaxb-runtime` (compile)

---

### 10. `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-case-mgmt/pom.xml`
- **Changed:** `com.sun.xml.bind:jaxb-core` → `org.glassfish.jaxb:jaxb-core` (test)

---

### 11. `kie-server-parent/kie-server-wars/kie-server/pom.xml`
- **Changed:** `com.sun.xml.bind:jaxb-xjc` → `org.glassfish.jaxb:jaxb-xjc` (compile)
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `hibernate-core` exclusion comment

---

### 12. `kie-server-parent/kie-server-services/kie-server-services-jbpm/pom.xml`
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `xstream-hibernate` exclusion comment *(exclusion itself was already `org.glassfish.jaxb:jaxb-runtime`)*

---

### 13. `kie-spring-boot/kie-spring-boot-autoconfiguration/kie-server-spring-boot-autoconfiguration-jbpm/pom.xml`
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `jbpm-services-ejb-impl` exclusion comment *(exclusion itself was already `org.glassfish.jaxb:jaxb-runtime`)*

---

### 14. `kie-spring-boot/kie-spring-boot-autoconfiguration/kie-server-spring-boot-autoconfiguration-audit-replication/pom.xml`
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  exclusion comment *(exclusion itself was already `org.glassfish.jaxb:jaxb-runtime`)*

---

### 15. `droolsjbpm-bpms-distribution/pom.xml`
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `jbpm-test` exclusion comment *(exclusion itself was already `org.glassfish.jaxb:jaxb-runtime`)*

---

### 16. `kie-server-parent/kie-server-tests/kie-server-integ-tests-all/pom.xml`
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `hibernate-core` exclusion comment *(exclusion itself was already `org.glassfish.jaxb:jaxb-runtime`)*

---

### 17. `kie-server-parent/kie-server-tests/kie-server-integ-tests-jbpm/pom.xml`
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `hibernate-core` exclusion comment *(exclusion itself was already `org.glassfish.jaxb:jaxb-runtime`)*

---

### 18. `kie-server-parent/kie-server-tests/kie-server-integ-tests-router/pom.xml`
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `hibernate-core` exclusion comment *(exclusion itself was already `org.glassfish.jaxb:jaxb-runtime`)*

---

### 19. `kie-server-parent/kie-server-tests/kie-server-integ-tests-policy/pom.xml`
- **Updated comment:** stale `com.sun.xml.bind:jaxb-impl:jar:2.3.0` reference updated in
  `hibernate-core` exclusion comment *(exclusion itself was already `org.glassfish.jaxb:jaxb-runtime`)*

---

### 20. `kie-osgi/kie-karaf-features/pom.xml`
- **Changed property:** `karaf.servicemix.version.com.sun.xml.bind.jaxb` →
  `karaf.servicemix.version.org.glassfish.jaxb.jaxb-runtime`
- **Changed property:** `karaf.servicemix.version.com.sun.xml.bind.jaxb.xjc` →
  `karaf.servicemix.version.org.glassfish.jaxb.jaxb-xjc`
- **Updated version references** for `org.apache.servicemix.bundles.jaxb-xjc` and
  `org.apache.servicemix.bundles.jaxb-runtime` to use the renamed properties

  > **Note:** The Karaf OSGi feature uses `org.apache.servicemix.bundles` wrappers
  > (`jaxb-runtime`, `jaxb-xjc`), not the `com.sun.xml.bind` JARs directly. Only
  > the property *names* were updated to align with the new `org.glassfish.jaxb` naming;
  > the bundle versions (e.g. `2.3.2_3`) remain unchanged as they refer to the
  > ServiceMix bundle wrapper versions.

---

## Files with `com.sun.xml.bind` only in Non-pom Context (not modified)

| File | Note |
|------|------|
| `kie-osgi/kie-karaf-features/src/main/filtered-resources/repository/features.xml` | Karaf features descriptor (not a pom) |
| `kie-server-parent/kie-server-controller/kie-server-controller-standalone/src/main/assembly/assembly-*.xml` | Assembly descriptors (not a pom) |
| `kie-server-parent/kie-server-wars/kie-server/src/main/assembly/assembly-*.xml` | Assembly descriptors (not a pom) |

---

## Version Management

All `org.glassfish.jaxb` dependencies are version-managed in the `kie-parent` BOM:

```xml
<!-- In kie-parent-7.81.1-SNAPSHOT.pom dependencyManagement -->
<dependency>
  <groupId>org.glassfish.jaxb</groupId>
  <artifactId>jaxb-core</artifactId>
  <version>${version.org.glassfish.jaxb}</version>   <!-- 4.0.5 -->
</dependency>
<dependency>
  <groupId>org.glassfish.jaxb</groupId>
  <artifactId>jaxb-runtime</artifactId>
  <version>${version.org.glassfish.jaxb}</version>   <!-- 4.0.5 -->
</dependency>
<dependency>
  <groupId>org.glassfish.jaxb</groupId>
  <artifactId>jaxb-xjc</artifactId>
  <version>${version.org.glassfish.jaxb}</version>   <!-- 4.0.5 -->
</dependency>
```

No module pom needs an inline `<version>` for these artifacts.
