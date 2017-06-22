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

package org.kie.server.integrationtests.jbpm.search;

import org.assertj.core.api.Assertions;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.api.model.definition.ProcessInstanceField;
import org.kie.server.api.model.definition.ProcessInstanceQueryFilterSpec;
import org.kie.server.api.util.ProcessInstanceQueryFilterSpecBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ProcessSearchServiceIntegrationTest extends JbpmQueriesKieServerBaseIntegrationTest{

    private static ReleaseId releaseId = new ReleaseId(GROUP_ID, CONTAINER_ID, VERSION);

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

   // @Test
    public void testFindProcessWithIncompatibleTypeFilter() throws Exception {
        assertClientException(
                () -> searchServicesClient.findProcessInstancesWithFilters(createQueryFilterEqualsTo(ProcessInstanceField.PROCESSID, 1), 0, 100),
                400,
                "Bad request");
    }

    @Test
    public void testFindProcessInstanceWithProcessNameEqualsToFilter() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
        Assertions.assertThat(processInstanceId).isNotNull();
        testFindProcessInstanceWithQueryFilter(createQueryFilterEqualsTo(ProcessInstanceField.PROCESSNAME, PROCESS_NAME_EVALUATION), processInstanceId);
    }

    @Test
    public void testFindProcessInstanceWithProcessInstanceIdEqualsToFilter() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
        Assertions.assertThat(processInstanceId).isNotNull();
        testFindProcessInstanceWithQueryFilter(createQueryFilterEqualsTo(ProcessInstanceField.PROCESSINSTANCEID, processInstanceId), processInstanceId);
    }

    @Test
    public void testFindProcessInstanceWithStartDateEqualsToFilter() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
        Assertions.assertThat(processInstanceId).isNotNull();
        ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
        testFindProcessInstanceWithQueryFilter(createQueryFilterEqualsTo(ProcessInstanceField.START_DATE, pi.getDate()), processInstanceId);
    }

    @Test
    public void testFindProcessInstanceWithCorrelationKeyEqualsToFilter() throws Exception {
       Map<String, Object> parameters = new HashMap<>();
       Long processInstanceId  = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
       Assertions.assertThat(processInstanceId).isNotNull();
       testFindProcessInstanceWithQueryFilter(createQueryFilterEqualsTo(ProcessInstanceField.CORRELATIONKEY, String.valueOf(processInstanceId)), processInstanceId);
    }

    @Test
    public void testFindProcessInstanceWithExternalIdAndPidEqualsToFilter() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
        Assertions.assertThat(processInstanceId).isNotNull();

        HashMap<ProcessInstanceField, Comparable<?>> compareList = new HashMap<>();
        compareList.put(ProcessInstanceField.EXTERNALID, CONTAINER_ID);
        compareList.put(ProcessInstanceField.PROCESSID, PROCESS_ID_EVALUATION);

        testFindProcessInstanceWithQueryFilter(createQueryFilterAndEqualsTo(compareList), processInstanceId);
    }

    @Test
    public void testFindProcessInstanceWithUserIdAndPidEqualsToFilter() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
        Assertions.assertThat(processInstanceId).isNotNull();

        HashMap<ProcessInstanceField, Comparable<?>> compareList = new HashMap<>();
        compareList.put(ProcessInstanceField.PROCESSID, PROCESS_ID_EVALUATION);
        compareList.put(ProcessInstanceField.USER_IDENTITY, USER_YODA);

        testFindProcessInstanceWithQueryFilter(createQueryFilterAndEqualsTo(compareList), processInstanceId);
    }

    @Test
    public void testFindProcessInstanceWithParentIdAndProcessNameEqualsToFilter() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        Long processInstanceId  = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
        Assertions.assertThat(processInstanceId).isNotNull();

        HashMap<ProcessInstanceField, Comparable<?>> compareList = new HashMap<>();
        compareList.put(ProcessInstanceField.PARENTPROCESSINSTANCEID, -1);
        compareList.put(ProcessInstanceField.PROCESSNAME, PROCESS_NAME_EVALUATION);

        testFindProcessInstanceWithQueryFilter(createQueryFilterAndEqualsTo(compareList), processInstanceId);
    }

    public void testFindProcessInstanceWithStatusEqualsToFilter() throws Exception {
        Map<String, Object> parameters = new HashMap<>();
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION, parameters);
        Assertions.assertThat(processInstanceId).isNotNull();
        testFindProcessInstanceWithQueryFilter(createQueryFilterEqualsTo(ProcessInstanceField.STATUS, 1), processInstanceId);
    }

    @Test
    public void testFindTaskWithAndEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_EVALUATION);
        Assertions.assertThat(processInstanceId).isNotNull();
        ProcessInstance process = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
        Assertions.assertThat(process).isNotNull();

        HashMap<ProcessInstanceField, Comparable<?>> compareList = new HashMap<>();
        compareList.put(ProcessInstanceField.PROCESSID, process.getProcessId());
        compareList.put(ProcessInstanceField.EXTERNALID, CONTAINER_ID);
        compareList.put(ProcessInstanceField.PROCESSINSTANCEID, processInstanceId);
        compareList.put(ProcessInstanceField.PROCESSINSTANCEDESCRIPTION, process.getProcessInstanceDescription());
        compareList.put(ProcessInstanceField.CORRELATIONKEY, process.getCorrelationKey());
        compareList.put(ProcessInstanceField.USER_IDENTITY, USER_YODA);
        compareList.put(ProcessInstanceField.PARENTPROCESSINSTANCEID, process.getParentId());
        compareList.put(ProcessInstanceField.STATUS, process.getState());
        compareList.put(ProcessInstanceField.PROCESSVERSION, process.getProcessVersion());
        compareList.put(ProcessInstanceField.PROCESSNAME, process.getProcessName());

        List<Long> resultsIds = new ArrayList<>();
        List<ProcessInstance> results = searchServicesClient.
                findProcessInstancesWithFilters(createQueryFilterAndEqualsTo(compareList), 0, 100);

        for (ProcessInstance res : results) {
            resultsIds.add(res.getId());
        }

        Assertions.assertThat(results).isNotNull();
        Assertions.assertThat(results).isNotEmpty();
        Assertions.assertThat(resultsIds).contains(process.getId());

        ProcessInstance instance = results.stream().filter(processInstance -> processInstance.getId().equals(process.getId()))
                .findFirst().orElse(null);
        Assertions.assertThat(instance).isNotNull();

        Assertions.assertThat(instance.getContainerId()).isEqualTo(CONTAINER_ID);
        Assertions.assertThat(instance.getId()).isEqualTo(processInstanceId);
        Assertions.assertThat(instance.getProcessName()).isEqualTo(process.getProcessName());
        Assertions.assertThat(instance.getCorrelationKey()).isEqualTo(process.getCorrelationKey());
        Assertions.assertThat(instance.getInitiator()).isEqualTo(USER_YODA);
        Assertions.assertThat(instance.getProcessInstanceDescription()).isEqualTo(process.getProcessInstanceDescription());
        Assertions.assertThat(instance.getParentId()).isEqualTo(process.getParentId());
        Assertions.assertThat(instance.getState()).isEqualTo(process.getState());
        Assertions.assertThat(instance.getProcessVersion()).isEqualTo(process.getProcessVersion());
        Assertions.assertThat(instance.getProcessId()).isEqualTo(process.getProcessId());
    }

    private void testFindProcessInstanceWithQueryFilter(ProcessInstanceQueryFilterSpec filter, Long processInstanceId) {
        List<Long> resultsIds = new ArrayList<>();
        List<ProcessInstance> results;

        results = searchServicesClient.
                findProcessInstancesWithFilters(filter, 0, 100);
        for (ProcessInstance res : results) {
            resultsIds.add(res.getId());
        }

        Assertions.assertThat(results).isNotNull();
        Assertions.assertThat(results).isNotEmpty();
        Assertions.assertThat(resultsIds).contains(processInstanceId);
    }

    private ProcessInstanceQueryFilterSpec createQueryFilterEqualsTo(ProcessInstanceField processInstanceField, Comparable<?> equalsTo) {
        return  new ProcessInstanceQueryFilterSpecBuilder().equalsTo(processInstanceField, equalsTo).get();
    }

    private ProcessInstanceQueryFilterSpec createQueryFilterAndEqualsTo(Map<ProcessInstanceField, Comparable<?>> filterProperties) {
        ProcessInstanceQueryFilterSpecBuilder result = new ProcessInstanceQueryFilterSpecBuilder();
        filterProperties.forEach(result::equalsTo);
        return  result.get();
    }
}
