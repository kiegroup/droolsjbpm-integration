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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.TaskField;
import org.kie.server.api.model.definition.TaskQueryFilterSpec;
import org.kie.server.api.model.instance.JobRequestInstance;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.api.util.TaskQueryFilterSpecBuilder;
import org.kie.server.client.JobServicesClient;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

public class TaskSearchServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final String GROUP_ID = "org.kie.server.testing";
    private static final String VERSION = "1.0.0.Final";
    private static final String CONTAINER_ID = "definition-project";
    private static final String PROCESS_ID_USERTASK = "definition-project.usertask";   
    private static final String FIRST_TASK_NAME = "First task";
    
    private static final String QUERY_NAME = "taskInstancesQuery";
    private static final String TASK_QUERY = "select ti.* from AuditTaskImpl ti";
    
    private static ReleaseId releaseId = new ReleaseId( GROUP_ID,
                                                        CONTAINER_ID,
                                                        VERSION );

    @BeforeClass
    public static void buildAndDeployArtifacts() throws Exception {
        JobServicesClient jsc = createDefaultStaticClient().getServicesClient(JobServicesClient.class);
        long id = jsc.scheduleRequest(JobRequestInstance.builder().command("org.jbpm.executor.commands.LogCleanupCommand").build());
        KieServerSynchronization.waitForJobToFinish(jsc, id, 120000L);
        
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject( ClassLoader.class.getResource( "/kjars-sources/definition-project" )
                                                                       .getFile() );

        kieContainer = KieServices.Factory.get().newKieContainer( releaseId );

        createContainer( CONTAINER_ID,
                         releaseId );
    }
    
    @Before
    public void registerQuery() {
        QueryDefinition query = new QueryDefinition();
        query.setName(QUERY_NAME);
        query.setSource(System.getProperty("org.kie.server.persistence.ds", "jdbc/jbpm-ds"));
        query.setExpression(TASK_QUERY);
        query.setTarget("CUSTOM");
        queryClient.registerQuery(query);
    }

    @After
    public void unregisterQuery() {
        queryClient.unregisterQuery(QUERY_NAME);
    }

    @Override
    protected void addExtraCustomClasses( Map<String, Class<?>> extraClasses ) throws Exception {
        extraClasses.put( PERSON_CLASS_NAME,
                          Class.forName( PERSON_CLASS_NAME,
                                         true,
                                         kieContainer.getClassLoader() ) );
    }

    @Test
    public void testFindTaskWithIncompatibleTypeFilter() throws Exception {
        assertClientException(
                               () -> queryClient.findHumanTasksWithFilters( QUERY_NAME, createQueryFilterGreaterThanOrEqualsTo( TaskField.CREATEDON,
                                                                                                                             "invalid data type" ),

                                                                                     0,
                                                                                     100 ),
                               400,
                               "The request could not be understood by the server due to malformed syntax: ",
                               "Can't lookup on specified data set: taskInstancesQuery");
    }

    @Test
    public void testFindTaskWithCreatedOnGreaterThanOrEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterGreaterThanOrEqualsTo( TaskField.CREATEDON,
                                                                                       subtractOneMinuteFromDate( task.getCreatedOn() ) ) ,
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithProcessIdEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterEqualsTo( TaskField.PROCESSID,
                                                                          PROCESS_ID_USERTASK ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithDescriptionEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterEqualsTo( TaskField.DESCRIPTION,
                                                                          "" ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithProcessInstanceIdEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterEqualsTo( TaskField.PROCESSINSTANCEID,
                                                                          processInstanceId ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithActualOwnerEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterEqualsTo( TaskField.ACTUALOWNER,
                                                                          USER_YODA ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithCreatedByEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterEqualsTo( TaskField.CREATEDBY,
                                                                          USER_YODA ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithNameEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterEqualsTo( TaskField.NAME,
                                                                          FIRST_TASK_NAME ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithTaskIdEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterEqualsTo( TaskField.TASKID,
                                                                          task.getId() ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithDeploymentIdEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterEqualsTo( TaskField.DEPLOYMENTID,
                                                                          CONTAINER_ID ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithStatusEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterEqualsTo( TaskField.STATUS,
                                                                          task.getStatus() ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithPriorityGreaterThanFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterGreaterThan( TaskField.PRIORITY,
                                                                             -1 ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithActivationTimeGreaterThanEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterGreaterThanOrEqualsTo( TaskField.ACTIVATIONTIME,
                                                                                       subtractOneMinuteFromDate( task.getActivationTime() ) ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithParentIdGreaterThanAndEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );
        testFindTaskInstanceWithSearchService( createQueryFilterGreaterThanAndEqualsTo( TaskField.PARENTID,
                                                                                        -2,
                                                                                        -1 ),
                                               task.getId() );
    }

    @Test
    public void testFindTaskWithAndEqualsToFilter() throws Exception {
        Long processInstanceId = processClient.startProcess( CONTAINER_ID,
                                                             PROCESS_ID_USERTASK );
        Assertions.assertThat( processInstanceId ).isNotNull();

        List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner( USER_YODA,
                                                                                0,
                                                                                10 );
        Assertions.assertThat( tasks ).isNotEmpty();
        TaskSummary task = tasks.get( 0 );

        HashMap<TaskField, Comparable<?>> compareList = new HashMap<>();
        compareList.put( TaskField.TASKID,
                         task.getId() );
        compareList.put( TaskField.DEPLOYMENTID,
                         CONTAINER_ID );
        compareList.put( TaskField.PROCESSINSTANCEID,
                         processInstanceId );
        compareList.put( TaskField.NAME,
                         FIRST_TASK_NAME );
        compareList.put( TaskField.CREATEDBY,
                         USER_YODA );
        compareList.put( TaskField.ACTUALOWNER,
                         USER_YODA );
        compareList.put( TaskField.DESCRIPTION,
                         "" );
        compareList.put( TaskField.DUEDATE,
                         task.getExpirationTime() );
        compareList.put( TaskField.PRIORITY,
                         task.getPriority() );
        compareList.put( TaskField.STATUS,
                         task.getStatus() );

        List<Long> resultsIds = new ArrayList<>();
        List<TaskInstance> results = queryClient.findHumanTasksWithFilters( QUERY_NAME, 
                                                                            createQueryFilterAndEqualsTo( compareList ),
                                                                                     0,
                                                                                     100 );

        for ( TaskInstance res : results ) {
            resultsIds.add( res.getId() );
        }

        Assertions.assertThat( results ).isNotNull();
        Assertions.assertThat( results ).isNotEmpty();
        Assertions.assertThat( resultsIds ).contains( task.getId() );

        TaskInstance instance = results.stream()
                                       .filter( taskInstance -> taskInstance.getId().equals( task.getId() ) )
                                       .findFirst()
                                       .orElse( null );
        Assertions.assertThat( instance ).isNotNull();

        Assertions.assertThat( instance.getContainerId() ).isEqualTo( CONTAINER_ID );
        Assertions.assertThat( instance.getProcessInstanceId() ).isEqualTo( processInstanceId );
        Assertions.assertThat( instance.getName() ).isEqualTo( FIRST_TASK_NAME );
        Assertions.assertThat( instance.getActualOwner() ).isEqualTo( USER_YODA );
        Assertions.assertThat( instance.getCreatedBy() ).isEqualTo( USER_YODA );
        Assertions.assertThat( instance.getDescription().trim() ).isEmpty();
        Assertions.assertThat( instance.getExpirationDate() ).isEqualTo( task.getExpirationTime() );
        Assertions.assertThat( instance.getPriority() ).isEqualTo( task.getPriority() );
        Assertions.assertThat( instance.getStatus() ).isEqualTo( task.getStatus() );
    }

    private void testFindTaskInstanceWithSearchService( TaskQueryFilterSpec filter,
                                                        Long taskInstanceId ) {
        List<Long> resultsIds = new ArrayList<>();
        List<TaskInstance> results = queryClient.findHumanTasksWithFilters( QUERY_NAME, 
                                                                            filter,
                                                                            0,
                                                                            100 );
        for ( TaskInstance res : results ) {
            resultsIds.add( res.getId() );
            
            Assertions.assertThat(res.getInputData()).isNullOrEmpty();
            Assertions.assertThat(res.getOutputData()).isNullOrEmpty();
        }

        Assertions.assertThat( results ).isNotNull();
        Assertions.assertThat( results ).isNotEmpty();
        Assertions.assertThat( resultsIds ).contains( taskInstanceId );
    }

    private TaskQueryFilterSpec createQueryFilterEqualsTo( TaskField taskField,
                                                           Comparable<?> equalsTo ) {
        return new TaskQueryFilterSpecBuilder().equalsTo( taskField,
                                                          equalsTo ).get();
    }

    private TaskQueryFilterSpec createQueryFilterGreaterThan( TaskField taskField,
                                                              Comparable<?> greaterThan ) {
        return new TaskQueryFilterSpecBuilder().greaterThan( taskField,
                                                             greaterThan ).get();
    }

    private TaskQueryFilterSpec createQueryFilterGreaterThanAndEqualsTo( TaskField taskField,
                                                                         Comparable<?> greaterThan,
                                                                         Comparable<?> equalsTo ) {
        return new TaskQueryFilterSpecBuilder().greaterThan( taskField,
                                                             greaterThan ).equalsTo( taskField,
                                                                                     equalsTo ).get();
    }

    private TaskQueryFilterSpec createQueryFilterGreaterThanOrEqualsTo( TaskField taskField,
                                                                        Comparable<?> equalsTo ) {
        return new TaskQueryFilterSpecBuilder().greaterOrEqualTo( taskField,
                                                                  equalsTo ).get();
    }

    private TaskQueryFilterSpec createQueryFilterAndEqualsTo( Map<TaskField, Comparable<?>> filterProperties ) {
        TaskQueryFilterSpecBuilder result = new TaskQueryFilterSpecBuilder();
        filterProperties.forEach( result::equalsTo );
        return result.get();
    }
}