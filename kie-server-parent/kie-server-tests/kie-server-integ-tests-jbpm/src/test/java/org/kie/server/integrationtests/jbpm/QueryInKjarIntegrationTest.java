/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.integrationtests.jbpm;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class QueryInKjarIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ClassLoader classLoader = QueryInKjarIntegrationTest.class.getClassLoader();

    private static ReleaseId releaseIdKjarOne = new ReleaseId("org.kie.server.testing", "query-definition-kjar-one", "1.0.0.Final");
    private static ReleaseId releaseIdKjarOneUpdated = new ReleaseId("org.kie.server.testing", "query-definition-kjar-one", "1.0.1.Final");
    private static ReleaseId releaseIdKjarTwo = new ReleaseId("org.kie.server.testing", "query-definition-kjar-two", "1.0.0.Final");
    private static ReleaseId releaseIdKjarDuplicateQueries = new ReleaseId("org.kie.server.testing", "query-definition-kjar-duplicate-queries", "1.0.0.Final");
    private static ReleaseId releaseIdKjarEmptyQueries = new ReleaseId("org.kie.server.testing", "query-definition-kjar-empty-queries", "1.0.0.Final");
    private static ReleaseId releaseIdKjarUnmarshallableQueries = new ReleaseId("org.kie.server.testing", "query-definition-kjar-unmarshallable-queries", "1.0.0.Final");

    private static final String KJAR_ONE_REGISTERED_QUERY = "first-query";
    private static final String KJAR_TWO_REGISTERED_QUERY = "second-query";
    private static final String KJAR_DUPLICATE_QUERIES_REGISTERED_QUERY = KJAR_TWO_REGISTERED_QUERY;
    private static final String KJAR_ONE_UPDATED_FIRST_REGISTERED_QUERY = "first-query";
    private static final String KJAR_ONE_UPDATED_SECOND_REGISTERED_QUERY = KJAR_TWO_REGISTERED_QUERY;

    private static final String EXPECTED_RESOLVED_DS = System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds");

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.createAndDeployKJar(releaseIdKjarOne, getQueryDefinitionKjarContentFromFile("query-definitions-kjar-one.json"));
        KieServerDeployer.createAndDeployKJar(releaseIdKjarOneUpdated, getQueryDefinitionKjarContentFromFile("query-definitions-kjar-one-updated.json"));
        KieServerDeployer.createAndDeployKJar(releaseIdKjarTwo, getQueryDefinitionKjarContentFromFile("query-definitions-kjar-two.json"));
        KieServerDeployer.createAndDeployKJar(releaseIdKjarDuplicateQueries, getQueryDefinitionKjarContentFromFile("query-definitions-kjar-duplicate-queries.json"));

        KieServerDeployer.createAndDeployKJar(releaseIdKjarEmptyQueries, getQueryDefinitionKjarContentFromValue(""));
        KieServerDeployer.createAndDeployKJar(releaseIdKjarUnmarshallableQueries, getQueryDefinitionKjarContentFromValue("unmarshallable content"));
    }

    @After
    public void deleteContainers() {
        KieContainerResourceList containers = client.listContainers().getResult();
        for (KieContainerResource container : containers.getContainers()) {
            client.disposeContainer(container.getContainerId());
        }
    }

    @Test
    public void testQueryDefinitionsFromKjar() throws Exception {
        String containerIdKjarOne = "container-one";
        String containerIdKjarTwo = "container-two";

        // Deploy container and check query
        KieServerAssert.assertSuccess(client.createContainer(containerIdKjarOne, new KieContainerResource(containerIdKjarOne, releaseIdKjarOne)));

        Map<String, QueryDefinition> queriesMap = retrieveQueryDefinitions();
        assertKjarOneQuery(queriesMap.get(KJAR_ONE_REGISTERED_QUERY));
        Assertions.assertThat(queriesMap.get(KJAR_TWO_REGISTERED_QUERY)).isNull();

        // Second container is registered with second query
        KieServerAssert.assertSuccess(client.createContainer(containerIdKjarTwo, new KieContainerResource(containerIdKjarTwo, releaseIdKjarTwo)));

        queriesMap = retrieveQueryDefinitions();
        assertKjarOneQuery(queriesMap.get(KJAR_ONE_REGISTERED_QUERY));
        assertKjarTwoQuery(queriesMap.get(KJAR_TWO_REGISTERED_QUERY));

        // Disposed container unregisters its query
        KieServerAssert.assertSuccess(client.disposeContainer(containerIdKjarOne));

        queriesMap = retrieveQueryDefinitions();
        Assertions.assertThat(queriesMap.get(KJAR_ONE_REGISTERED_QUERY)).isNull();
        assertKjarTwoQuery(queriesMap.get(KJAR_TWO_REGISTERED_QUERY));

        // Redeployed container registers its query again
        KieServerAssert.assertSuccess(client.createContainer(containerIdKjarOne, new KieContainerResource(containerIdKjarOne, releaseIdKjarOne)));

        queriesMap = retrieveQueryDefinitions();
        assertKjarOneQuery(queriesMap.get(KJAR_ONE_REGISTERED_QUERY));
        assertKjarTwoQuery(queriesMap.get(KJAR_TWO_REGISTERED_QUERY));
    }

    @Test
    public void testTwoQueriesWithSameName() throws Exception {
        String containerIdDuplicateQueries = "container-duplicate";

        KieServerAssert.assertSuccess(client.createContainer(containerIdDuplicateQueries, new KieContainerResource(containerIdDuplicateQueries, releaseIdKjarDuplicateQueries)));

        assertKjarWithDuplicateQueriesResultQuery(retrieveQueryDefinitions().get(KJAR_DUPLICATE_QUERIES_REGISTERED_QUERY));
    }

    @Test
    public void testDeployTwoContainersWithSameQueryName() throws Exception {
        String containerIdDuplicateQueries = "container-duplicate";
        String containerIdKjarTwo = "container-two";

        KieServerAssert.assertSuccess(client.createContainer(containerIdDuplicateQueries, new KieContainerResource(containerIdDuplicateQueries, releaseIdKjarDuplicateQueries)));

        assertKjarWithDuplicateQueriesResultQuery(retrieveQueryDefinitions().get(KJAR_TWO_REGISTERED_QUERY));

        KieServerAssert.assertSuccess(client.createContainer(containerIdKjarTwo, new KieContainerResource(containerIdKjarTwo, releaseIdKjarTwo)));

        assertKjarTwoQuery(retrieveQueryDefinitions().get(KJAR_TWO_REGISTERED_QUERY));
    }

    @Test
    public void testEmptyQueryDefinitions() throws Exception {
        String containerIdEmptyQueryDefinitions = "container-empty-definitions";

        int oldQueriesSize = queryClient.getQueries(0, 100).size();

        KieServerAssert.assertSuccess(client.createContainer(containerIdEmptyQueryDefinitions, new KieContainerResource(containerIdEmptyQueryDefinitions, releaseIdKjarEmptyQueries)));

        int newQueriesSize = queryClient.getQueries(0, 100).size();

        Assertions.assertThat(newQueriesSize).isEqualTo(oldQueriesSize);
    }

    @Test
    public void testUnmarshallableQueryDefinitions() throws Exception {
        String containerIdUnmarshallableQueryDefinitions = "container-unmarshallable-definitions";

        int oldQueriesSize = queryClient.getQueries(0, 100).size();

        KieServerAssert.assertSuccess(client.createContainer(containerIdUnmarshallableQueryDefinitions, new KieContainerResource(containerIdUnmarshallableQueryDefinitions, releaseIdKjarUnmarshallableQueries)));

        int newQueriesSize = queryClient.getQueries(0, 100).size();

        Assertions.assertThat(newQueriesSize).isEqualTo(oldQueriesSize);
    }

    @Test
    public void testUpdateContainerWithQueryDefinitionsFromKjar() throws Exception {
        String containerIdKjarOne = "container-one";

        KieServerAssert.assertSuccess(client.createContainer(containerIdKjarOne, new KieContainerResource(containerIdKjarOne, releaseIdKjarOne)));

        assertKjarOneQuery(retrieveQueryDefinitions().get(KJAR_ONE_REGISTERED_QUERY));

        // Update container
        KieServerAssert.assertSuccess(client.updateReleaseId(containerIdKjarOne, releaseIdKjarOneUpdated));

        assertKjarOneUpdatedQuery(retrieveQueryDefinitions().get(KJAR_ONE_UPDATED_FIRST_REGISTERED_QUERY));
        assertKjarTwoQuery(retrieveQueryDefinitions().get(KJAR_ONE_UPDATED_SECOND_REGISTERED_QUERY));
    }

    private void assertKjarOneQuery(QueryDefinition registeredQuery) {
        Assertions.assertThat(registeredQuery).isNotNull();
        Assertions.assertThat(registeredQuery.getName()).isEqualTo(KJAR_ONE_REGISTERED_QUERY);
        Assertions.assertThat(registeredQuery.getSource()).isEqualTo(EXPECTED_RESOLVED_DS);
        Assertions.assertThat(registeredQuery.getExpression()).isEqualTo("select * from ProcessInstanceLog");
        Assertions.assertThat(registeredQuery.getTarget()).isEqualTo("PROCESS");
    }

    private void assertKjarOneUpdatedQuery(QueryDefinition registeredQuery) {
        Assertions.assertThat(registeredQuery).isNotNull();
        Assertions.assertThat(registeredQuery.getName()).isEqualTo(KJAR_ONE_UPDATED_FIRST_REGISTERED_QUERY);
        Assertions.assertThat(registeredQuery.getSource()).isEqualTo(EXPECTED_RESOLVED_DS);
        Assertions.assertThat(registeredQuery.getExpression()).isEqualTo("select * from NodeInstanceLog");
        Assertions.assertThat(registeredQuery.getTarget()).isEqualTo("CUSTOM");
    }

    private void assertKjarTwoQuery(QueryDefinition registeredQuery) {
        Assertions.assertThat(registeredQuery).isNotNull();
        Assertions.assertThat(registeredQuery.getName()).isEqualTo(KJAR_TWO_REGISTERED_QUERY);
        Assertions.assertThat(registeredQuery.getSource()).isEqualTo(EXPECTED_RESOLVED_DS);
        Assertions.assertThat(registeredQuery.getExpression()).isEqualTo("select * from VariableInstanceLog");
        Assertions.assertThat(registeredQuery.getTarget()).isEqualTo("CUSTOM");
    }

    private void assertKjarWithDuplicateQueriesResultQuery(QueryDefinition registeredQuery) {
        Assertions.assertThat(registeredQuery).isNotNull();
        Assertions.assertThat(registeredQuery.getName()).isEqualTo(KJAR_DUPLICATE_QUERIES_REGISTERED_QUERY);
        Assertions.assertThat(registeredQuery.getSource()).isEqualTo(EXPECTED_RESOLVED_DS);
        Assertions.assertThat(registeredQuery.getExpression()).isEqualTo("select * from AuditTaskImpl where status = 'InProgress'");
        Assertions.assertThat(registeredQuery.getTarget()).isEqualTo("CUSTOM");
    }

    private static Map<String, String> getQueryDefinitionKjarContentFromFile(String queryDefinitionFileName) {
        String queryContent = readFile(queryDefinitionFileName, classLoader);

        return getQueryDefinitionKjarContentFromValue(queryContent);
    }

    private static Map<String, String> getQueryDefinitionKjarContentFromValue(String queryDefinitionContent) {
        String scriptProcess = readFile("script-process.bpmn2", classLoader);

        Map<String, String> kjarContent = new HashMap<>();
        kjarContent.put("src/main/resources/query-definitions.json", queryDefinitionContent);
        kjarContent.put("src/main/resources/script-process.bpmn2", scriptProcess);

        return kjarContent;
    }

    private static String readFile(String resourceName, ClassLoader classLoader) {
        try {
            URI resourceUri = classLoader.getResources(resourceName).nextElement().toURI();
            return new String(Files.readAllBytes(Paths.get(resourceUri)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String,QueryDefinition> retrieveQueryDefinitions() {
        List<QueryDefinition> queries = queryClient.getQueries(0, 100);
        return queries.stream().collect(Collectors.toMap(QueryDefinition::getName, Function.identity()));
    }
}
