# Hibernate EntityManager Migration: `hibernate-entitymanager` removed

## Overview

`org.hibernate:hibernate-entitymanager` was a separate Maven artifact in Hibernate 4/5 that
packaged the JPA `EntityManager` implementation. Starting with **Hibernate 5.2**, its contents
were merged into `org.hibernate:hibernate-core`, making the standalone artifact redundant.
Declaring both is unnecessary and can lead to classpath conflicts.

All occurrences have been removed from pom.xml files. Where `hibernate-core` was not yet
declared, it was added as a direct replacement, preserving the original scope and exclusions.
Where `hibernate-core` was already present, `hibernate-entitymanager` was simply dropped
(with any unique exclusions it carried merged into the existing `hibernate-core` entry).

---

## Modified pom.xml Files

### 1. `kie-spring/pom.xml`

**Action:** Replaced `hibernate-entitymanager` with `hibernate-core` — `hibernate-core` was
**not** previously declared in this module.

- Scope preserved: `test`
- Existing exclusion preserved: `org.glassfish.jaxb:jaxb-runtime`

```xml
<!-- Before -->
<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-entitymanager</artifactId>
  <scope>test</scope>
  <exclusions>
    <exclusion>
      <groupId>org.glassfish.jaxb</groupId>
      <artifactId>jaxb-runtime</artifactId>
    </exclusion>
  </exclusions>
</dependency>

<!-- After -->
<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-core</artifactId>
  <scope>test</scope>
  <exclusions>
    <exclusion>
      <groupId>org.glassfish.jaxb</groupId>
      <artifactId>jaxb-runtime</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

---

### 2. `kie-aries-blueprint/pom.xml`

**Action:** Removed the entire commented-out `<!-- test persistence -->` block which contained
`hibernate-entitymanager`, `h2`, `spring-jdbc`, and `spring-orm`. The block was already
fully commented out and served no purpose.

```xml
<!-- Removed (was already commented out): -->
<!-- test persistence -->
<!--<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-entitymanager</artifactId>
  <scope>test</scope>
</dependency>
... -->
```

---

### 3. `kie-osgi/kie-karaf-features/pom.xml`

**Action:** Removed `hibernate-entitymanager` (provided scope) — `hibernate-core` was
**already declared** immediately above it with the same `provided` scope.

```xml
<!-- Removed: -->
<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-entitymanager</artifactId>
  <scope>provided</scope>
</dependency>
```

---

### 4. `kie-server-parent/kie-server-services/kie-server-services-jbpm/pom.xml`

**Action:** Replaced `hibernate-entitymanager` with `hibernate-core` — `hibernate-core` was
**not** previously declared in this module.

- Scope preserved: `test`

```xml
<!-- Before -->
<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-entitymanager</artifactId>
  <scope>test</scope>
</dependency>

<!-- After -->
<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-core</artifactId>
  <scope>test</scope>
</dependency>
```

---

### 5. `kie-server-parent/kie-server-wars/kie-server/pom.xml`

Two occurrences handled:

#### 5a. Dependency block (webcontainer dependencies)

**Action:** Removed `hibernate-entitymanager` — `hibernate-core` was **already declared**
directly below it. The `org.dom4j:dom4j` exclusion carried by `hibernate-entitymanager`
was merged into the existing `hibernate-core` entry.

```xml
<!-- Before -->
<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-entitymanager</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.dom4j</groupId>
      <artifactId>dom4j</artifactId>
    </exclusion>
  </exclusions>
</dependency>
<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-core</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.glassfish.jaxb</groupId>
      <artifactId>jaxb-runtime</artifactId>
    </exclusion>
  </exclusions>
</dependency>

<!-- After (dom4j exclusion merged in) -->
<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-core</artifactId>
  <exclusions>
    <exclusion>
      <groupId>org.glassfish.jaxb</groupId>
      <artifactId>jaxb-runtime</artifactId>
    </exclusion>
    <exclusion>
      <groupId>org.dom4j</groupId>
      <artifactId>dom4j</artifactId>
    </exclusion>
  </exclusions>
</dependency>
```

#### 5b. `maven-dependency-plugin` `copy-hibernate-for-ee7` execution

**Action:** Removed the `hibernate-entitymanager` `<artifactItem>` — `hibernate-core`
was **already present** as the next item in the same `<artifactItems>` list.

```xml
<!-- Removed artifactItem: -->
<artifactItem>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-entitymanager</artifactId>
  <version>${version.org.hibernate-4ee7}</version>
  <type>jar</type>
</artifactItem>
```

---

### 6. `kie-spring-boot/kie-spring-boot-autoconfiguration/jbpm-spring-boot-autoconfiguration/pom.xml`

**Action:** Removed `hibernate-entitymanager` — `hibernate-core` was **already declared**
immediately below it, both without an explicit scope (compile).

```xml
<!-- Removed: -->
<dependency>
  <groupId>org.hibernate</groupId>
  <artifactId>hibernate-entitymanager</artifactId>
</dependency>
```

---

## Summary Table

| pom.xml | Action | hibernate-core already present? |
|---|---|---|
| `kie-spring/pom.xml` | Replaced with `hibernate-core` (test) | No — added |
| `kie-aries-blueprint/pom.xml` | Removed (was already commented out) | N/A |
| `kie-osgi/kie-karaf-features/pom.xml` | Removed | Yes |
| `kie-server-services/kie-server-services-jbpm/pom.xml` | Replaced with `hibernate-core` (test) | No — added |
| `kie-server-wars/kie-server/pom.xml` (dep + plugin) | Removed + dom4j exclusion merged; plugin artifactItem removed | Yes |
| `jbpm-spring-boot-autoconfiguration/pom.xml` | Removed | Yes |
