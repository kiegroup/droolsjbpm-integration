/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.UIServicesClient;
import org.kie.server.client.UserTaskServicesClient;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;

@RunWith(Parameterized.class)
public class RenderFormServiceIntegrationTest extends KieServerBaseIntegrationTest {
    
    protected static final String USER_YODA = "yoda";
    protected static final String USER_JOHN = "john";
    protected static final String USER_ADMINISTRATOR = "Administrator";
    
    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();
    
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {restConfiguration}
                        }
        ));


        if (TestConfig.getRemotingUrl() != null && !TestConfig.skipJMS()) {
            KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
            parameterData.addAll(Arrays.asList(new Object[][]
                            {
                                    {jmsConfiguration}
                            })
            );
        }

        return parameterData;
    }
    @Parameterized.Parameter(0)
    public KieServicesConfiguration configuration;

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    
    private static ReleaseId caseReleaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "definition-project";
    private static final String HIRING_PROCESS_ID = "hiring";
    private static final String HIRING_2_PROCESS_ID = "hiring2";
    private static final String USERTASK_PROCESS_ID = "definition-project.usertask";
    private static final String PLACE_ORDER_PROCESS_ID = "form-rendering.place_order";
    
    private static final String CASE_CONTAINER_ID = "insurance";
    private static final String CLAIM_CASE_DEF_ID = "insurance-claims.CarInsuranceClaimCase";
    
    private static final String ORDER_CLASS_NAME = "com.myspace.form_rendering.Order";
    private static final String ITEM_CLASS_NAME = "com.myspace.form_rendering.Item";
    
    protected static KieContainer kieContainer;

    protected ProcessServicesClient processClient;
    protected UserTaskServicesClient taskClient;
    protected QueryServicesClient queryClient;
    protected UIServicesClient uiServicesClient;
    protected CaseServicesClient caseClient;

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/case-insurance").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
        
        createContainer(CONTAINER_ID, releaseId);
        createContainer(CASE_CONTAINER_ID, caseReleaseId);
    }
    
    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(ORDER_CLASS_NAME, Class.forName(ORDER_CLASS_NAME, true, kieContainer.getClassLoader()));
        extraClasses.put(ITEM_CLASS_NAME, Class.forName(ITEM_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
    }

    @After
    public void abortAllProcesses() {
        List<Integer> status = new ArrayList<Integer>();
        status.add(STATE_ACTIVE);
        List<ProcessInstance> activeInstances = queryClient.findProcessInstancesByStatus(status, 0, 100,
                CaseServicesClient.SORT_BY_PROCESS_INSTANCE_ID, false);
        if (activeInstances != null) {
            for (org.kie.server.api.model.instance.ProcessInstance instance : activeInstances) {
                processClient.abortProcessInstance(instance.getContainerId(), instance.getId());
            }
        }
    }

    @Override
    protected void setupClients(KieServicesClient client) {
        processClient = client.getServicesClient(ProcessServicesClient.class);
        taskClient = client.getServicesClient(UserTaskServicesClient.class);
        queryClient = client.getServicesClient(QueryServicesClient.class);
        uiServicesClient = client.getServicesClient(UIServicesClient.class);
        caseClient = client.getServicesClient(CaseServicesClient.class);
    }
    
    
    @Test
    public void testRenderProcessFormViaUIClientTest() throws Exception {
        String result = uiServicesClient.renderProcessForm(CONTAINER_ID, HIRING_PROCESS_ID);
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
        
        // it has the patternfly (default renderer) css
        assertThat(result).contains("/files/patternfly/css/patternfly.min.css\" rel=\"stylesheet\">");
        assertThat(result).contains("/files/patternfly/css/patternfly-additions.min.css\" rel=\"stylesheet\">");
        
        // it has required js files
        assertThat(result).contains("/files/patternfly/js/jquery.min.js\"></script>");
        assertThat(result).contains("/files/patternfly/js/patternfly.min.js\"></script>");
        assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                
        // it has the form header
        assertThat(result).contains("<h3 class=\"panel-title\">hiring-taskform.frm</h3>");
        
        // it has single input field
        assertThat(result).contains("<input name=\"name\" type=\"text\" class=\"form-control\" id=\"field_2225717094101704E12\" placeholder=\"\" value=\"\" pattern=\"\"  >");
        
        // it has start process button
        assertThat(result).contains("<button type=\"button\" class=\"btn btn-primary\" onclick=\"startProcess(this);\">Submit</button>");
    }
    
    @Test
    public void testRenderProcessFormViaUIClientBoostrapRendererTest() throws Exception {
        String result = uiServicesClient.renderProcessForm(CONTAINER_ID, HIRING_PROCESS_ID, UIServicesClient.BOOTSTRAP_FORM_RENDERER);
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
        
        // it has the bootstrap css
        assertThat(result).contains("files/bootstrap/css/bootstrap.min.css\" rel=\"stylesheet\">");
        
        // it has required js files
        assertThat(result).contains("/files/bootstrap/js/jquery.min.js\"></script>");
        assertThat(result).contains("/files/bootstrap/js/bootstrap.min.js\"></script>");
        assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                
        // it has the form header
        assertThat(result).contains("<h3 class=\"panel-title\">hiring-taskform.frm</h3>");
        
        // it has single input field
        assertThat(result).contains("<input name=\"name\" type=\"text\" class=\"form-control\" id=\"field_2225717094101704E12\" placeholder=\"\" value=\"\" pattern=\"\"  >");
        
        // it has start process button
        assertThat(result).contains("<button type=\"button\" class=\"btn btn-success\" onclick=\"startProcess(this);\">Submit</button>");
    }
    
    @Test
    public void testRenderProcessFormViaUIClientNoFrmFoundTest() throws Exception {
        
        assertClientException(
                () -> uiServicesClient.renderProcessForm(CONTAINER_ID, USERTASK_PROCESS_ID),
                404,
                "Form for process " + USERTASK_PROCESS_ID + " not found with supported suffix -taskform.frm",
                "Form for process " + USERTASK_PROCESS_ID + " not found with supported suffix -taskform.frm");
    }
  
    @Test
    public void testRenderTaskFormViaUIClientTest() throws Exception {
        changeUser(USER_JOHN);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "john");
        parameters.put("age", 33);
        parameters.put("mail", "john@doe.org");
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID, parameters);
        try {
            
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();
            parameters.put("out_age", 33);
            parameters.put("out_mail", "john@doe.org");
            taskClient.completeAutoProgress(CONTAINER_ID, taskId, USER_JOHN, parameters);

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            taskId = tasks.get(0).getId();
            taskClient.completeAutoProgress(CONTAINER_ID, taskId, USER_JOHN, parameters);

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            taskId = tasks.get(0).getId();

            String result = uiServicesClient.renderTaskForm(CONTAINER_ID, taskId);
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();
            
            // it has the patternfly (default renderer) css
            assertThat(result).contains("/files/patternfly/css/patternfly.min.css\" rel=\"stylesheet\">");
            assertThat(result).contains("/files/patternfly/css/patternfly-additions.min.css\" rel=\"stylesheet\">");
            
            // it has required js files
            assertThat(result).contains("/files/patternfly/js/jquery.min.js\"></script>");
            assertThat(result).contains("/files/patternfly/js/patternfly.min.js\"></script>");
            assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                    
            // it has the form header
            assertThat(result).contains("<h3 class=\"panel-title\">CreateProposal-taskform.frm</h3>");
            
            // it has three input fields
            assertThat(result).contains("<input name=\"offering\" type=\"text\" class=\"form-control\" id=\"field_3367047850452004E12\" placeholder=\"\" value=\"\" pattern=\"^\\d+$\"  >");
            assertThat(result).contains("<input name=\"tech_score\" type=\"text\" class=\"form-control\" id=\"field_4298972052332983E11\" placeholder=\"\" value=\"\" pattern=\"^\\d+$\"  >");
            assertThat(result).contains("<input name=\"hr_score\" type=\"text\" class=\"form-control\" id=\"field_800259544288992E11\" placeholder=\"\" value=\"\" pattern=\"^\\d+$\"  >");
            
            // it has life cycle buttons process button
            assertThat(result).contains("<button id=\"claimButton\" type=\"button\" class=\"btn btn-default\" onclick=\"claimTask();\">Claim</button>");
            assertThat(result).contains("<button id=\"releaseButton\" type=\"button\" class=\"btn btn-default\" onclick=\"releaseTask();\">Release</button>");
            assertThat(result).contains("<button id=\"startButton\" type=\"button\" class=\"btn btn-default\" onclick=\"startTask();\">Start</button>");
            assertThat(result).contains("<button id=\"stopButton\" type=\"button\" class=\"btn btn-default\" onclick=\"stopTask();\">Stop</button>");
            assertThat(result).contains("<button id=\"saveButton\" type=\"button\" class=\"btn btn-default\" onclick=\"saveTask();\">Save</button>");
            assertThat(result).contains("<button id=\"completeButton\" type=\"button\" class=\"btn btn-primary\" onclick=\"completeTask();\">Complete</button>");
        } finally {
            changeUser(USER_YODA);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
    
    @Test
    public void testRenderTaskFormViaUIClientTestBootstrap() throws Exception {
        changeUser(USER_JOHN);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "john");
        parameters.put("age", 33);
        parameters.put("mail", "john@doe.org");
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID, parameters);
        try {
            
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();
            parameters.put("out_age", 33);
            parameters.put("out_mail", "john@doe.org");
            taskClient.completeAutoProgress(CONTAINER_ID, taskId, USER_JOHN, parameters);

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            taskId = tasks.get(0).getId();
            taskClient.completeAutoProgress(CONTAINER_ID, taskId, USER_JOHN, parameters);

            tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            taskId = tasks.get(0).getId();

            String result = uiServicesClient.renderTaskForm(CONTAINER_ID, taskId, UIServicesClient.BOOTSTRAP_FORM_RENDERER);
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();
            
            // it has the bootstrap css
            assertThat(result).contains("/files/bootstrap/css/bootstrap.min.css\" rel=\"stylesheet\">");
            
            // it has required js files
            assertThat(result).contains("/files/bootstrap/js/jquery.min.js\"></script>");
            assertThat(result).contains("/files/bootstrap/js/bootstrap.min.js\"></script>");
            assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                    
            // it has the form header
            assertThat(result).contains("<h3 class=\"panel-title\">CreateProposal-taskform.frm</h3>");
            
            // it has three input fields
            assertThat(result).contains("<input name=\"offering\" type=\"text\" class=\"form-control\" id=\"field_3367047850452004E12\" placeholder=\"\" value=\"\" pattern=\"^\\d+$\"  >");
            assertThat(result).contains("<input name=\"tech_score\" type=\"text\" class=\"form-control\" id=\"field_4298972052332983E11\" placeholder=\"\" value=\"\" pattern=\"^\\d+$\"  >");
            assertThat(result).contains("<input name=\"hr_score\" type=\"text\" class=\"form-control\" id=\"field_800259544288992E11\" placeholder=\"\" value=\"\" pattern=\"^\\d+$\"  >");
            
            // it has life cycle buttons process button
            assertThat(result).contains("<button id=\"claimButton\" type=\"button\" class=\"btn btn-default\" onclick=\"claimTask();\">Claim</button>");
            assertThat(result).contains("<button id=\"releaseButton\" type=\"button\" class=\"btn btn-default\" onclick=\"releaseTask();\">Release</button>");
            assertThat(result).contains("<button id=\"startButton\" type=\"button\" class=\"btn btn-default\" onclick=\"startTask();\">Start</button>");
            assertThat(result).contains("<button id=\"stopButton\" type=\"button\" class=\"btn btn-default\" onclick=\"stopTask();\">Stop</button>");
            assertThat(result).contains("<button id=\"saveButton\" type=\"button\" class=\"btn btn-default\" onclick=\"saveTask();\">Save</button>");
            assertThat(result).contains("<button id=\"completeButton\" type=\"button\" class=\"btn btn-success\" onclick=\"completeTask();\">Complete</button>");
        } finally {
            changeUser(USER_YODA);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
    
    @Test
    public void testRenderTaskFormViaUIClientNoFrmFoundTest() throws Exception {
        changeUser(USER_JOHN);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "john");
        parameters.put("age", 33);
        parameters.put("mail", "john@doe.org");
        long processInstanceId = processClient.startProcess(CONTAINER_ID, HIRING_2_PROCESS_ID, parameters);
        try {
            
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();          
            assertClientException(
                () -> uiServicesClient.renderTaskForm(CONTAINER_ID, taskId),
                404,
                "Form for task " + taskId + " not found with supported suffix -taskform.frm",
                "Form for task " + taskId + " not found with supported suffix -taskform.frm");
        
        } finally {
            changeUser(USER_YODA);
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
    
    @Test
    public void testRenderProcessFormViaUIClientNotExistingRendererTest() throws Exception {
        
        assertClientException(
                () -> uiServicesClient.renderProcessForm(CONTAINER_ID, HIRING_PROCESS_ID, "NOT_EXISTING"),
                404,
                "Form renderer with name NOT_EXISTING not found",
                "Form renderer with name NOT_EXISTING not found");
    }
    
    @Test
    public void testRenderCaseFormViaUIClientTest() throws Exception {
        String result = uiServicesClient.renderCaseForm(CASE_CONTAINER_ID, CLAIM_CASE_DEF_ID);
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
        
        // it has the patternfly (default renderer) css
        assertThat(result).contains("/files/patternfly/css/patternfly.min.css\" rel=\"stylesheet\">");
        assertThat(result).contains("/files/patternfly/css/patternfly-additions.min.css\" rel=\"stylesheet\">");
        
        // it has required js files
        assertThat(result).contains("/files/patternfly/js/jquery.min.js\"></script>");
        assertThat(result).contains("/files/patternfly/js/patternfly.min.js\"></script>");
        assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                
        // it has the form header
        assertThat(result).contains("<h3 class=\"panel-title\">Case form</h3>");
        
        // it has role assignments input fields
        assertThat(result).contains("<input name=\"user_insured\" type=\"text\" class=\"form-control\" id=\"user_insured\" placeholder=\"Enter role assignment for insured (user assignment)\" >");
        assertThat(result).contains("<input name=\"group_insured\" type=\"text\" class=\"form-control\" id=\"group_insured\" placeholder=\"Enter role assignment for insured group assignment)\" >");
        assertThat(result).contains("<input name=\"user_insuranceRepresentative\" type=\"text\" class=\"form-control\" id=\"user_insuranceRepresentative\" placeholder=\"Enter role assignment for insuranceRepresentative (user assignment)\" >");
        assertThat(result).contains("<input name=\"group_insuranceRepresentative\" type=\"text\" class=\"form-control\" id=\"group_insuranceRepresentative\" placeholder=\"Enter role assignment for insuranceRepresentative group assignment)\" >");
        assertThat(result).contains("<input name=\"user_assessor\" type=\"text\" class=\"form-control\" id=\"user_assessor\" placeholder=\"Enter role assignment for assessor (user assignment)\" >");
        assertThat(result).contains("<input name=\"group_assessor\" type=\"text\" class=\"form-control\" id=\"group_assessor\" placeholder=\"Enter role assignment for assessor group assignment)\" >");
        
        // it has start process button
        assertThat(result).contains("<button type=\"button\" class=\"btn btn-primary\" onclick=\"startCase(this);\">Submit</button>");
    }
    
    @Test
    public void testRenderCaseFormViaUIClientTestBootstrap() throws Exception {
        String result = uiServicesClient.renderCaseForm(CASE_CONTAINER_ID, CLAIM_CASE_DEF_ID, UIServicesClient.BOOTSTRAP_FORM_RENDERER);
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
        
        // it has the bootstrap css
        assertThat(result).contains("/files/bootstrap/css/bootstrap.min.css\" rel=\"stylesheet\">");
        
        // it has required js files
        assertThat(result).contains("/files/bootstrap/js/jquery.min.js\"></script>");
        assertThat(result).contains("/files/bootstrap/js/bootstrap.min.js\"></script>");
        assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                
        // it has the form header
        assertThat(result).contains("<h3 class=\"panel-title\">Case form</h3>");
        
        // it has role assignments input fields
        assertThat(result).contains("<input name=\"user_insured\" type=\"text\" class=\"form-control\" id=\"user_insured\" placeholder=\"Enter role assignment for insured (user assignment)\" >");
        assertThat(result).contains("<input name=\"group_insured\" type=\"text\" class=\"form-control\" id=\"group_insured\" placeholder=\"Enter role assignment for insured group assignment)\" >");
        assertThat(result).contains("<input name=\"user_insuranceRepresentative\" type=\"text\" class=\"form-control\" id=\"user_insuranceRepresentative\" placeholder=\"Enter role assignment for insuranceRepresentative (user assignment)\" >");
        assertThat(result).contains("<input name=\"group_insuranceRepresentative\" type=\"text\" class=\"form-control\" id=\"group_insuranceRepresentative\" placeholder=\"Enter role assignment for insuranceRepresentative group assignment)\" >");
        assertThat(result).contains("<input name=\"user_assessor\" type=\"text\" class=\"form-control\" id=\"user_assessor\" placeholder=\"Enter role assignment for assessor (user assignment)\" >");
        assertThat(result).contains("<input name=\"group_assessor\" type=\"text\" class=\"form-control\" id=\"group_assessor\" placeholder=\"Enter role assignment for assessor group assignment)\" >");
        
        // it has start process button
        assertThat(result).contains("<button type=\"button\" class=\"btn btn-success\" onclick=\"startCase(this);\">Submit</button>");
    }
    
    @Test
    public void testRenderProcessFormViaUIClientTestMultiSubForm() throws Exception {
        String result = uiServicesClient.renderProcessForm(CONTAINER_ID, PLACE_ORDER_PROCESS_ID);
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
        
        // it has the patternfly (default renderer) css
        assertThat(result).contains("/files/patternfly/css/patternfly.min.css\" rel=\"stylesheet\">");
        assertThat(result).contains("/files/patternfly/css/patternfly-additions.min.css\" rel=\"stylesheet\">");
        
        // it has required js files
        assertThat(result).contains("/files/patternfly/js/jquery.min.js\"></script>");
        assertThat(result).contains("/files/patternfly/js/patternfly.min.js\"></script>");
        assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                
        // it has the form header
        assertThat(result).contains("<h3 class=\"panel-title\">form-rendering.place_order-taskform.frm</h3>");
        
        // it has three input fields
        assertThat(result).contains("<input name=\"orderNumber\" type=\"text\" class=\"form-control\" id=\"field_733428728888174E11\" placeholder=\"OrderNumber\" value=\"\" pattern=\"\"  required>");
        assertThat(result).contains("<input name=\"customer\" type=\"text\" class=\"form-control\" id=\"field_094300706550535E11\" placeholder=\"Customer\" value=\"\" pattern=\"\"  required>");
        assertThat(result).contains("<input name=\"address\" type=\"text\" class=\"form-control\" id=\"field_428085212636635E12\" placeholder=\"Address\" value=\"\" pattern=\"\"  required>");
        
        // it has a hidden form for multisubform items
        assertThat(result).contains("<div class=\"row hidden\" id=\"form_field_180962688550559E11\">");
        // with three input fields
        assertThat(result).contains("<input name=\"name\" type=\"text\" class=\"form-control\" id=\"field_822453895302379E11\" placeholder=\"Name\" value=\"\" pattern=\"\"  required>");
        assertThat(result).contains("<input name=\"quantity\" type=\"text\" class=\"form-control\" id=\"field_99531536931457E11\" placeholder=\"Quantity\" value=\"\" pattern=\"^\\d+$\"  required>");
        assertThat(result).contains("<input name=\"price\" type=\"text\" class=\"form-control\" id=\"field_774182907094941E11\" placeholder=\"Price\" value=\"\" pattern=\"^\\d+(\\.\\d+)?$\"  required>");
        
        // it has a table
        assertThat(result).contains("<table class=\"table table-bordered\" id=\"table_field_180962688550559E11\" data-type=\"com.myspace.form_rendering.Item\">");
        
        // it has three columns and action column
        assertThat(result).contains("<th>Name</th>");
        assertThat(result).contains("<th>Quantity</th>");
        assertThat(result).contains("<th>Price</th>");
        assertThat(result).contains("<th width=\"10%\" class=\"text-center\" colspan=\"2\"><button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-primary\" onclick=\"openForm('field_180962688550559E11');\">Add</button></th>");
        
        // it has hidden row to be cloned
        assertThat(result).contains("<tr id=\"hiddenRow\" class=\"hidden\">");
        // with three column placeholders and two action columns 
        assertThat(result).contains("<td data-name=\"name\" data-type=\"String(\">placeholder</td>");
        assertThat(result).contains("<td data-name=\"quantity\" data-type=\"Number(\">placeholder</td>");
        assertThat(result).contains("<td data-name=\"price\" data-type=\"Number(\">placeholder</td>");
        assertThat(result).contains("<button data-row=\"\" data-table=\"field_180962688550559E11\" style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-default\" onclick=\"editItem($(this).data('table'), $(this).data('row'));\">Edit</button>");
        assertThat(result).contains("<button data-row=\"\" data-table=\"table_field_180962688550559E11\" style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-danger\" onclick=\"deleteItem($(this).data('table'), $(this).data('row'));\">Delete</button>");
        
        // it has start process button
        assertThat(result).contains("<button type=\"button\" class=\"btn btn-primary\" onclick=\"startProcess(this);\">Submit</button>");
    }
    
    @Test
    public void testRenderProcessFormViaUIClientTestMultiSubFormBootStrap() throws Exception {
        String result = uiServicesClient.renderProcessForm(CONTAINER_ID, PLACE_ORDER_PROCESS_ID, UIServicesClient.BOOTSTRAP_FORM_RENDERER);
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
        
        // it has the bootstrap css
        assertThat(result).contains("/files/bootstrap/css/bootstrap.min.css\" rel=\"stylesheet\">");
        
        // it has required js files
        assertThat(result).contains("/files/bootstrap/js/jquery.min.js\"></script>");
        assertThat(result).contains("/files/bootstrap/js/bootstrap.min.js\"></script>");
        assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                
        // it has the form header
        assertThat(result).contains("<h3 class=\"panel-title\">form-rendering.place_order-taskform.frm</h3>");
        
        // it has three input fields
        assertThat(result).contains("<input name=\"orderNumber\" type=\"text\" class=\"form-control\" id=\"field_733428728888174E11\" placeholder=\"OrderNumber\" value=\"\" pattern=\"\"  required>");
        assertThat(result).contains("<input name=\"customer\" type=\"text\" class=\"form-control\" id=\"field_094300706550535E11\" placeholder=\"Customer\" value=\"\" pattern=\"\"  required>");
        assertThat(result).contains("<input name=\"address\" type=\"text\" class=\"form-control\" id=\"field_428085212636635E12\" placeholder=\"Address\" value=\"\" pattern=\"\"  required>");
        
        // it has a hidden form for multisubform items
        assertThat(result).contains("<div class=\"row hidden\" id=\"form_field_180962688550559E11\">");
        // with three input fields
        assertThat(result).contains("<input name=\"name\" type=\"text\" class=\"form-control\" id=\"field_822453895302379E11\" placeholder=\"Name\" value=\"\" pattern=\"\"  required>");
        assertThat(result).contains("<input name=\"quantity\" type=\"text\" class=\"form-control\" id=\"field_99531536931457E11\" placeholder=\"Quantity\" value=\"\" pattern=\"^\\d+$\"  required>");
        assertThat(result).contains("<input name=\"price\" type=\"text\" class=\"form-control\" id=\"field_774182907094941E11\" placeholder=\"Price\" value=\"\" pattern=\"^\\d+(\\.\\d+)?$\"  required>");
        
        // it has a table
        assertThat(result).contains("<table class=\"table table-bordered\" id=\"table_field_180962688550559E11\" data-type=\"com.myspace.form_rendering.Item\">");
        
        // it has three columns and action column
        assertThat(result).contains("<th>Name</th>");
        assertThat(result).contains("<th>Quantity</th>");
        assertThat(result).contains("<th>Price</th>");
        assertThat(result).contains("<th width=\"10%\" class=\"text-center\" colspan=\"2\"><button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-primary\" onclick=\"openForm('field_180962688550559E11');\">Add</button></th>");
        
        // it has hidden row to be cloned
        assertThat(result).contains("<tr id=\"hiddenRow\" class=\"hidden\">");
        // with three column placeholders and two action columns 
        assertThat(result).contains("<td data-name=\"name\" data-type=\"String(\">placeholder</td>");
        assertThat(result).contains("<td data-name=\"quantity\" data-type=\"Number(\">placeholder</td>");
        assertThat(result).contains("<td data-name=\"price\" data-type=\"Number(\">placeholder</td>");
        assertThat(result).contains("<button data-row=\"\" data-table=\"field_180962688550559E11\" style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-default\" onclick=\"editItem($(this).data('table'), $(this).data('row'));\">Edit</button>");
        assertThat(result).contains("<button data-row=\"\" data-table=\"table_field_180962688550559E11\" style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-danger\" onclick=\"deleteItem($(this).data('table'), $(this).data('row'));\">Delete</button>");
        
        // it has start process button
        assertThat(result).contains("<button type=\"button\" class=\"btn btn-success\" onclick=\"startProcess(this);\">Submit</button>");
    }
    
    @Test
    public void testRenderTaskFormViaUIClientTestMultiSubForm() throws Exception {
        
        ClassLoader loader = kieContainer.getClassLoader();
        
        Object firstItem = KieServerReflections.createInstance(ITEM_CLASS_NAME, loader, "first", 100, 25.5);
        Object secondItem = KieServerReflections.createInstance(ITEM_CLASS_NAME, loader, "second", 200, 8.5);
        
        List<Object> items = new ArrayList<>();
        items.add(firstItem);
        items.add(secondItem);
        
        Object order = KieServerReflections.createInstance(ORDER_CLASS_NAME, loader, "XXX", "JOHN", "MainStreet", items);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("order", order);
        
        long processInstanceId = processClient.startProcess(CONTAINER_ID, PLACE_ORDER_PROCESS_ID, parameters);
        try {
            
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();            

            String result = uiServicesClient.renderTaskForm(CONTAINER_ID, taskId);
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();
            
            // it has the patternfly (default renderer) css
            assertThat(result).contains("/files/patternfly/css/patternfly.min.css\" rel=\"stylesheet\">");
            assertThat(result).contains("/files/patternfly/css/patternfly-additions.min.css\" rel=\"stylesheet\">");
            
            // it has required js files
            assertThat(result).contains("/files/patternfly/js/jquery.min.js\"></script>");
            assertThat(result).contains("/files/patternfly/js/patternfly.min.js\"></script>");
            assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                    
            // it has the form header
            assertThat(result).contains("<h3 class=\"panel-title\">AddItems-taskform.frm</h3>");
            
            // it has three input fields with data from starting process
            assertThat(result).contains("<input name=\"orderNumber\" type=\"text\" class=\"form-control\" id=\"field_733428728888174E11\" placeholder=\"OrderNumber\" value=\"XXX\" pattern=\"\"  required>");
            assertThat(result).contains("<input name=\"customer\" type=\"text\" class=\"form-control\" id=\"field_094300706550535E11\" placeholder=\"Customer\" value=\"JOHN\" pattern=\"\"  required>");
            assertThat(result).contains("<input name=\"address\" type=\"text\" class=\"form-control\" id=\"field_428085212636635E12\" placeholder=\"Address\" value=\"MainStreet\" pattern=\"\"  required>");
            
            // it has a hidden form for multisubform items
            assertThat(result).contains("<div class=\"row hidden\" id=\"form_field_180962688550559E11\">");
            // with three input fields
            assertThat(result).contains("<input name=\"name\" type=\"text\" class=\"form-control\" id=\"field_822453895302379E11\" placeholder=\"Name\" value=\"\" pattern=\"\"  required>");
            assertThat(result).contains("<input name=\"quantity\" type=\"text\" class=\"form-control\" id=\"field_99531536931457E11\" placeholder=\"Quantity\" value=\"\" pattern=\"^\\d+$\"  required>");
            assertThat(result).contains("<input name=\"price\" type=\"text\" class=\"form-control\" id=\"field_774182907094941E11\" placeholder=\"Price\" value=\"\" pattern=\"^\\d+(\\.\\d+)?$\"  required>");
            
            // it has a table
            assertThat(result).contains("<table class=\"table table-bordered\" id=\"table_field_180962688550559E11\" data-type=\"com.myspace.form_rendering.Item\">");
            
            // it has three columns and action column
            assertThat(result).contains("<th>Name</th>");
            assertThat(result).contains("<th>Quantity</th>");
            assertThat(result).contains("<th>Price</th>");
            assertThat(result).contains("<th width=\"10%\" class=\"text-center\" colspan=\"2\"><button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-primary\" onclick=\"openForm('field_180962688550559E11');\">Add</button></th>");
            
            // it has hidden row to be cloned
            assertThat(result).contains("<tr id=\"hiddenRow\" class=\"hidden\">");
            // with three column placeholders and two action columns 
            assertThat(result).contains("<td data-name=\"name\" data-type=\"String(\">placeholder</td>");
            assertThat(result).contains("<td data-name=\"quantity\" data-type=\"Number(\">placeholder</td>");
            assertThat(result).contains("<td data-name=\"price\" data-type=\"Number(\">placeholder</td>");
            assertThat(result).contains("<button data-row=\"\" data-table=\"field_180962688550559E11\" style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-default\" onclick=\"editItem($(this).data('table'), $(this).data('row'));\">Edit</button>");
            assertThat(result).contains("<button data-row=\"\" data-table=\"table_field_180962688550559E11\" style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-danger\" onclick=\"deleteItem($(this).data('table'), $(this).data('row'));\">Delete</button>");
            
            // table has two rwos with data from the items given at start
            assertThat(result).contains("<tr id=\"table_field_180962688550559E11_0\">");
            assertThat(result).contains("<td data-name=\"name\" data-type=\"String(\">first</td>");
            assertThat(result).contains("<td data-name=\"quantity\" data-type=\"Number(\">100</td>");
            assertThat(result).contains("<td data-name=\"price\" data-type=\"Number(\">25.5</td>");
            assertThat(result).contains("<button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-default\" onclick=\"editItem('field_180962688550559E11', 'table_field_180962688550559E11_0');\">Edit</button>");
            assertThat(result).contains("<button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-danger\" onclick=\"deleteItem('table_field_180962688550559E11', 'table_field_180962688550559E11_0');\">Delete</button>");
            
            assertThat(result).contains("<tr id=\"table_field_180962688550559E11_1\">");
            assertThat(result).contains("<td data-name=\"name\" data-type=\"String(\">second</td>");
            assertThat(result).contains("<td data-name=\"quantity\" data-type=\"Number(\">200</td>");
            assertThat(result).contains("<td data-name=\"price\" data-type=\"Number(\">8.5</td>");
            assertThat(result).contains("<button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-default\" onclick=\"editItem('field_180962688550559E11', 'table_field_180962688550559E11_1');\">Edit</button>");
            assertThat(result).contains("<button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-danger\" onclick=\"deleteItem('table_field_180962688550559E11', 'table_field_180962688550559E11_1');\">Delete</button>");
            
            // it has life cycle buttons process button
            assertThat(result).contains("<button id=\"claimButton\" type=\"button\" class=\"btn btn-default\" onclick=\"claimTask();\">Claim</button>");
            assertThat(result).contains("<button id=\"releaseButton\" type=\"button\" class=\"btn btn-default\" onclick=\"releaseTask();\">Release</button>");
            assertThat(result).contains("<button id=\"startButton\" type=\"button\" class=\"btn btn-default\" onclick=\"startTask();\">Start</button>");
            assertThat(result).contains("<button id=\"stopButton\" type=\"button\" class=\"btn btn-default\" onclick=\"stopTask();\">Stop</button>");
            assertThat(result).contains("<button id=\"saveButton\" type=\"button\" class=\"btn btn-default\" onclick=\"saveTask();\">Save</button>");
            assertThat(result).contains("<button id=\"completeButton\" type=\"button\" class=\"btn btn-primary\" onclick=\"completeTask();\">Complete</button>");
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
    
    @Test
    public void testRenderTaskFormViaUIClientTestMultiSubFormBootstrap() throws Exception {
        
        ClassLoader loader = kieContainer.getClassLoader();
        
        Object firstItem = KieServerReflections.createInstance(ITEM_CLASS_NAME, loader, "first", 100, 25.5);
        Object secondItem = KieServerReflections.createInstance(ITEM_CLASS_NAME, loader, "second", 200, 8.5);
        
        List<Object> items = new ArrayList<>();
        items.add(firstItem);
        items.add(secondItem);
        
        Object order = KieServerReflections.createInstance(ORDER_CLASS_NAME, loader, "XXX", "JOHN", "MainStreet", items);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("order", order);
        
        long processInstanceId = processClient.startProcess(CONTAINER_ID, PLACE_ORDER_PROCESS_ID, parameters);
        try {
            
            List<TaskSummary> tasks = taskClient.findTasksByStatusByProcessInstanceId(processInstanceId, null, 0, 10);
            assertThat(tasks).isNotNull().hasSize(1);

            Long taskId = tasks.get(0).getId();            

            String result = uiServicesClient.renderTaskForm(CONTAINER_ID, taskId, UIServicesClient.BOOTSTRAP_FORM_RENDERER);
            logger.debug("Form content is '{}'", result);
            assertThat(result).isNotNull().isNotEmpty();
            
            // it has the bootstrap css
            assertThat(result).contains("/files/bootstrap/css/bootstrap.min.css\" rel=\"stylesheet\">");
            
            // it has required js files
            assertThat(result).contains("/files/bootstrap/js/jquery.min.js\"></script>");
            assertThat(result).contains("/files/bootstrap/js/bootstrap.min.js\"></script>");
            assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                    
            // it has the form header
            assertThat(result).contains("<h3 class=\"panel-title\">AddItems-taskform.frm</h3>");
            
            // it has three input fields with data from starting process
            assertThat(result).contains("<input name=\"orderNumber\" type=\"text\" class=\"form-control\" id=\"field_733428728888174E11\" placeholder=\"OrderNumber\" value=\"XXX\" pattern=\"\"  required>");
            assertThat(result).contains("<input name=\"customer\" type=\"text\" class=\"form-control\" id=\"field_094300706550535E11\" placeholder=\"Customer\" value=\"JOHN\" pattern=\"\"  required>");
            assertThat(result).contains("<input name=\"address\" type=\"text\" class=\"form-control\" id=\"field_428085212636635E12\" placeholder=\"Address\" value=\"MainStreet\" pattern=\"\"  required>");
            
            // it has a hidden form for multisubform items
            assertThat(result).contains("<div class=\"row hidden\" id=\"form_field_180962688550559E11\">");
            // with three input fields
            assertThat(result).contains("<input name=\"name\" type=\"text\" class=\"form-control\" id=\"field_822453895302379E11\" placeholder=\"Name\" value=\"\" pattern=\"\"  required>");
            assertThat(result).contains("<input name=\"quantity\" type=\"text\" class=\"form-control\" id=\"field_99531536931457E11\" placeholder=\"Quantity\" value=\"\" pattern=\"^\\d+$\"  required>");
            assertThat(result).contains("<input name=\"price\" type=\"text\" class=\"form-control\" id=\"field_774182907094941E11\" placeholder=\"Price\" value=\"\" pattern=\"^\\d+(\\.\\d+)?$\"  required>");
            
            // it has a table
            assertThat(result).contains("<table class=\"table table-bordered\" id=\"table_field_180962688550559E11\" data-type=\"com.myspace.form_rendering.Item\">");
            
            // it has three columns and action column
            assertThat(result).contains("<th>Name</th>");
            assertThat(result).contains("<th>Quantity</th>");
            assertThat(result).contains("<th>Price</th>");
            assertThat(result).contains("<th width=\"10%\" class=\"text-center\" colspan=\"2\"><button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-primary\" onclick=\"openForm('field_180962688550559E11');\">Add</button></th>");
            
            // it has hidden row to be cloned
            assertThat(result).contains("<tr id=\"hiddenRow\" class=\"hidden\">");
            // with three column placeholders and two action columns 
            assertThat(result).contains("<td data-name=\"name\" data-type=\"String(\">placeholder</td>");
            assertThat(result).contains("<td data-name=\"quantity\" data-type=\"Number(\">placeholder</td>");
            assertThat(result).contains("<td data-name=\"price\" data-type=\"Number(\">placeholder</td>");
            assertThat(result).contains("<button data-row=\"\" data-table=\"field_180962688550559E11\" style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-default\" onclick=\"editItem($(this).data('table'), $(this).data('row'));\">Edit</button>");
            assertThat(result).contains("<button data-row=\"\" data-table=\"table_field_180962688550559E11\" style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-danger\" onclick=\"deleteItem($(this).data('table'), $(this).data('row'));\">Delete</button>");
            
            // table has two rwos with data from the items given at start
            assertThat(result).contains("<tr id=\"table_field_180962688550559E11_0\">");
            assertThat(result).contains("<td data-name=\"name\" data-type=\"String(\">first</td>");
            assertThat(result).contains("<td data-name=\"quantity\" data-type=\"Number(\">100</td>");
            assertThat(result).contains("<td data-name=\"price\" data-type=\"Number(\">25.5</td>");
            assertThat(result).contains("<button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-default\" onclick=\"editItem('field_180962688550559E11', 'table_field_180962688550559E11_0');\">Edit</button>");
            assertThat(result).contains("<button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-danger\" onclick=\"deleteItem('table_field_180962688550559E11', 'table_field_180962688550559E11_0');\">Delete</button>");
            
            assertThat(result).contains("<tr id=\"table_field_180962688550559E11_1\">");
            assertThat(result).contains("<td data-name=\"name\" data-type=\"String(\">second</td>");
            assertThat(result).contains("<td data-name=\"quantity\" data-type=\"Number(\">200</td>");
            assertThat(result).contains("<td data-name=\"price\" data-type=\"Number(\">8.5</td>");
            assertThat(result).contains("<button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-default\" onclick=\"editItem('field_180962688550559E11', 'table_field_180962688550559E11_1');\">Edit</button>");
            assertThat(result).contains("<button style=\"width: 80px !important;\" type=\"button\" class=\"btn btn-danger\" onclick=\"deleteItem('table_field_180962688550559E11', 'table_field_180962688550559E11_1');\">Delete</button>");
            
            // it has life cycle buttons process button
            assertThat(result).contains("<button id=\"claimButton\" type=\"button\" class=\"btn btn-default\" onclick=\"claimTask();\">Claim</button>");
            assertThat(result).contains("<button id=\"releaseButton\" type=\"button\" class=\"btn btn-default\" onclick=\"releaseTask();\">Release</button>");
            assertThat(result).contains("<button id=\"startButton\" type=\"button\" class=\"btn btn-default\" onclick=\"startTask();\">Start</button>");
            assertThat(result).contains("<button id=\"stopButton\" type=\"button\" class=\"btn btn-default\" onclick=\"stopTask();\">Stop</button>");
            assertThat(result).contains("<button id=\"saveButton\" type=\"button\" class=\"btn btn-default\" onclick=\"saveTask();\">Save</button>");
            assertThat(result).contains("<button id=\"completeButton\" type=\"button\" class=\"btn btn-success\" onclick=\"completeTask();\">Complete</button>");
        } finally {
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
        }
    }
    
    /*
     * Helper methods
     */

    @Override
    protected KieServicesClient createDefaultClient() throws Exception {
        addExtraCustomClasses(extraClasses);
        KieServicesConfiguration restConfiguration = configuration;
        if (TestConfig.isLocalServer()) {
            restConfiguration = KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), null, null);
        }
        return createDefaultClient(restConfiguration, MarshallingFormat.JAXB);
    }   
    
    protected static KieServicesConfiguration createKieServicesRestConfiguration() {
        return KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());
    }
    
    protected static KieServicesConfiguration createKieServicesJmsConfiguration() {
        try {
            InitialContext context = TestConfig.getInitialRemoteContext();

            Queue requestQueue = (Queue) context.lookup(TestConfig.getRequestQueueJndi());
            Queue responseQueue = (Queue) context.lookup(TestConfig.getResponseQueueJndi());
            ConnectionFactory connectionFactory = (ConnectionFactory) context.lookup(TestConfig.getConnectionFactory());

            KieServicesConfiguration jmsConfiguration = KieServicesFactory.newJMSConfiguration(
                    connectionFactory, requestQueue, responseQueue, TestConfig.getUsername(),
                    TestConfig.getPassword());

            return jmsConfiguration;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create JMS client configuration!", e);
        }
    }

    protected void changeUser(String username) throws Exception {
        if(username == null) {
            username = TestConfig.getUsername();
        }
        configuration.setUserName(username);
        client = createDefaultClient();
    }
    
    protected void assertClientException(ThrowableAssert.ThrowingCallable callable, int expectedHttpCode, String restMessage, String jmsMessage) {
        if(configuration.isRest()) {
            Assertions.assertThatThrownBy(callable)
                    .isInstanceOf(KieServicesHttpException.class)
                    .hasFieldOrPropertyWithValue("httpCode", expectedHttpCode)
                    .hasMessageContaining(restMessage);
        } else {
            Assertions.assertThatThrownBy(callable)
                    .isInstanceOf(KieServicesException.class)
                    .hasMessageContaining(jmsMessage);
        }
    }
}
