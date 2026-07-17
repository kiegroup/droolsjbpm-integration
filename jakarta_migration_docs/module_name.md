# `java.module.name` Property Map

Complete map of all **120** `pom.xml` files carrying a `java.module.name` property across this
repository, grouped by module area. All values are unique — no duplicates.

**Legend**
- 🆕 `added` — property introduced in this session
- 🔧 `fixed` — pre-existing value was a duplicate and has been corrected
- ✅ `pre-existing` — property was already present before this session

---

## Duplicate check result

✅ **No duplicate `java.module.name` values** across all 120 modules.

> One pre-existing duplicate was found and corrected:
> `kie-server-integ-tests-scenario-simulation` incorrectly carried
> `org.kie.server.itests.dmn` (also assigned to `kie-server-integ-tests-dmn`).
> It has been fixed to `org.kie.server.itests.scenariosimulation`. 🔧

---

## camel-container-tests

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| 🆕 | `org.drools:camel-container-integration-tests` | `org.drools.camel.container.integration.tests` | `camel-container-tests/camel-container-integration-tests/pom.xml` |
| 🆕 | `org.drools:camel-container-tests-api` | `org.drools.camel.container.tests.api` | `camel-container-tests/camel-container-tests-api/pom.xml` |
| 🆕 | `org.drools:camel-container-tests-model` | `org.drools.camel.container.tests.model` | `camel-container-tests/camel-container-tests-model/pom.xml` |
| 🆕 | `org.drools:camel-container-tests-module` | `org.drools.camel.container.tests.module` | `camel-container-tests/camel-container-tests-module/pom.xml` |

---

## Distribution

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| 🆕 | `org.drools:droolsjbpm-bpms-distribution` | `org.drools.bpms.distribution` | `droolsjbpm-bpms-distribution/pom.xml` |
| 🆕 | `org.drools:droolsjbpm-brms-distribution` | `org.drools.brms.distribution` | `droolsjbpm-brms-distribution/pom.xml` |
| 🆕 | `org.drools:droolsjbpm-integration-distribution` | `org.drools.integration.distribution` | `droolsjbpm-integration-distribution/pom.xml` |
| ✅ | `org.drools:droolsjbpm-integration-examples` | `org.droolsjbpm.integration.examples` | `droolsjbpm-integration-examples/pom.xml` |

---

## jbpm-process-svg / jbpm-simulation

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie:jbpm-process-svg` | `org.jbpm.process.svg` | `jbpm-process-svg/pom.xml` |
| ✅ | `org.drools:jbpm-simulation` | `org.jbpm.simulation` | `jbpm-simulation/pom.xml` |

---

## kie-aries-blueprint / kie-camel / kie-spring

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie:kie-aries-blueprint` | `org.kie.aries.blueprint` | `kie-aries-blueprint/pom.xml` |
| ✅ | `org.kie:kie-camel` | `org.kie.camel` | `kie-camel/pom.xml` |
| ✅ | `org.kie:kie-spring` | `org.kie.spring` | `kie-spring/pom.xml` |

---

## kie-integration-test-coverage

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| 🆕 | `org.drools.testcoverage:kjar-with-instrumentation-default-execmodel-wrapper` | `org.drools.testcoverage.kjar.default.execmodel.wrapper` | `kie-integration-test-coverage/kie-maven-plugin-default-execmodel-tests/kjar-with-instrumentation-default-execmodel-wrapper/pom.xml` |
| 🆕 | `org.drools.testcoverage:kie-maven-plugin-default-execmodel-test-suite` | `org.drools.testcoverage.default.execmodel.test.suite` | `kie-integration-test-coverage/kie-maven-plugin-default-execmodel-tests/test-suite/pom.xml` |
| 🆕 | `org.drools.testcoverage:kjar-with-instrumentation-no-execmodel-wrapper` | `org.drools.testcoverage.kjar.no.execmodel.wrapper` | `kie-integration-test-coverage/kie-maven-plugin-no-execmodel-tests/kjar-with-instrumentation-no-execmodel-wrapper/pom.xml` |
| 🆕 | `org.drools.testcoverage:kie-maven-plugin-no-execmodel-test-suite` | `org.drools.testcoverage.no.execmodel.test.suite` | `kie-integration-test-coverage/kie-maven-plugin-no-execmodel-tests/test-suite/pom.xml` |
| 🆕 | `org.drools.testcoverage:kjar-with-separate-instrumentation-wrapper` | `org.drools.testcoverage.kjar.separate.model.wrapper` | `kie-integration-test-coverage/kie-maven-plugin-separate-model-tests/kjar-with-separate-instrumentation-wrapper/pom.xml` |
| 🆕 | `org.drools.testcoverage:model-wrapper` | `org.drools.testcoverage.separate.model.wrapper` | `kie-integration-test-coverage/kie-maven-plugin-separate-model-tests/model-wrapper/pom.xml` |
| 🆕 | `org.drools.testcoverage:kie-maven-plugin-separate-model-test-suite` | `org.drools.testcoverage.separate.model.test.suite` | `kie-integration-test-coverage/kie-maven-plugin-separate-model-tests/test-suite/pom.xml` |
| 🆕 | `org.drools.testcoverage:kie-spring-test` | `org.drools.testcoverage.spring.test` | `kie-integration-test-coverage/kie-spring-test/pom.xml` |

---

## kie-maven-plugin / kie-maven-project-example / kie-plugins-testing / kie-takari-plugin

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| 🆕 | `org.kie:kie-maven-plugin` | `org.kie.maven.plugin` | `kie-maven-plugin/pom.xml` |
| 🆕 | `org.kie:kie-maven-project-example` | `org.kie.maven.project.example` | `kie-maven-project-example/pom.xml` |
| 🆕 | `org.kie:kie-plugin-testing` | `org.kie.plugin.testing` | `kie-plugins-testing/pom.xml` |
| 🆕 | `org.kie:kie-takari-plugin` | `org.kie.takari.plugin` | `kie-takari-plugin/pom.xml` |

---

## kie-osgi

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie:kie-karaf-features` | `org.kie.karaf.features` | `kie-osgi/kie-karaf-features/pom.xml` |
| ✅ | `org.kie:kie-karaf-itests-domain-model` | `org.kie.karaf.itests.domain.model` | `kie-osgi/kie-karaf-itests-domain-model/pom.xml` |
| ✅ | `org.kie:kie-karaf-itests` | `org.kie.karaf.itests.main` | `kie-osgi/kie-karaf-itests/pom.xml` |
| ✅ | `org.kie:kie-osgi-integration` | `org.kie.osgi.integration` | `kie-osgi/kie-osgi-integration/pom.xml` |
| 🆕 | `org.kie:kie-osgi-servlet3-compatibility` | `org.kie.osgi.servlet3.compatibility` | `kie-osgi/kie-osgi-servlet3-compatibility/pom.xml` |

---

## kie-performance-kit / kie-swagger-ui / kie-tomcat-integration

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie:kie-performance-kit` | `org.kie.performance.kit` | `kie-performance-kit/pom.xml` |
| ✅ | `org.kie:kie-swagger-ui` | `org.kie.swagger` | `kie-swagger-ui/pom.xml` |
| ✅ | `org.kie:kie-tomcat-integration` | `org.kie.tomcat` | `kie-tomcat-integration/pom.xml` |

---

## kie-server-parent — API / common

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie.server:kie-server-api` | `org.kie.server.api` | `kie-server-parent/kie-server-api/pom.xml` |
| ✅ | `org.kie.server:kie-server-common` | `org.kie.server.common` | `kie-server-parent/kie-server-common/pom.xml` |
| 🆕 | `org.kie.server:kie-server-controller-plugin` | `org.kie.server.controller.plugin` | `kie-server-parent/kie-server-controller-plugin/pom.xml` |
| 🆕 | `org.kie.server:kie-server-maven-plugin` | `org.kie.server.maven.plugin` | `kie-server-parent/kie-server-maven-plugin/pom.xml` |

---

## kie-server-parent — controller

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie.server:kie-server-controller-api` | `org.kie.server.controller.api` | `kie-server-parent/kie-server-controller/kie-server-controller-api/pom.xml` |
| ✅ | `org.kie.server:kie-server-controller-client` | `org.kie.server.controller.client` | `kie-server-parent/kie-server-controller/kie-server-controller-client/pom.xml` |
| ✅ | `org.kie.server:kie-server-controller-impl` | `org.kie.server.controller.impl` | `kie-server-parent/kie-server-controller/kie-server-controller-impl/pom.xml` |
| ✅ | `org.kie.server:kie-server-controller-openshift` | `org.kie.server.controller.openshift` | `kie-server-parent/kie-server-controller/kie-server-controller-openshift/pom.xml` |
| ✅ | `org.kie.server:kie-server-controller-rest` | `org.kie.server.controller.rest` | `kie-server-parent/kie-server-controller/kie-server-controller-rest/pom.xml` |
| ✅ | `org.kie.server:kie-server-controller-standalone-impl` | `org.kie.server.controller.standalone.impl` | `kie-server-parent/kie-server-controller/kie-server-controller-standalone-impl/pom.xml` |
| ✅ | `org.kie.server:kie-server-controller-standalone` | `org.kie.server.controller.standalone.wars` | `kie-server-parent/kie-server-controller/kie-server-controller-standalone/pom.xml` |
| ✅ | `org.kie.server:kie-server-controller-websocket-client` | `org.kie.server.controller.websocket.client` | `kie-server-parent/kie-server-controller/kie-server-controller-websocket-client/pom.xml` |
| ✅ | `org.kie.server:kie-server-controller-websocket-common` | `org.kie.server.controller.websocket.common` | `kie-server-parent/kie-server-controller/kie-server-controller-websocket-common/pom.xml` |
| ✅ | `org.kie.server:kie-server-controller-websocket` | `org.kie.server.controller.websocket.impl` | `kie-server-parent/kie-server-controller/kie-server-controller-websocket/pom.xml` |

---

## kie-server-parent — remote (client / JMS / REST)

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie.server:kie-server-client` | `org.kie.server.client` | `kie-server-parent/kie-server-remote/kie-server-client/pom.xml` |
| ✅ | `org.kie.server:kie-server-jms` | `org.kie.server.jms` | `kie-server-parent/kie-server-remote/kie-server-jms/pom.xml` |
| ✅ | `org.kie.server:kie-server-rest-case-mgmt` | `org.kie.server.rest.casemgmt` | `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-case-mgmt/pom.xml` |
| ✅ | `org.kie.server:kie-server-rest-common` | `org.kie.server.rest.common` | `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-common/pom.xml` |
| ✅ | `org.kie.server:kie-server-rest-dmn` | `org.kie.server.rest.dmn` | `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-dmn/pom.xml` |
| ✅ | `org.kie.server:kie-server-rest-drools` | `org.kie.server.rest.drools` | `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-drools/pom.xml` |
| ✅ | `org.kie.server:kie-server-rest-jbpm-ui` | `org.kie.server.rest.jbpm.ui` | `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-jbpm-ui/pom.xml` |
| ✅ | `org.kie.server:kie-server-rest-jbpm` | `org.kie.server.rest.jbpm.core` | `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-jbpm/pom.xml` |
| ✅ | `org.kie.server:kie-server-rest-prometheus` | `org.kie.server.rest.prometheus` | `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-prometheus/pom.xml` |
| ✅ | `org.kie.server:kie-server-rest-scenario-simulation` | `org.kie.server.rest.scenariosimulation` | `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-scenario-simulation/pom.xml` |
| ✅ | `org.kie.server:kie-server-rest-swagger` | `org.kie.server.rest.swagger` | `kie-server-parent/kie-server-remote/kie-server-rest/kie-server-rest-swagger/pom.xml` |

---

## kie-server-parent — router

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie.server:kie-server-router-client` | `org.kie.server.router.client` | `kie-server-parent/kie-server-router/kie-server-router-client/pom.xml` |
| ✅ | `org.kie.server:kie-server-router-proxy` | `org.kie.server.router.proxy` | `kie-server-parent/kie-server-router/kie-server-router-proxy/pom.xml` |

---

## kie-server-parent — services

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie.server:kie-server-services-case-mgmt` | `org.kie.server.services.casemgmt` | `kie-server-parent/kie-server-services/kie-server-services-case-mgmt/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-common` | `org.kie.server.services.common` | `kie-server-parent/kie-server-services/kie-server-services-common/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-dmn` | `org.kie.server.services.dmn` | `kie-server-parent/kie-server-services/kie-server-services-dmn/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-drools` | `org.kie.server.services.drools` | `kie-server-parent/kie-server-services/kie-server-services-drools/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-jbpm-cluster` | `org.kie.server.services.jbpm.cluster` | `kie-server-parent/kie-server-services/kie-server-services-jbpm-cluster/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-jbpm-ui` | `org.kie.server.services.jbpm.ui` | `kie-server-parent/kie-server-services/kie-server-services-jbpm-ui/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-jbpm` | `org.kie.server.services.jbpm.core` | `kie-server-parent/kie-server-services/kie-server-services-jbpm/pom.xml` |
| 🆕 | `org.kie.server:kie-server-services-kafka` | `org.kie.server.services.kafka` | `kie-server-parent/kie-server-services/kie-server-services-kafka/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-openshift` | `org.kie.server.services.openshift` | `kie-server-parent/kie-server-services/kie-server-services-openshift/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-prometheus` | `org.kie.server.services.prometheus` | `kie-server-parent/kie-server-services/kie-server-services-prometheus/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-scenario-simulation` | `org.kie.server.services.scenariosimulation` | `kie-server-parent/kie-server-services/kie-server-services-scenario-simulation/pom.xml` |
| ✅ | `org.kie.server:kie-server-services-swagger` | `org.kie.server.services.swagger` | `kie-server-parent/kie-server-services/kie-server-services-swagger/pom.xml` |

---

## kie-server-parent — tests

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie.server:kie-server-integ-tests-all` | `org.kie.server.itests.all` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-all/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-case-id-generator` | `org.kie.server.itests.caseidgenerator` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-case-id-generator/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-common` | `org.kie.server.itests.common` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-common/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-controller` | `org.kie.server.itests.controller` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-controller/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-custom-extension-client` | `org.kie.server.itests.custom.extension.client` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-custom-extension/kie-server-integ-tests-custom-extension-client/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-custom-extension-prometheus` | `org.kie.server.itests.custom.extension.prometheus` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-custom-extension/kie-server-integ-tests-custom-extension-prometheus/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-custom-extension-rest` | `org.kie.server.itests.custom.extension.rest` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-custom-extension/kie-server-integ-tests-custom-extension-rest/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-custom-extension-services` | `org.kie.server.itests.custom.extension.service` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-custom-extension/kie-server-integ-tests-custom-extension-services/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-custom-extension-test` | `org.kie.server.itests.custom.extension.test` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-custom-extension/kie-server-integ-tests-custom-extension-test/pom.xml` |
| 🆕 | `org.kie.server:kie-server-integ-tests-custom-extension` | `org.kie.server.itests.custom.extension` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-custom-extension/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-dmn-pmml-trusty` | `org.kie.server.itests.dmn.pmml-trusty` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-dmn-pmml-trusty/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-dmn-pmml` | `org.kie.server.itests.dmn.pmml` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-dmn-pmml/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-dmn` | `org.kie.server.itests.dmn` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-dmn/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-drools` | `org.kie.server.itests.drools` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-drools/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-jbpm` | `org.kie.server.itests.jbpm.core` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-jbpm/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-pmml-legacy` | `org.kie.server.itests.pmml-legacy` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-pmml-legacy/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-pmml` | `org.kie.server.itests.pmml` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-pmml/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-policy` | `org.kie.server.itests.policy` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-policy/pom.xml` |
| ✅ | `org.kie.server:kie-server-integ-tests-router` | `org.kie.server.itests.router` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-router/pom.xml` |
| 🔧 | `org.kie.server:kie-server-integ-tests-scenario-simulation` | `org.kie.server.itests.scenariosimulation` | `kie-server-parent/kie-server-tests/kie-server-integ-tests-scenario-simulation/pom.xml` |
| 🆕 | `org.kie.server:kie-server-test-web-service` | `org.kie.server.itests.test.web.service` | `kie-server-parent/kie-server-tests/kie-server-test-web-service/pom.xml` |
| 🆕 | `org.kie.server:kie-server-tests` | `org.kie.server.tests` | `kie-server-parent/kie-server-tests/pom.xml` |

---

## kie-server-parent — wars

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| ✅ | `org.kie.server:kie-server-distribution` | `org.kie.server.distribution.archive` | `kie-server-parent/kie-server-wars/kie-server-distribution/pom.xml` |
| ✅ | `org.kie.server:kie-server` | `org.kie.server.distribution.wars` | `kie-server-parent/kie-server-wars/kie-server/pom.xml` |

---

## kie-spring-boot — autoconfiguration

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| 🆕 | `org.kie:jbpm-spring-boot-autoconfiguration` | `org.kie.jbpm.spring.boot.autoconfiguration` | `kie-spring-boot/kie-spring-boot-autoconfiguration/jbpm-spring-boot-autoconfiguration/pom.xml` |
| 🆕 | `org.kie:jbpm-spring-boot-data-sources` | `org.kie.jbpm.spring.boot.data.sources` | `kie-spring-boot/kie-spring-boot-autoconfiguration/jbpm-spring-boot-data-sources/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-autoconfiguration-audit-replication` | `org.kie.server.spring.boot.autoconfiguration.audit.replication` | `kie-spring-boot/kie-spring-boot-autoconfiguration/kie-server-spring-boot-autoconfiguration-audit-replication/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-autoconfiguration-drools` | `org.kie.server.spring.boot.autoconfiguration.drools` | `kie-spring-boot/kie-spring-boot-autoconfiguration/kie-server-spring-boot-autoconfiguration-drools/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-autoconfiguration-jbpm` | `org.kie.server.spring.boot.autoconfiguration.jbpm` | `kie-spring-boot/kie-spring-boot-autoconfiguration/kie-server-spring-boot-autoconfiguration-jbpm/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-autoconfiguration` | `org.kie.server.spring.boot.autoconfiguration` | `kie-spring-boot/kie-spring-boot-autoconfiguration/kie-server-spring-boot-autoconfiguration/pom.xml` |

---

## kie-spring-boot — samples

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| 🆕 | `org.kie:jbpm-spring-boot-sample-basic` | `org.kie.jbpm.spring.boot.sample.basic` | `kie-spring-boot/kie-spring-boot-samples/jbpm-spring-boot-sample-basic/pom.xml` |
| 🆕 | `org.kie:keycloak-kie-server-spring-boot-sample` | `org.kie.keycloak.server.spring.boot.sample` | `kie-spring-boot/kie-spring-boot-samples/keycloak-kie-server-spring-boot-sample/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-integ-tests-sample` | `org.kie.server.spring.boot.integ.tests.sample` | `kie-spring-boot/kie-spring-boot-samples/kie-server-spring-boot-integ-tests-sample/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-kafka-sample` | `org.kie.server.spring.boot.kafka.sample` | `kie-spring-boot/kie-spring-boot-samples/kie-server-spring-boot-kafka-sample/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-sample` | `org.kie.server.spring.boot.sample` | `kie-spring-boot/kie-spring-boot-samples/kie-server-spring-boot-sample/pom.xml` |
| 🆕 | `org.kie.server.testing:case-insurance` | `org.kie.server.testing.case.insurance` | `kie-spring-boot/kie-spring-boot-samples/kjars/case-insurance/pom.xml` |
| 🆕 | `org.jbpm.test:evaluation` | `org.jbpm.test.evaluation` | `kie-spring-boot/kie-spring-boot-samples/kjars/evaluation/pom.xml` |
| 🆕 | `org.jbpm:intermediate-timer-sample` | `org.jbpm.intermediate.timer.sample` | `kie-spring-boot/kie-spring-boot-samples/kjars/intermediate-timer-sample/pom.xml` |
| 🆕 | `org.jbpm:notification` | `org.jbpm.notification` | `kie-spring-boot/kie-spring-boot-samples/kjars/notification/pom.xml` |
| 🆕 | `org.jbpm:timer-in-process-migration` | `org.jbpm.timer.in.process.migration` | `kie-spring-boot/kie-spring-boot-samples/kjars/timer-in-process-migration/v1/pom.xml` |
| 🆕 | `org.jbpm:timer-in-process-migration` | `org.jbpm.timer.in.process.migration2` | `kie-spring-boot/kie-spring-boot-samples/kjars/timer-in-process-migration/v2/pom.xml` |
| 🆕 | `org.jbpm.test:timer` | `org.jbpm.test.timer` | `kie-spring-boot/kie-spring-boot-samples/kjars/timer/pom.xml` |
| 🆕 | `org.jbpm:timer-in-process-migration` | `org.jbpm.timer.in.process.migration.sample` | `kie-spring-boot/kie-spring-boot-samples/timer-in-process-migration/pom.xml` |
| 🆕 | `org.jbpm:timer-in-process-migration` | `org.jbpm.timer.in.process.migration2.sample` | `kie-spring-boot/kie-spring-boot-samples/timer-in-process-migration2/pom.xml` |

---

## kie-spring-boot — starters

| Status | `groupId:artifactId` | `java.module.name` | pom.xml |
|--------|---------------------|--------------------|---------|
| 🆕 | `org.kie:jbpm-spring-boot-starter-basic` | `org.kie.jbpm.spring.boot.starter.basic` | `kie-spring-boot/kie-spring-boot-starters/jbpm-spring-boot-starter-basic/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-starter-drools` | `org.kie.server.spring.boot.starter.drools` | `kie-spring-boot/kie-spring-boot-starters/kie-server-spring-boot-starter-drools/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-starter-jbpm` | `org.kie.server.spring.boot.starter.jbpm` | `kie-spring-boot/kie-spring-boot-starters/kie-server-spring-boot-starter-jbpm/pom.xml` |
| 🆕 | `org.kie:kie-server-spring-boot-starter` | `org.kie.server.spring.boot.starter` | `kie-spring-boot/kie-spring-boot-starters/kie-server-spring-boot-starter/pom.xml` |

---

## Statistics

| Category | Count |
|---|---|
| 🆕 Added in this session | 51 |
| 🔧 Fixed pre-existing duplicate | 1 |
| ✅ Pre-existing (unchanged) | 68 |
| **Total** | **120** |
