# Technical Report: Resolution of Test Failures in `kie-camel`

**Project:** BAMOE 8 / Drools & jBPM Integration  
**Module:** [`kie-camel`](kie-camel/)  
**Target Environment:** Apache Camel 4.18.4 | Jakarta EE 10 | Spring 6 | JUnit 5  
**Final Test Status:** **109 Tests Run, 0 Failures, 0 Errors, 0 Skipped (100% Pass Rate)**

---

## 1. Executive Summary

During the upgrade of the BAMOE 8 platform to **Apache Camel 4.18.4** and **Jakarta EE 10**, the `kie-camel` integration module faced **28 distinct failing test cases** across embedded endpoints, SOAP/REST CXF transports, and data marshaling subsystems. 

The failures were root-caused to major architectural and breaking changes across:
1. **Camel 4 Core Split & Relocations**: Reorganization into modular subcomponents (`camel-core-support`, `camel-endpointsupport`, etc.) and API signature updates.
2. **Removal of `camel-xstream` from Camel 4**: Complete elimination of Camel's legacy XStream DataFormat support.
3. **Endpoint Session Caching vs. Dynamic Rebinding**: Static registry caching preventing JUnit test suites from re-binding new `KieSession` instances across test methods.
4. **Removal of `http://camel.apache.org/schema/cxf` Spring XML Namespace**: Deprecation and removal of XML schema configurations for CXF RS and SOAP endpoints.
5. **Jakarta EE 10 Namespace Migration**: Replacement of legacy `javax.xml.soap.*` with `jakarta.xml.soap.*`.
6. **MVEL & Modern XStream Serialization Alignment**: Security permission hardening and structural XML output changes in modern XStream.

All 28 failing test cases were successfully analyzed, refactored, and verified with zero regressions.

---

## 2. Root Cause Analysis & Technical Solutions

### 2.1 Implementation of Custom `DroolsXStreamDataFormat` (Camel 4 Compatibility)
- **Problem:**
  - Camel 4 removed the built-in `camel-xstream` component.
  - Previous Drools integrations relied directly on `org.apache.camel.model.dataformat.XStreamDataFormat` and `org.apache.camel.dataformat.xstream.XStreamDataFormat`, which no longer existed in Camel 4 dependencies.
  - Drools commands (`BatchExecutionCommandImpl`, `InsertElementsCommand`, `SetGlobalCommand`, etc.) failed during `.marshal()` and `.unmarshal()` operations.
- **Solution:**
  - Implemented [`DroolsXStreamDataFormat.java`](kie-camel/src/main/java/org/kie/camel/embedded/component/DroolsXStreamDataFormat.java) as a native Camel 4 `DataFormat` / `DataFormatDefinition` bridge.
  - Refactored [`XStreamHelper.java`](kie-camel/src/main/java/org/kie/camel/embedded/component/XStreamHelper.java), [`XStreamXml.java`](kie-camel/src/main/java/org/kie/camel/embedded/component/XStreamXml.java), and [`XStreamJson.java`](kie-camel/src/main/java/org/kie/camel/embedded/component/XStreamJson.java) to configure Drools converters (`BatchExecutionResultConverter`, `CommandConverter`, etc.) and security permission filters directly on the underlying `XStream` engine.
  - Updated [`xstream.mvt`](kie-camel/src/test/resources/org/kie/camel/embedded/camel/component/xstream.mvt) templates to match modern XStream XML serialization tags.

---

### 2.2 Dynamic Session Lookup in `KieEmbeddedEndpoint`
- **Problem:**
  - In JUnit test suites (`CamelEndpointTest`, `BatchTest`, `XStreamBatchExecutionTest`), each `@BeforeEach` / `@Test` method creates and registers a fresh `KieSession` in the Camel registry under a known key (e.g., `"ksession1"` or `"ksession2"`).
  - `KieEmbeddedEndpoint` was previously looking up and storing a static `CommandExecutor` field during Camel endpoint initialization. When subsequent tests bound a new session instance to the registry, the endpoint continued invoking the stale/closed session instance from the first test.
- **Solution:**
  - Modified [`KieEmbeddedEndpoint.java`](kie-camel/src/main/java/org/kie/camel/embedded/component/KieEmbeddedEndpoint.java) method `getExecutor()`:
    ```java
    public CommandExecutor getExecutor() {
        if (ksessionId != null && executorsByName == null) {
            return getCamelContext().getRegistry().lookupByNameAndType(ksessionId, CommandExecutor.class);
        }
        return executor;
    }
    ```
  - Changed `createExchange(Object pojo)` to correctly bind `CamelContext` to `DefaultExchange` and `DefaultMessage`.

---

### 2.3 `KiePolicy` and Route Augmentation in Camel 4
- **Problem:**
  - `RouteContext` was removed from `org.apache.camel.spi.Policy` in Camel 4. Signatures changed from `beforeWrap(RouteContext, ProcessorDefinition)` to `beforeWrap(Route, NamedNode)` and `wrap(Route, Processor)`.
  - `resolveDataFormatDefinition(ref)` was removed from `CamelContext`.
  - HTTP headers in Camel 4 standardized from `Exchange.ACCEPT_CONTENT_TYPE` to standard HTTP `"Accept"`.
- **Solution:**
  - Updated [`KiePolicy.java`](kie-camel/src/main/java/org/kie/camel/embedded/component/KiePolicy.java) to implement the Camel 4 `Policy` interface.
  - Updated route augmentation logic to dynamically discover and clone `DroolsXStreamDataFormat` and `JaxbDataFormat` definitions.
  - Updated REST client preparation processors to use `"Accept"` headers.

---

### 2.4 Removal of Spring XML & Programmatic CXF Migration
- **Problem:**
  - Tests like `CxfRestTest`, `CxfSoapTest`, `CxfRestWithLookupTest`, `CxfRestWithImportTest`, and `CxfSoapWithLookupTest` relied on `CamelSpringTestSupport` and legacy Spring XML files containing `<cxf:cxfEndpoint>` and `<cxf:rsServer>`.
  - The XML namespace `http://camel.apache.org/schema/cxf` was dropped in Camel 4.
- **Solution:**
  - Migrated all CXF test classes to pure Java `RouteBuilder` configurations extending JUnit 5 `CamelTestSupport`.
  - Programmatically instantiated and bound `CxfRsEndpoint` and `CxfEndpoint` (JAX-WS) within `bindToRegistry(Registry registry)`.
  - Registered `CommandMessageBodyReader` in the registry for automatic URI parameter resolution (`providers=#commandMessageBodyReader`).

---

### 2.5 Jakarta EE 10 SOAP Migration
- **Problem:**
  - CXF SOAP processors used legacy `javax.xml.soap.*` classes causing classloader and linkage errors on modern runtimes.
- **Solution:**
  - Migrated [`PreCxfSoapProcessor.java`](kie-camel/src/main/java/org/kie/jax/soap/PreCxfSoapProcessor.java), [`PostCxfSoapProcessor.java`](kie-camel/src/main/java/org/kie/jax/soap/PostCxfSoapProcessor.java), [`PreCxfTransportSoapProcessor.java`](kie-camel/src/main/java/org/kie/jax/soap/PreCxfTransportSoapProcessor.java), and [`PostCxfTransportSoapProcessor.java`](kie-camel/src/main/java/org/kie/jax/soap/PostCxfTransportSoapProcessor.java) to `jakarta.xml.soap.*`.
  - Updated payload extraction and header management to adhere to Jakarta SOAP standards.

---

### 2.6 JUnit 5 Jupiter & `CamelTestSupport` Migration
- **Problem:**
  - Camel 4 requires JUnit 5 (`org.apache.camel.test.junit5.CamelTestSupport`).
  - Legacy `JndiRegistry` / `createRegistry()` methods in Camel 2/3 tests were obsolete.
- **Solution:**
  - Modernized [`KieCamelTestSupport.java`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/KieCamelTestSupport.java) and [`BaseKieComponentTest.java`](kie-camel/src/test/java/org/kie/camel/BaseKieComponentTest.java) to JUnit 5 `@BeforeEach`, `@AfterEach`, and overridden `bindToRegistry(Registry registry)`.

---

## 3. Comprehensive Test Case Resolution Matrix

| # | Test Class | Methods Resolved | Count | Root Cause & Resolution |
|---|---|---|:---:|---|
| **1** | [`CxfRestTest`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/cxf/CxfRestTest.java) | `test1` | 1 | Replaced deprecated Spring XML with programmatic `CxfRsEndpoint` route wiring; registered `CommandMessageBodyReader`. |
| **2** | [`CxfRestWithLookupTest`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/cxf/CxfRestWithLookupTest.java) | `test1` | 1 | Programmatic CXF-RS route with dynamic session lookup from Camel registry. |
| **3** | [`CxfRestWithImportTest`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/cxf/CxfRestWithImportTest.java) | `test1` | 1 | Programmatic REST pipeline with `DroolsXStreamDataFormat` marshaling. |
| **4** | [`CxfSoapTest`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/cxf/CxfSoapTest.java) | `test1` | 1 | Migrated to `jakarta.xml.soap.*`, programmatic `CxfEndpoint` (JAX-WS), fixed `KiePolicy` SOAP processor wrapping. |
| **5** | [`CxfSoapWithLookupTest`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/cxf/CxfSoapWithLookupTest.java) | `test1` | 1 | Jakarta SOAP message envelope parsing and dynamic session resolver. |
| **6** | [`CamelEndpointTest`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/CamelEndpointTest.java) | `testNoSessionInsert`<br>`testNoSessionInsertWithHeaders`<br>`testSessionInsert`<br>`testSessionInsertWithHeaders`<br>`testSessionBatchExecutionCommand`<br>`testSessionBatchExecutionCommandWithHeader`<br>`testSessionGetObject` | 7 | Dynamic session lookup in `KieEmbeddedEndpoint.getExecutor()`; fixed Camel 4 `DefaultExchange` / `DefaultMessage` instantiation. |
| **7** | [`CamelEndpointWithJaxbTest`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/CamelEndpointWithJaxbTest.java) | `testInsertElements`<br>`testSessionInsert`<br>`testSessionModify`<br>`testSessionGetObject`<br>`testProcess`<br>`testProcessInstanceSignalEvent`<br>`testQuery` | 7 | Fixed JAXB `DataFormatDefinition` cloning and runtime augmentation in `KiePolicy`. |
| **8** | [`BatchTest`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/BatchTest.java) | `testInsertObject`<br>`testInsertElements`<br>`testSetGlobal`<br>`testSetGlobalWithLookup`<br>`testFireAllRules` | 5 | Replaced missing `camel-xstream` with `DroolsXStreamDataFormat`; dynamic `CommandExecutor` lookup. |
| **9** | [`XStreamBatchExecutionTest`](kie-camel/src/test/java/org/kie/camel/embedded/camel/component/XStreamBatchExecutionTest.java) | `testInsertObject`<br>`testInsertElements`<br>`testSetGlobal`<br>`testFireAllRules` | 4 | Aligned MVEL template (`xstream.mvt`) formatting with modern XStream; configured XStream security permissions. |
| **Total** | | | **28** | **All 28 Test Cases Passing** |

---

## 4. Verification and Validation

The entire test suite was executed and validated with Apache Maven:

```bash
mvn clean test -f kiegroup-droolsjbpm-integration/kie-camel/pom.xml
```

### Execution Output:
```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.kie.camel.KieComponentOpOnUriTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.JsonQueryTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.CamelEndpointTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.CamelEndpointWithJaxWrapperCollectionTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.JaxbInsertTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.CamelEndpointWithJaxbTest
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.BatchTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.cxf.CxfRestTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.cxf.CxfSoapTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.cxf.CxfRestWithLookupTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.cxf.CxfSoapWithLookupTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.cxf.CxfRestWithImportTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running org.kie.camel.embedded.camel.component.XStreamBatchExecutionTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
...
[INFO] -------------------------------------------------------
[INFO] Results:
[INFO] 
[INFO] Tests run: 109, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
