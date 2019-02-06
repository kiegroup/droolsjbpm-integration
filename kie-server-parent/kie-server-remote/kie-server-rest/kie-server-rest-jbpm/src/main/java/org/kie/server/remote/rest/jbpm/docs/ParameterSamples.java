/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.remote.rest.jbpm.docs;


public class ParameterSamples {
    
    public static final String JSON = "application/json";
    public static final String XML = "application/xml";
    
    /*
     * 
     * JSON sample payloads
     * 
     */

    public static final String SIMPLE_VAR_MAP_JSON = "{\n" + 
            "    \"age\": 25,\n" + 
            "    \"name\": \"john\"\n" +                  
            "}";
    
    public static final String VAR_MAP_JSON = "{\n" + 
            "    \"age\": 25,\n" + 
            "    \"person\": {\n" + 
            "        \"Person\": {\n" + 
            "            \"name\": \"john\"\n" + 
            "        }\n" + 
            "    }\n" + 
            "}";
    
    public static final String VAR_JSON = "{\n" + 
            "    \"Person\": {\n" + 
            "        \"name\": \"john\"\n" + 
            "    }\n" +             
            "}";
    
    public static final String DOCUMENT_JSON = "{\n" + 
            "  \"document-name\" : \"first document\",\n" + 
            "  \"document-link\" : null,\n" + 
            "  \"document-size\" : 17,\n" + 
            "  \"document-last-mod\" : {\n" + 
            "    \"java.util.Date\" : 1539936629148\n" + 
            "  },\n" + 
            "  \"document-content\" : \"anVzdCB0ZXh0IGNvbnRlbnQ=\"\n" + 
            "}";
    
    public static final String JOB_JSON = "{\n" + 
            "  \"job-command\" : \"org.jbpm.executor.commands.PrintOutCommand\",\n" + 
            "  \"scheduled-date\" : {\n" + 
            "    \"java.util.Date\" : 1540023411655\n" + 
            "  },\n" + 
            "  \"request-data\" : {\n" + 
            "    \"businessKey\" : \"test key\"\n" + 
            "  }\n" + 
            "}";
    
    public static final String QUERY_DEF_JSON = "{\n" + 
            "  \"query-name\" : \"allProcessInstances\",\n" + 
            "  \"query-source\" : \"jdbc/jbpm-ds\",\n" + 
            "  \"query-expression\" : \"select * from ProcessInstanceLog where status = 1\",\n" + 
            "  \"query-target\" : \"PROCESS\",\n" + 
            "  \"query-columns\" : null\n" + 
            "}";
    
    public static final String QUERY_FILTER_SPEC_JSON = "{\n" + 
            "  \"order-by\" : null,\n" + 
            "  \"order-asc\" : false,\n" + 
            "  \"query-params\" : [ {\n" + 
            "    \"cond-column\" : \"processinstanceid\",\n" + 
            "    \"cond-operator\" : \"GREATER_THAN\",\n" + 
            "    \"cond-values\" : [ 9 ]\n" + 
            "  } ],\n" + 
            "  \"result-column-mapping\" : null,\n" + 
            "  \"order-by-clause\" : null\n" + 
            "}";
    
    public static final String INTEGER_JSON = "10";
    public static final String DATE_JSON = "{\n" + 
            "  \"java.util.Date\" : 1540025263987\n" + 
            "}";
    public static final String BOOLEAN_JSON = "false";
    public static final String STRING_JSON = "\"Simple user task.\"";
    public static final String TASK_COMMENT_JSON = "{\n" + 
            "  \"comment-id\" : null,\n" + 
            "  \"comment\" : \"First comment.\",\n" + 
            "  \"comment-added-by\" : \"yoda\",\n" + 
            "  \"comment-added-at\" : {\n" + 
            "    \"java.util.Date\" : 1539939094774\n" + 
            "  }\n" + 
            "}";
    public static final String TASK_ATTACHMENT_JSON = VAR_JSON;
    public static final String TASK_JSON = "{\n" + 
            "  \"task-id\" : null,\n" + 
            "  \"task-priority\" : 10,\n" + 
            "  \"task-name\" : \"Modified name\",\n" + 
            "  \"task-subject\" : null,\n" + 
            "  \"task-description\" : \"Simple user task.\",\n" + 
            "  \"task-type\" : null,\n" + 
            "  \"task-form\" : null,\n" + 
            "  \"task-status\" : null,\n" + 
            "  \"task-actual-owner\" : null,\n" + 
            "  \"task-created-by\" : null,\n" + 
            "  \"task-created-on\" : null,\n" + 
            "  \"task-activation-time\" : null,\n" + 
            "  \"task-expiration-time\" : {\n" + 
            "    \"java.util.Date\" : 1540025025627\n" + 
            "  },\n" + 
            "  \"task-skippable\" : null,\n" + 
            "  \"task-workitem-id\" : null,\n" + 
            "  \"task-process-instance-id\" : null,\n" + 
            "  \"task-parent-id\" : null,\n" + 
            "  \"task-process-id\" : null,\n" + 
            "  \"task-container-id\" : null,\n" + 
            "  \"task-pot-owners\" : null,\n" + 
            "  \"task-excl-owners\" : null,\n" + 
            "  \"task-business-admins\" : null,\n" + 
            "  \"task-input-data\" : {\n" + 
            "    \"added input\" : \"test\"\n" + 
            "  },\n" + 
            "  \"task-output-data\" : {\n" + 
            "    \"person_\" : {\n" + 
            "      \"org.jbpm.data.Person\" : {\n" + 
            "        \"name\" : \"mary\"\n" + 
            "      }\n" + 
            "    },\n" + 
            "    \"string_\" : \"my custom data\"\n" + 
            "  }\n" + 
            "}";
    
    public static final String TIMER_VAR_MAP_JSON = "{\n" + 
            "  \"period\" : 0,\n" + 
            "  \"delay\" : 3,\n" + 
            "  \"repeatLimit\" : 0\n" + 
            "}";
    
    public static final String ORG_ENTITIES_LIST_JSON = "{\n" + 
            "  \"users\" : [ \"john\" ],\n" + 
            "  \"groups\" : null\n" + 
            "}";
    public static final String EMAIL_NOTIFICATION_JSON = "{\n" + 
            "  \"from\" : \"test@jbpm.org\",\n" + 
            "  \"reply-to\" : \"no-reply@jbpm.org\",\n" + 
            "  \"users\" : [ \"john\" ],\n" + 
            "  \"groups\" : null,\n" + 
            "  \"subject\" : \"reminder\",\n" + 
            "  \"body\" : \"my test content\"\n" + 
            "}";
    
    
    public static final String GET_PROCESS_DEF_RESPONSE_JSON = "{\n" + 
            "  \"associatedEntities\": {\n" + 
            "    \"Qualify\": [\n" + 
            "      \"approver\"\n" + 
            "    ],\n" + 
            "    \"Final Approval\": [\n" + 
            "      \"manager\"\n" + 
            "    ],\n" + 
            "    \"Correct Data\": [\n" + 
            "      \"broker\"\n" + 
            "    ],\n" + 
            "    \"Increase Down Payment\": [\n" + 
            "      \"broker\"\n" + 
            "    ]\n" + 
            "  },\n" + 
            "  \"serviceTasks\": {},\n" + 
            "  \"processVariables\": {\n" + 
            "    \"inlimit\": \"Boolean\",\n" + 
            "    \"application\": \"com.myspace.mortgage_app.Application\",\n" + 
            "    \"incdownpayment\": \"Boolean\"\n" + 
            "  },\n" + 
            "  \"reusableSubProcesses\": [],\n" + 
            "  \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\",\n" + 
            "  \"process-name\": \"MortgageApprovalProcess\",\n" + 
            "  \"process-version\": \"1.0\",\n" + 
            "  \"package\": \"com.myspace.mortgage_app\",\n" + 
            "  \"container-id\": \"mortgage-process_1.0.0-SNAPSHOT\",\n" + 
            "  \"dynamic\": false\n" + 
            "}";
    public static final String GET_PROCESS_SUBP_RESPONSE_JSON = "{\n" + 
            "  \"subProcesses\" : [ \"evaluation\" ]\n" + 
            "}";
    public static final String GET_PROCESS_VARS_RESPONSE_JSON = "{\n" + 
            "  \"variables\": {\n" + 
            "    \"inlimit\": \"Boolean\",\n" + 
            "    \"application\": \"com.myspace.mortgage_app.Application\",\n" + 
            "    \"incdownpayment\": \"Boolean\"\n" + 
            "  }\n" + 
            "}";
    public static final String GET_PROCESS_SERVICE_TASKS_RESPONSE_JSON = "{\n" + 
            "  \"serviceTasks\" : {\n" + 
            "    \"Email results\" : \"Email\"\n" + 
            "  }\n" + 
            "}";
    public static final String GET_PROCESS_ENTITIES_RESPONSE_JSON = "{\n" + 
            "  \"associatedEntities\": {\n" + 
            "    \"Qualify\": [\n" + 
            "      \"approver\"\n" + 
            "    ],\n" + 
            "    \"Final Approval\": [\n" + 
            "      \"manager\"\n" + 
            "    ],\n" + 
            "    \"Correct Data\": [\n" + 
            "      \"broker\"\n" + 
            "    ],\n" + 
            "    \"Increase Down Payment\": [\n" + 
            "      \"broker\"\n" + 
            "    ]\n" + 
            "  }\n" + 
            "}";
    public static final String GET_TASKS_RESPONSE_JSON = "{\n" + 
            "  \"task\": [\n" + 
            "    {\n" + 
            "      \"associatedEntities\": [\n" + 
            "        \"broker\"\n" + 
            "      ],\n" + 
            "      \"taskInputMappings\": {\n" + 
            "        \"application\": \"com.myspace.mortgage_app.Application\",\n" + 
            "        \"TaskName\": \"String\",\n" + 
            "        \"Skippable\": \"Object\",\n" + 
            "        \"GroupId\": \"Object\"\n" + 
            "      },\n" + 
            "      \"taskOutputMappings\": {\n" + 
            "        \"application\": \"com.myspace.mortgage_app.Application\"\n" + 
            "      },\n" + 
            "      \"task-id\": \"6\",\n" + 
            "      \"task-name\": \"Correct Data\",\n" + 
            "      \"task-priority\": 0,\n" + 
            "      \"task-comment\": \"\",\n" + 
            "      \"task-created-by\": \"\",\n" + 
            "      \"task-skippable\": false,\n" + 
            "      \"task-form-name\": \"CorrectData\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"associatedEntities\": [\n" + 
            "        \"approver\"\n" + 
            "      ],\n" + 
            "      \"taskInputMappings\": {\n" + 
            "        \"application\": \"com.myspace.mortgage_app.Application\",\n" + 
            "        \"TaskName\": \"String\",\n" + 
            "        \"Skippable\": \"Object\",\n" + 
            "        \"GroupId\": \"Object\"\n" + 
            "      },\n" + 
            "      \"taskOutputMappings\": {\n" + 
            "        \"inlimit\": \"Boolean\"\n" + 
            "      },\n" + 
            "      \"task-id\": \"8\",\n" + 
            "      \"task-name\": \"Qualify\",\n" + 
            "      \"task-priority\": 0,\n" + 
            "      \"task-comment\": \"\",\n" + 
            "      \"task-created-by\": \"\",\n" + 
            "      \"task-skippable\": false,\n" + 
            "      \"task-form-name\": \"Qualify\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"associatedEntities\": [\n" + 
            "        \"manager\"\n" + 
            "      ],\n" + 
            "      \"taskInputMappings\": {\n" + 
            "        \"inlimit\": \"Boolean\",\n" + 
            "        \"application\": \"com.myspace.mortgage_app.Application\",\n" + 
            "        \"TaskName\": \"String\",\n" + 
            "        \"Skippable\": \"Object\",\n" + 
            "        \"GroupId\": \"Object\"\n" + 
            "      },\n" + 
            "      \"taskOutputMappings\": {},\n" + 
            "      \"task-id\": \"10\",\n" + 
            "      \"task-name\": \"Final Approval\",\n" + 
            "      \"task-priority\": 0,\n" + 
            "      \"task-comment\": \"\",\n" + 
            "      \"task-created-by\": \"\",\n" + 
            "      \"task-skippable\": false,\n" + 
            "      \"task-form-name\": \"FinalApproval\"\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_TASK_INPUTS_RESPONSE_JSON = "{\n" + 
            "  \"taskInputs\": {\n" + 
            "    \"inlimit\": \"Boolean\",\n" + 
            "    \"application\": \"com.myspace.mortgage_app.Application\",\n" + 
            "    \"TaskName\": \"String\",\n" + 
            "    \"Skippable\": \"Object\",\n" + 
            "    \"GroupId\": \"Object\"\n" + 
            "  }\n" + 
            "}";
    public static final String GET_TASK_OUTPUTS_RESPONSE_JSON = "{\n" + 
            "  \"taskOutputs\": {\n" + 
            "    \"application\": \"com.myspace.mortgage_app.Application\"\n" + 
            "  }\n" + 
            "}";
    
    
    public static final String GET_DOCUMENT_RESPONSE_JSON = "{\n" + 
            "  \"document-id\": \"651a7035-3a90-4a25-bffb-bb09ea14fdac\",\n" + 
            "  \"document-name\": \"MyDocument\",\n" + 
            "  \"document-link\": \"\",\n" + 
            "  \"document-size\": 18,\n" + 
            "  \"document-last-mod\": {\n" + 
            "    \"java.util.Date\": 1539936629000\n" + 
            "  },\n" + 
            "  \"document-content\": \"VGhpcyBpcyBhIGRvY3VtZW50\"\n" + 
            "}";
    public static final String GET_DOCUMENTS_RESPONSE_JSON = "{\n" + 
            "  \"document-instances\": [\n" + 
            "    {\n" + 
            "      \"document-id\": \"001df463-2482-4dd5-abec-4622d16edaee\",\n" + 
            "      \"document-name\": \"MyDocument\",\n" + 
            "      \"document-link\": \"\",\n" + 
            "      \"document-size\": 18,\n" + 
            "      \"document-last-mod\": {\n" + 
            "        \"java.util.Date\": 1539936629000\n" + 
            "      },\n" + 
            "      \"document-content\": null\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"document-id\": \"651a7035-3a90-4a25-bffb-bb09ea14fdac\",\n" + 
            "      \"document-name\": \"MyDocument2\",\n" + 
            "      \"document-link\": \"\",\n" + 
            "      \"document-size\": 18,\n" + 
            "      \"document-last-mod\": {\n" + 
            "        \"java.util.Date\": 1539936629000\n" + 
            "      },\n" + 
            "      \"document-content\": null\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"document-id\": \"c10153e3-dfe7-41a9-811b-edd72c48f5d3\",\n" + 
            "      \"document-name\": \"MyDocument3\",\n" + 
            "      \"document-link\": \"\",\n" + 
            "      \"document-size\": 18,\n" + 
            "      \"document-last-mod\": {\n" + 
            "        \"java.util.Date\": 1539936629000\n" + 
            "      },\n" + 
            "      \"document-content\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String CREATE_DOC_RESPONSE_JSON = "a03672e8-8671-4d84-bd14-0ab5a341b80d";
    
    public static final String CREATE_JOB_RESPONSE_JSON = "123";
    public static final String GET_REQUEST_RESPONSE_JSON = "{\n" + 
            "  \"request-instance-id\": 6,\n" + 
            "  \"request-status\": \"DONE\",\n" + 
            "  \"request-business-key\": \"job3\",\n" + 
            "  \"request-message\": \"Ready to execute\",\n" + 
            "  \"request-retries\": 5,\n" + 
            "  \"request-executions\": 1,\n" + 
            "  \"request-command\": \"org.jbpm.executor.commands.LogCleanupCommand\",\n" + 
            "  \"request-scheduled-date\": {\n" + 
            "    \"java.util.Date\": 1540385144520\n" + 
            "  },\n" + 
            "  \"request-data\": {\n" + 
            "    \"retries\": \"5\",\n" + 
            "    \"businessKey\": \"job3\"\n" + 
            "  },\n" + 
            "  \"response-data\": {\n" + 
            "    \"RequestInfoLogsRemoved\": 2,\n" + 
            "    \"BAMLogRemoved\": 0,\n" + 
            "    \"VariableInstanceLogRemoved\": 0,\n" + 
            "    \"ProcessInstanceLogRemoved\": 0,\n" + 
            "    \"TaskAuditLogRemoved\": 0,\n" + 
            "    \"ErrorInfoLogsRemoved\": 0,\n" + 
            "    \"TaskEventLogRemoved\": 0,\n" + 
            "    \"TaskVariableLogRemoved\": 0,\n" + 
            "    \"NodeInstanceLogRemoved\": 0\n" + 
            "  },\n" + 
            "  \"request-errors\": {\n" + 
            "    \"error-info-instance\": []\n" + 
            "  },\n" + 
            "  \"request-container-id\": null\n" + 
            "}";
    public static final String GET_REQUESTS_RESPONSE_JSON = "{\n" + 
            "  \"request-info-instance\": [\n" + 
            "    {\n" + 
            "      \"request-instance-id\": 1,\n" + 
            "      \"request-status\": \"DONE\",\n" + 
            "      \"request-business-key\": \"job1\",\n" + 
            "      \"request-message\": \"Ready to execute\",\n" + 
            "      \"request-retries\": 2,\n" + 
            "      \"request-executions\": 1,\n" + 
            "      \"request-command\": \"org.jbpm.executor.commands.LogCleanupCommand\",\n" + 
            "      \"request-scheduled-date\": {\n" + 
            "        \"java.util.Date\": 1540299058209\n" + 
            "      },\n" + 
            "      \"request-data\": null,\n" + 
            "      \"response-data\": null,\n" + 
            "      \"request-errors\": null,\n" + 
            "      \"request-container-id\": null\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"request-instance-id\": 2,\n" + 
            "      \"request-status\": \"DONE\",\n" + 
            "      \"request-business-key\": \"job2\",\n" + 
            "      \"request-message\": \"Ready to execute\",\n" + 
            "      \"request-retries\": 0,\n" + 
            "      \"request-executions\": 0,\n" + 
            "      \"request-command\": \"java.lang.String\",\n" + 
            "      \"request-scheduled-date\": {\n" + 
            "        \"java.util.Date\": 1540328449419\n" + 
            "      },\n" + 
            "      \"request-data\": null,\n" + 
            "      \"response-data\": null,\n" + 
            "      \"request-errors\": null,\n" + 
            "      \"request-container-id\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    
    public static final String GET_PROCESS_INSTANCE_RESPONSE_JSON = "{\n" + 
            "  \"process-instance-id\": 1,\n" + 
            "  \"process-id\": \"Employee_Rostering.Process1\",\n" + 
            "  \"process-name\": \"Process1\",\n" + 
            "  \"process-version\": \"1.0\",\n" + 
            "  \"process-instance-state\": 1,\n" + 
            "  \"container-id\": \"employee-rostering\",\n" + 
            "  \"initiator\": \"baAdmin\",\n" + 
            "  \"start-date\": {\n" + 
            "    \"java.util.Date\": 1539184095041\n" + 
            "  },\n" + 
            "  \"process-instance-desc\": \"Process1\",\n" + 
            "  \"correlation-key\": \"1\",\n" + 
            "  \"parent-instance-id\": -1,\n" + 
            "  \"sla-compliance\": 0,\n" + 
            "  \"sla-due-date\": null,\n" + 
            "  \"active-user-tasks\": null,\n" + 
            "  \"process-instance-variables\": {\n" + 
            "    \"initiator\": \"baAdmin\"\n" + 
            "  }\n" + 
            "}";
    public static final String GET_PROCESS_INSTANCE_VAR_RESPONSE_JSON = "{\n" + 
            "  \"com.myspace.mortgage_app.Application\": {\n" + 
            "    \"applicant\": {\n" + 
            "      \"com.myspace.mortgage_app.Applicant\": {\n" + 
            "        \"name\": \"\",\n" + 
            "        \"annualincome\": 0,\n" + 
            "        \"address\": null,\n" + 
            "        \"ssn\": 0,\n" + 
            "        \"creditrating\": null\n" + 
            "      }\n" + 
            "    },\n" + 
            "    \"property\": {\n" + 
            "      \"com.myspace.mortgage_app.Property\": {\n" + 
            "        \"age\": 0,\n" + 
            "        \"address\": \"\",\n" + 
            "        \"locale\": \"\",\n" + 
            "        \"saleprice\": 0\n" + 
            "      }\n" + 
            "    },\n" + 
            "    \"downpayment\": 0,\n" + 
            "    \"amortization\": 0,\n" + 
            "    \"mortgageamount\": null\n" + 
            "  }\n" + 
            "}";
    public static final String GET_PROCESS_INSTANCE_VARS_RESPONSE_JSON = "{\n" + 
            "  \"application\": {\n" + 
            "    \"com.myspace.mortgage_app.Application\": {\n" + 
            "      \"applicant\": {\n" + 
            "        \"com.myspace.mortgage_app.Applicant\": {\n" + 
            "          \"name\": \"\",\n" + 
            "          \"annualincome\": 0,\n" + 
            "          \"address\": null,\n" + 
            "          \"ssn\": 0,\n" + 
            "          \"creditrating\": null\n" + 
            "        }\n" + 
            "      },\n" + 
            "      \"property\": {\n" + 
            "        \"com.myspace.mortgage_app.Property\": {\n" + 
            "          \"age\": 0,\n" + 
            "          \"address\": \"\",\n" + 
            "          \"locale\": \"\",\n" + 
            "          \"saleprice\": 0\n" + 
            "        }\n" + 
            "      },\n" + 
            "      \"downpayment\": 0,\n" + 
            "      \"amortization\": 0,\n" + 
            "      \"mortgageamount\": null\n" + 
            "    }\n" + 
            "  },\n" + 
            "  \"initiator\": \"baAdmin\"\n" + 
            "}";
    public static final String GET_PROCESS_INSTANCE_SIGNALS_RESPONSE_JSON = "["
            + " \"wait\","
            + " \"another\""
            + "]";
    public static final String GET_PROCESS_INSTANCE_WORK_ITEM_RESPONSE_JSON = "{\n" + 
            "  \"work-item-instance\": [\n" + 
            "    {\n" + 
            "      \"work-item-id\": 4,\n" + 
            "      \"work-item-name\": \"Human Task\",\n" + 
            "      \"work-item-state\": 0,\n" + 
            "      \"work-item-params\": {\n" + 
            "        \"application\": {\n" + 
            "          \"com.myspace.mortgage_app.Application\": {\n" + 
            "            \"applicant\": {\n" + 
            "              \"com.myspace.mortgage_app.Applicant\": {\n" + 
            "                \"name\": \"NewName\",\n" + 
            "                \"annualincome\": 0,\n" + 
            "                \"address\": null,\n" + 
            "                \"ssn\": 0,\n" + 
            "                \"creditrating\": null\n" + 
            "              }\n" + 
            "            },\n" + 
            "            \"property\": {\n" + 
            "              \"com.myspace.mortgage_app.Property\": {\n" + 
            "                \"age\": 0,\n" + 
            "                \"address\": \"NewAddress\",\n" + 
            "                \"locale\": \"\",\n" + 
            "                \"saleprice\": 0\n" + 
            "              }\n" + 
            "            },\n" + 
            "            \"downpayment\": 0,\n" + 
            "            \"amortization\": 0,\n" + 
            "            \"mortgageamount\": null\n" + 
            "          }\n" + 
            "        },\n" + 
            "        \"TaskName\": \"CorrectData\",\n" + 
            "        \"NodeName\": \"Correct Data\",\n" + 
            "        \"Skippable\": \"false\",\n" + 
            "        \"GroupId\": \"broker\"\n" + 
            "      },\n" + 
            "      \"process-instance-id\": 4,\n" + 
            "      \"container-id\": \"mortgage-process_1.0.0-SNAPSHOT\",\n" + 
            "      \"node-instance-id\": 5,\n" + 
            "      \"node-id\": 6\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_PROCESS_INSTANCE_WORK_ITEMS_RESPONSE_JSON = "{\n" + 
            "  \"work-item-instance\": [\n" + 
            "    {\n" + 
            "      \"work-item-id\": 4,\n" + 
            "      \"work-item-name\": \"Human Task\",\n" + 
            "      \"work-item-state\": 0,\n" + 
            "      \"work-item-params\": {\n" + 
            "        \"application\": {\n" + 
            "          \"com.myspace.mortgage_app.Application\": {\n" + 
            "            \"applicant\": {\n" + 
            "              \"com.myspace.mortgage_app.Applicant\": {\n" + 
            "                \"name\": \"NewName\",\n" + 
            "                \"annualincome\": 0,\n" + 
            "                \"address\": null,\n" + 
            "                \"ssn\": 0,\n" + 
            "                \"creditrating\": null\n" + 
            "              }\n" + 
            "            },\n" + 
            "            \"property\": {\n" + 
            "              \"com.myspace.mortgage_app.Property\": {\n" + 
            "                \"age\": 0,\n" + 
            "                \"address\": \"NewAddress\",\n" + 
            "                \"locale\": \"\",\n" + 
            "                \"saleprice\": 0\n" + 
            "              }\n" + 
            "            },\n" + 
            "            \"downpayment\": 0,\n" + 
            "            \"amortization\": 0,\n" + 
            "            \"mortgageamount\": null\n" + 
            "          }\n" + 
            "        },\n" + 
            "        \"TaskName\": \"CorrectData\",\n" + 
            "        \"NodeName\": \"Correct Data\",\n" + 
            "        \"Skippable\": \"false\",\n" + 
            "        \"GroupId\": \"broker\"\n" + 
            "      },\n" + 
            "      \"process-instance-id\": 4,\n" + 
            "      \"container-id\": \"mortgage-process_1.0.0-SNAPSHOT\",\n" + 
            "      \"node-instance-id\": 5,\n" + 
            "      \"node-id\": 6\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_PROCESS_DEFS_RESPONSE_JSON = "{\n" + 
            "  \"processes\": [\n" + 
            "    {\n" + 
            "      \"associatedEntities\": null,\n" + 
            "      \"serviceTasks\": null,\n" + 
            "      \"processVariables\": null,\n" + 
            "      \"reusableSubProcesses\": null,\n" + 
            "      \"process-id\": \"Employee_Rostering.Process1\",\n" + 
            "      \"process-name\": \"Process1\",\n" + 
            "      \"process-version\": \"1.0\",\n" + 
            "      \"package\": \"employeerostering.employeerostering\",\n" + 
            "      \"container-id\": \"employee-rostering\",\n" + 
            "      \"dynamic\": false\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_PROCESS_INSTANCE_NODES_RESPONSE_JSON = "{\n" + 
            "  \"node-instance\": [\n" + 
            "    {\n" + 
            "      \"node-instance-id\": 1,\n" + 
            "      \"node-name\": \"Task\",\n" + 
            "      \"process-instance-id\": 2,\n" + 
            "      \"work-item-id\": 2,\n" + 
            "      \"container-id\": \"employee-rostering\",\n" + 
            "      \"start-date\": {\n" + 
            "        \"java.util.Date\": 1539184095048\n" + 
            "      },\n" + 
            "      \"node-id\": \"_5F8EED98-433C-4E7B-97BC-0E70615F13CB\",\n" + 
            "      \"node-type\": \"HumanTaskNode\",\n" + 
            "      \"node-connection\": \"_E89FEB0F-B8E1-4138-9DF0-397C9F9A6512\",\n" + 
            "      \"node-completed\": false,\n" + 
            "      \"reference-id\": null,\n" + 
            "      \"sla-compliance\": 0,\n" + 
            "      \"sla-due-date\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_PROCESS_INSTANCE_VARS_LOG_RESPONSE_JSON = "{\n" + 
            "  \"variable-instance\": [\n" + 
            "    {\n" + 
            "      \"name\": \"initiator\",\n" + 
            "      \"old-value\": \"\",\n" + 
            "      \"value\": \"baAdmin\",\n" + 
            "      \"process-instance-id\": 2,\n" + 
            "      \"modification-date\": {\n" + 
            "        \"java.util.Date\": 1539610491992\n" + 
            "      }\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"application\",\n" + 
            "      \"old-value\": \"com.myspace.mortgage_app.Application@bd449b3\",\n" + 
            "      \"value\": \"com.myspace.mortgage_app.Application@bd449b3\",\n" + 
            "      \"process-instance-id\": 2,\n" + 
            "      \"modification-date\": {\n" + 
            "        \"java.util.Date\": 1539610492006\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_PROCESS_INSTANCES_RESPONSE_JSON = "{\n" + 
            "  \"process-instance\": [\n" + 
            "    {\n" + 
            "      \"process-instance-id\": 1,\n" + 
            "      \"process-id\": \"Employee_Rostering.Process1\",\n" + 
            "      \"process-name\": \"Process1\",\n" + 
            "      \"process-version\": \"1.0\",\n" + 
            "      \"process-instance-state\": 1,\n" + 
            "      \"container-id\": \"employee-rostering\",\n" + 
            "      \"initiator\": \"baAdmin\",\n" + 
            "      \"start-date\": {\n" + 
            "        \"java.util.Date\": 1539184095041\n" + 
            "      },\n" + 
            "      \"process-instance-desc\": \"Process1\",\n" + 
            "      \"correlation-key\": \"1\",\n" + 
            "      \"parent-instance-id\": -1,\n" + 
            "      \"sla-compliance\": 0,\n" + 
            "      \"sla-due-date\": null,\n" + 
            "      \"active-user-tasks\": null,\n" + 
            "      \"process-instance-variables\": null\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"process-instance-id\": 2,\n" + 
            "      \"process-id\": \"Employee_Rostering.Process2\",\n" + 
            "      \"process-name\": \"Process2\",\n" + 
            "      \"process-version\": \"1.0\",\n" + 
            "      \"process-instance-state\": 1,\n" + 
            "      \"container-id\": \"employee-rostering\",\n" + 
            "      \"initiator\": \"baAdmin\",\n" + 
            "      \"start-date\": {\n" + 
            "        \"java.util.Date\": 1539184303976\n" + 
            "      },\n" + 
            "      \"process-instance-desc\": \"Process2\",\n" + 
            "      \"correlation-key\": \"2\",\n" + 
            "      \"parent-instance-id\": -1,\n" + 
            "      \"sla-compliance\": 0,\n" + 
            "      \"sla-due-date\": null,\n" + 
            "      \"active-user-tasks\": null,\n" + 
            "      \"process-instance-variables\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    public static final String QUERY_DEF_LIST_RESPONSE_JSON = "{\n" + 
            "  \"queries\": [\n" + 
            "    {\n" + 
            "      \"query-name\": \"tasksMonitoring\",\n" + 
            "      \"query-source\": \"java:jboss/datasources/ExampleDS\",\n" + 
            "      \"query-expression\": \"select p.processName, p.externalId, t.taskId, t.taskName, t.status, t.createdDate, t.startDate, t.endDate, t.processInstanceId, t.userId, t.duration from ProcessInstanceLog p inner join BAMTaskSummary t on (t.processInstanceId = p.processInstanceId) inner join (select min(pk) as pk from BAMTaskSummary group by taskId) d on t.pk = d.pk\",\n" + 
            "      \"query-target\": \"CUSTOM\",\n" + 
            "      \"query-columns\": {}\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"query-name\": \"jbpmExecutionErrorList\",\n" + 
            "      \"query-source\": \"java:jboss/datasources/ExampleDS\",\n" + 
            "      \"query-expression\": \"select eri.ERROR_ACK, eri.ERROR_ACK_BY, eri.ERROR_ACK_AT, eri.ACTIVITY_ID, eri.ACTIVITY_NAME, eri.DEPLOYMENT_ID, eri.ERROR_DATE, eri.ERROR_ID, eri.ERROR_MSG, eri.JOB_ID, eri.PROCESS_ID, eri.PROCESS_INST_ID, eri.ERROR_TYPE from ExecutionErrorInfo eri\",\n" + 
            "      \"query-target\": \"CUSTOM\",\n" + 
            "      \"query-columns\": {}\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"query-name\": \"jbpmRequestList\",\n" + 
            "      \"query-source\": \"java:jboss/datasources/ExampleDS\",\n" + 
            "      \"query-expression\": \"select ri.id, ri.timestamp, ri.status, ri.commandName, ri.message, ri.businessKey, ri.retries, ri.executions, pil.processName, pil.processInstanceId, pil.processInstanceDescription, ri.deploymentId from RequestInfo ri left join ProcessInstanceLog pil on pil.processInstanceId=ri.processInstanceId\",\n" + 
            "      \"query-target\": \"CUSTOM\",\n" + 
            "      \"query-columns\": {}\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    public static final String QUERY_DEF_RESPONSE_JSON = "{\n" + 
            "  \"query-name\": \"jbpmProcessInstancesWithVariables1\",\n" + 
            "  \"query-source\": \"java:jboss/datasources/ExampleDS\",\n" + 
            "  \"query-expression\": \"select vil.processInstanceId, vil.processId, vil.id, vil.variableId, vil.value from VariableInstanceLog vil where vil.id in (select MAX(v.id) from VariableInstanceLog v group by v.variableId, v.processInstanceId)\",\n" + 
            "  \"query-target\": \"CUSTOM\",\n" + 
            "  \"query-columns\": {\n" + 
            "    \"PROCESSINSTANCEID\": \"NUMBER\",\n" + 
            "    \"VARIABLEID\": \"LABEL\",\n" + 
            "    \"ID\": \"NUMBER\",\n" + 
            "    \"VALUE\": \"LABEL\",\n" + 
            "    \"PROCESSID\": \"LABEL\"\n" + 
            "  }\n" + 
            "}";
    
    public static final String GET_PROCESS_INSTANCE_NODE_RESPONSE_JSON = "{\n" + 
            "  \"node-instance-id\": 6,\n" + 
            "  \"node-name\": \"Correct Data\",\n" + 
            "  \"process-instance-id\": 7,\n" + 
            "  \"work-item-id\": 9,\n" + 
            "  \"container-id\": \"mortgage-process2\",\n" + 
            "  \"start-date\": {\n" + 
            "    \"java.util.Date\": 1539810634380\n" + 
            "  },\n" + 
            "  \"node-id\": \"_011ED858-F841-4C44-B0F1-F3BE388ADDA5\",\n" + 
            "  \"node-type\": \"HumanTaskNode\",\n" + 
            "  \"node-connection\": null,\n" + 
            "  \"node-completed\": false,\n" + 
            "  \"reference-id\": null,\n" + 
            "  \"sla-compliance\": 0,\n" + 
            "  \"sla-due-date\": null\n" + 
            "}";
    public static final String GET_TASK_RESPONSE_JSON = "{\n" + 
            "  \"task-id\": 1,\n" + 
            "  \"task-priority\": 0,\n" + 
            "  \"task-name\": \"Self Evaluation\",\n" + 
            "  \"task-subject\": null,\n" + 
            "  \"task-description\": \"Please perform a self-evalutation.\",\n" + 
            "  \"task-type\": null,\n" + 
            "  \"task-form\": null,\n" + 
            "  \"task-status\": \"Ready\",\n" + 
            "  \"task-actual-owner\": \"kiesu\",\n" + 
            "  \"task-created-by\": \"Jane\",\n" + 
            "  \"task-created-on\": {\n" + 
            "    \"java.util.Date\": 1539623679113\n" + 
            "  },\n" + 
            "  \"task-activation-time\": {\n" + 
            "    \"java.util.Date\": 1539623679113\n" + 
            "  },\n" + 
            "  \"task-expiration-time\": null,\n" + 
            "  \"task-skippable\": null,\n" + 
            "  \"task-workitem-id\": null,\n" + 
            "  \"task-process-instance-id\": 4,\n" + 
            "  \"task-parent-id\": null,\n" + 
            "  \"task-process-id\": \"Mortgage_Process.MortgageApprovalProcess\",\n" + 
            "  \"task-container-id\": \"mortgage-process_1.0.0-SNAPSHOT\",\n" + 
            "  \"task-pot-owners\": \"Jane\",\n" + 
            "  \"task-excl-owners\": null,\n" + 
            "  \"task-business-admins\": \"John\",\n" + 
            "  \"task-input-data\": null,\n" + 
            "  \"task-output-data\": null\n" + 
            "}";
    public static final String GET_TASK_SUMMARY_RESPONSE_JSON = "{\n" + 
            "  \"task-summary\": [\n" + 
            "    {\n" + 
            "      \"task-id\": \"2\",\n" + 
            "      \"task-name\": \"Self Evaluation\",\n" + 
            "      \"task-subject\": \"\",\n" + 
            "      \"task-description\": \"Please perform a self-evalutation.\",\n" + 
            "      \"task-status\": \"Ready\",\n" + 
            "      \"task-priority\": \"0\",\n" + 
            "      \"task-is-skippable\": \"false\",\n" + 
            "      \"task-created-by\": \"John\",\n" + 
            "      \"task-created-on\": \"2016-04-05T15:09:14.206+02:00\",\n" + 
            "      \"task-activation-time\": \"2016-04-05T15:09:14.206+02:00\",\n" + 
            "      \"task-proc-inst-id\": \"2\",\n" + 
            "      \"task-proc-def-id\": \"evaluation\",\n" + 
            "      \"task-container-id\": \"myContainer\",\n" + 
            "      \"task-parent-id\": \"-1\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"task-id\": \"1\",\n" + 
            "      \"task-name\": \"Self Evaluation\",\n" + 
            "      \"task-subject\": \"\",\n" + 
            "      \"task-description\": \"Please perform a self-evalutation.\",\n" + 
            "      \"task-status\": \"InProgress\",\n" + 
            "      \"task-priority\": \"0\",\n" + 
            "      \"task-is-skippable\": \"false\",\n" + 
            "      \"task-actual-owner\": \"kiesu\",\n" + 
            "      \"task-created-by\": \"John\",\n" + 
            "      \"task-created-on\": \"2016-04-05T15:05:06.508+02:00\",\n" + 
            "      \"task-activation-time\": \"2016-04-05T15:05:06.508+02:00\",\n" + 
            "      \"task-proc-inst-id\": \"1\",\n" + 
            "      \"task-proc-def-id\": \"evaluation\",\n" + 
            "      \"task-container-id\": \"myContainer\",\n" + 
            "      \"task-parent-id\": \"-1\"\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_TASK_EVENTS_RESPONSE_JSON = "{\n" + 
            "  \"task-event-instance\": [\n" + 
            "    {\n" + 
            "      \"task-event-id\": 4,\n" + 
            "      \"task-id\": 4,\n" + 
            "      \"task-event-type\": \"STARTED\",\n" + 
            "      \"task-event-user\": \"Mortgage_Process.MortgageApprovalProcess\",\n" + 
            "      \"task-event-date\": {\n" + 
            "        \"java.util.Date\": 1539623679130\n" + 
            "      },\n" + 
            "      \"task-process-instance-id\": 4,\n" + 
            "      \"task-work-item-id\": 4,\n" + 
            "      \"task-event-message\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    public static final String GET_TASK_COMMENTS_RESPONSE_JSON = "{\n" + 
            "  \"task-comment\": [\n" + 
            "    {\n" + 
            "      \"comment-id\": 1,\n" + 
            "      \"comment\": \"Ensure that this self evaluation is completed before the HR and PM evaluations.\",\n" + 
            "      \"comment-added-by\": \"baAdmin\",\n" + 
            "      \"comment-added-at\": {\n" + 
            "        \"java.util.Date\": 1540238822132\n" + 
            "      }\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"comment-id\": 2,\n" + 
            "      \"comment\": \"Task must be assigned to administrator.\",\n" + 
            "      \"comment-added-by\": \"baAdmin\",\n" + 
            "      \"comment-added-at\": {\n" + 
            "        \"java.util.Date\": 1540238822140\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_TASK_ATTACHMENTS_RESPONSE_JSON = "{\n" + 
            "  \"task-attachment\": [\n" + 
            "    {\n" + 
            "      \"attachment-id\": 1,\n" + 
            "      \"attachment-name\": \"Task Attachment\",\n" + 
            "      \"attachment-added-by\": \"baAdmin\",\n" + 
            "      \"attachment-added-at\": {\n" + 
            "        \"java.util.Date\": 1540229715779\n" + 
            "      },\n" + 
            "      \"attachment-type\": \"java.util.LinkedHashMap\",\n" + 
            "      \"attachment-size\": 233,\n" + 
            "      \"attachment-content-id\": 31\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"attachment-id\": 2,\n" + 
            "      \"attachment-name\": \"Task Attachment 2\",\n" + 
            "      \"attachment-added-by\": \"baAdmin\",\n" + 
            "      \"attachment-added-at\": {\n" + 
            "        \"java.util.Date\": 1540229715780\n" + 
            "      },\n" + 
            "      \"attachment-type\": \"java.util.LinkedHashMap\",\n" + 
            "      \"attachment-size\": 300,\n" + 
            "      \"attachment-content-id\": 32\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    public static final String GET_MIGRATION_REPORT_RESPONSE_JSON = "{\n" + 
            "  \"migration-successful\": true,\n" + 
            "  \"migration-start\": \"2018-10-15T15:12:47.194Z\",\n" + 
            "  \"migration-end\": \"2018-10-15T15:12:47.194Z\",\n" + 
            "  \"migration-logs\": [\n" + 
            "    \"15-Oct-2018 15:56:23: StartNode () - Human\\n15-Oct-2018 15:56:23: Join () - System\\n15-Oct-2018 15:56:23: RuleSetNode (Validation) - System\\n15-Oct-2018 15:56:23: Split () - System\\n15-Oct-2018 15:56:23: RuleSetNode (Retract Validation) - System\\n15-Oct-2018 15:56:23: HumanTaskNode (Correct Data) - System\\n15-Oct-2018 15:56:23: RuleSetNode (Retract Validation) Completed - System\\n15-Oct-2018 15:56:23: Split () Completed - System\\n15-Oct-2018 15:56:23: RuleSetNode (Validation) Completed - System\\n15-Oct-2018 15:56:23: Join () Completed - System\\n15-Oct-2018 15:56:23: StartNode () Completed - System\"\n" + 
            "  ],\n" + 
            "  \"migration-process-instance\": 4\n" + 
            "}";
    public static final String GET_MIGRATION_REPORTS_RESPONSE_JSON = "{\n" + 
            "  \"migration-report-instance\": [\n" + 
            "    {\n" + 
            "      \"migration-successful\": true,\n" + 
            "      \"migration-start\": \"2018-10-15T15:12:47.194Z\",\n" + 
            "      \"migration-end\": \"2018-10-15T15:13:17.202Z\",\n" + 
            "      \"migration-logs\": [\n" + 
            "        \"15-Oct-2018 15:56:23: StartNode () - Human\\n15-Oct-2018 15:56:23: Join () - System\\n15-Oct-2018 15:56:23: RuleSetNode (Validation) - System\\n15-Oct-2018 15:56:23: Split () - System\\n15-Oct-2018 15:56:23: RuleSetNode (Retract Validation) - System\\n15-Oct-2018 15:56:23: HumanTaskNode (Correct Data) - System\\n15-Oct-2018 15:56:23: RuleSetNode (Retract Validation) Completed - System\\n15-Oct-2018 15:56:23: Split () Completed - System\\n15-Oct-2018 15:56:23: RuleSetNode (Validation) Completed - System\\n15-Oct-2018 15:56:23: Join () Completed - System\\n15-Oct-2018 15:56:23: StartNode () Completed - System\"\n" + 
            "      ],\n" + 
            "      \"migration-process-instance\": 5\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"migration-successful\": true,\n" + 
            "      \"migration-start\": \"2018-10-15T15:13:17.202Z\",\n" + 
            "      \"migration-end\": \"2018-10-15T15:13:47.194Z\",\n" + 
            "      \"migration-logs\": [\n" + 
            "        \"15-Oct-2018 15:56:23: StartNode () - Human\\n15-Oct-2018 15:56:23: Join () - System\\n15-Oct-2018 15:56:23: RuleSetNode (Validation) - System\\n15-Oct-2018 15:56:23: Split () - System\\n15-Oct-2018 15:56:23: RuleSetNode (Retract Validation) - System\\n15-Oct-2018 15:56:23: HumanTaskNode (Correct Data) - System\\n15-Oct-2018 15:56:23: RuleSetNode (Retract Validation) Completed - System\\n15-Oct-2018 15:56:23: Split () Completed - System\\n15-Oct-2018 15:56:23: RuleSetNode (Validation) Completed - System\\n15-Oct-2018 15:56:23: Join () Completed - System\\n15-Oct-2018 15:56:23: StartNode () Completed - System\"\n" + 
            "      ],\n" + 
            "      \"migration-process-instance\": 6\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_TIMERS_RESPONSE_JSON = "{\n" + 
            "  \"timer-instance\": [\n" + 
            "    {\n" + 
            "      \"name\": \"MyTimer\",\n" + 
            "      \"id\": 1,\n" + 
            "      \"activation-time\": \"2018-10-18T04:49:28.907Z\",\n" + 
            "      \"last-fire-time\": \"2018-10-18T04:49:28.907Z\",\n" + 
            "      \"next-fire-time\": \"2018-10-18T04:49:28.907Z\",\n" + 
            "      \"delay\": 35000,\n" + 
            "      \"period\": 500000,\n" + 
            "      \"repeat-limit\": 2,\n" + 
            "      \"process-instance-id\": 6,\n" + 
            "      \"session-id\": 9\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_PROCESS_NODES_RESPONSE_JSON = "{\n" + 
            "  \"process-node\": [\n" + 
            "    {\n" + 
            "      \"name\": \"\",\n" + 
            "      \"id\": 1,\n" + 
            "      \"type\": \"StartNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"\",\n" + 
            "      \"id\": 2,\n" + 
            "      \"type\": \"Join\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Validation\",\n" + 
            "      \"id\": 3,\n" + 
            "      \"type\": \"RuleSetNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"\",\n" + 
            "      \"id\": 4,\n" + 
            "      \"type\": \"Split\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Retract Validation\",\n" + 
            "      \"id\": 5,\n" + 
            "      \"type\": \"RuleSetNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Correct Data\",\n" + 
            "      \"id\": 6,\n" + 
            "      \"type\": \"HumanTaskNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Mortgage Calculation\",\n" + 
            "      \"id\": 7,\n" + 
            "      \"type\": \"RuleSetNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Qualify\",\n" + 
            "      \"id\": 8,\n" + 
            "      \"type\": \"HumanTaskNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"\",\n" + 
            "      \"id\": 9,\n" + 
            "      \"type\": \"Split\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Final Approval\",\n" + 
            "      \"id\": 10,\n" + 
            "      \"type\": \"HumanTaskNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"\",\n" + 
            "      \"id\": 11,\n" + 
            "      \"type\": \"EndNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Increase Down Payment\",\n" + 
            "      \"id\": 12,\n" + 
            "      \"type\": \"HumanTaskNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"\",\n" + 
            "      \"id\": 13,\n" + 
            "      \"type\": \"Split\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"\",\n" + 
            "      \"id\": 14,\n" + 
            "      \"type\": \"EndNode\",\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\"\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_EXEC_ERROR_RESPONSE_JSON = "{\n" + 
            "      \"id\": \"a7982044-019d-4d4a-be3f-781f4ddca1df\",\n" + 
            "      \"type\": \"Process\",\n" + 
            "      \"container-id\": \"mortgage-process_1.0.0-SNAPSHOT\",\n" + 
            "      \"process-instance-id\": 4,\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\",\n" + 
            "      \"activity-id\": 12,\n" + 
            "      \"activity-name\": \"Validation\",\n" + 
            "      \"job-id\": null,\n" + 
            "      \"error-msg\": \"[Mortgage_Process.MortgageApprovalProcess:4 - Validation:3] -- [Mortgage_Process.MortgageApprovalProcess:4 - Validation:3] -- null\",\n" + 
            "      \"error\": null,\n" + 
            "      \"acknowledged\": false,\n" + 
            "      \"acknowledged-by\": null,\n" + 
            "      \"acknowledged-at\": null,\n" + 
            "      \"error-date\": 1539627364193\n" + 
            "    }";
    public static final String GET_EXEC_ERRORS_RESPONSE_JSON = "{\n" + 
            "  \"error-instance\": [\n" + 
            "    {\n" + 
            "      \"id\": \"54b04160-6242-475d-9452-0df3678123b0\",\n" + 
            "      \"type\": \"Process\",\n" + 
            "      \"container-id\": \"mortgage-process_1.0.0-SNAPSHOT\",\n" + 
            "      \"process-instance-id\": 4,\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\",\n" + 
            "      \"activity-id\": 12,\n" + 
            "      \"activity-name\": \"Validation\",\n" + 
            "      \"job-id\": null,\n" + 
            "      \"error-msg\": \"[Mortgage_Process.MortgageApprovalProcess:4 - Validation:3] -- [Mortgage_Process.MortgageApprovalProcess:4 - Validation:3] -- null\",\n" + 
            "      \"error\": null,\n" + 
            "      \"acknowledged\": false,\n" + 
            "      \"acknowledged-by\": null,\n" + 
            "      \"acknowledged-at\": null,\n" + 
            "      \"error-date\": 1539627373788\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"id\": \"a7982044-019d-4d4a-be3f-781f4ddca1df\",\n" + 
            "      \"type\": \"Process\",\n" + 
            "      \"container-id\": \"mortgage-process_1.0.0-SNAPSHOT\",\n" + 
            "      \"process-instance-id\": 4,\n" + 
            "      \"process-id\": \"Mortgage_Process.MortgageApprovalProcess\",\n" + 
            "      \"activity-id\": 12,\n" + 
            "      \"activity-name\": \"Validation\",\n" + 
            "      \"job-id\": null,\n" + 
            "      \"error-msg\": \"[Mortgage_Process.MortgageApprovalProcess:4 - Validation:3] -- [Mortgage_Process.MortgageApprovalProcess:4 - Validation:3] -- null\",\n" + 
            "      \"error\": null,\n" + 
            "      \"acknowledged\": false,\n" + 
            "      \"acknowledged-by\": null,\n" + 
            "      \"acknowledged-at\": null,\n" + 
            "      \"error-date\": 1539627364193\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    public static final String GET_TASK_REASSIGNMENTS_RESPONSE_JSON = "{\n" + 
            "  \"task-reassignment\": [\n" + 
            "    {\n" + 
            "      \"id\": 9995,\n" + 
            "      \"name\": \"\",\n" + 
            "      \"reassign-at\": 1540394323872,\n" + 
            "      \"users\": [\n" + 
            "        \"John\",\n" + 
            "        \"Jane\"\n" + 
            "      ],\n" + 
            "      \"groups\": [\n" + 
            "        \"IT\"\n" + 
            "      ],\n" + 
            "      \"active\": true\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_TASK_NOTIFICATIONS_RESPONSE_JSON = "{\n" + 
            "  \"task-notification\": [\n" + 
            "    {\n" + 
            "      \"id\": 11070,\n" + 
            "      \"name\": null,\n" + 
            "      \"notify-at\": 1540396524172,\n" + 
            "      \"users\": [\n" + 
            "        \"Sheldon\"\n" + 
            "      ],\n" + 
            "      \"groups\": [\n" + 
            "        \"IT\"\n" + 
            "      ],\n" + 
            "      \"active\": true,\n" + 
            "      \"subject\": \"You hava a task not started\",\n" + 
            "      \"content\": \"You have been assigned to a task (task-id ${taskId}).\\n  Important technical information that can be of use when working on it:\\n    - process instance id - ${processInstanceId}\\n  - work item id - ${workItemId}\\n  - work item id - ${workItemId}\\n   Regards from dev team\"\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    /*
     * 
     * XML (JAXB) sample payloads
     * 
     */
    
    
    public static final String SIMPLE_VAR_MAP_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<map-type>\n" + 
            "    <entries>\n" + 
            "        <entry>\n" + 
            "            <key>age</key>\n" + 
            "            <value xsi:type=\"xs:int\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n " +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">25</value>\n" + 
            "        </entry>\n" + 
            "        <entry>\n" + 
            "            <key>name</key>\n" + 
            "            <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n " +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">john</value>\n" + 
            "        </entry>\n" + 
            "    </entries>\n" + 
            "</map-type>";
    
    public static final String VAR_MAP_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<map-type>\n" + 
            "    <entries>\n" + 
            "        <entry>\n" + 
            "            <key>age</key>\n" + 
            "            <value xsi:type=\"xs:int\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n " +
            "                   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">25</value>\n" + 
            "        </entry>\n" + 
            "        <entry>\n" + 
            "            <key>person</key>\n" + 
            "            <value xsi:type=\"person\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
            "                <name>john</name>\n" + 
            "            </value>\n" + 
            "        </entry>\n" + 
            "    </entries>\n" + 
            "</map-type>";
    
    public static final String VAR_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<person>\n" + 
            "    <name>john</name>\n" + 
            "</person>";
    
    public static final String DOCUMENT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<document-instance>\n" + 
            "    <document-name>first document</document-name>\n" + 
            "    <document-size>17</document-size>\n" + 
            "    <document-last-mod>2018-10-19T10:10:28.914+02:00</document-last-mod>\n" + 
            "    <document-content>anVzdCB0ZXh0IGNvbnRlbnQ=</document-content>\n" + 
            "</document-instance>";
    
    public static final String JOB_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<job-request-instance>\n" + 
            "    <job-command>org.jbpm.executor.commands.PrintOutCommand</job-command>\n" + 
            "    <scheduled-date>2018-10-20T10:16:51.326+02:00</scheduled-date>\n" + 
            "    <data>\n" + 
            "        <entry>\n" + 
            "            <key>businessKey</key>\n" + 
            "            <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">test key</value>\n" + 
            "        </entry>\n" + 
            "    </data>\n" + 
            "</job-request-instance>";
    
    public static final String QUERY_DEF_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<query-definition>\n" + 
            "    <query-name>allProcessInstances</query-name>\n" + 
            "    <query-source>jdbc/jbpm-ds</query-source>\n" + 
            "    <query-expression>select * from ProcessInstanceLog where status = 1</query-expression>\n" + 
            "    <query-target>PROCESS</query-target>\n" + 
            "    <columns></columns>\n" + 
            "</query-definition>";
    
    public static final String QUERY_FILTER_SPEC_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<query-filter-spec>\n" + 
            "    <order-asc>false</order-asc>\n" + 
            "    <query-params>\n" + 
            "        <cond-column>processinstanceid</cond-column>\n" + 
            "        <cond-operator>GREATER_THAN</cond-operator>\n" + 
            "        <cond-values xsi:type=\"xs:long\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">9</cond-values>\n" + 
            "    </query-params>\n" + 
            "</query-filter-spec>";
    
    public static final String INTEGER_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<int-type>\n" + 
            "    <value>10</value>\n" + 
            "</int-type>";
    public static final String DATE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<date-type>\n" + 
            "    <value>2018-10-20T10:47:43.607+02:00</value>\n" + 
            "</date-type>";
    public static final String BOOLEAN_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<boolean-type>\n" + 
            "    <value>false</value>\n" + 
            "</boolean-type>";
    public static final String STRING_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<string-type>\n" + 
            "    <value>Simple user task.</value>\n" + 
            "</string-type>";
    public static final String TASK_COMMENT_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<task-comment>\n" + 
            "    <comment>First comment.</comment>\n" + 
            "    <comment-added-by>yoda</comment-added-by>\n" + 
            "    <comment-added-at>2018-10-19T10:51:34.405+02:00</comment-added-at>\n" + 
            "</task-comment>";
    public static final String TASK_ATTACHMENT_XML = VAR_XML;
    public static final String TASK_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<task-instance>\n" + 
            "    <task-priority>10</task-priority>\n" + 
            "    <task-name>Modified name</task-name>\n" + 
            "    <task-description>Simple user task.</task-description>\n" + 
            "    <task-expiration-time>2018-10-20T10:43:45.273+02:00</task-expiration-time>\n" + 
            "    <inputData>\n" + 
            "        <entry>\n" + 
            "            <key>added input</key>\n" + 
            "            <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">test</value>\n" + 
            "        </entry>\n" + 
            "    </inputData>\n" + 
            "    <outputData>\n" + 
            "        <entry>\n" + 
            "            <key>person_</key>\n" + 
            "            <value xsi:type=\"person\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
            "                <name>mary</name>\n" + 
            "            </value>\n" + 
            "        </entry>\n" + 
            "        <entry>\n" + 
            "            <key>string_</key>\n" + 
            "            <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">my custom data</value>\n" + 
            "        </entry>\n" + 
            "    </outputData>\n" + 
            "</task-instance>";
    
    public static final String TIMER_VAR_MAP_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<map-type>\n" + 
            "    <entries>\n" + 
            "        <entry>\n" + 
            "            <key>period</key>\n" + 
            "            <value xsi:type=\"xs:long\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">0</value>\n" + 
            "        </entry>\n" + 
            "        <entry>\n" + 
            "            <key>delay</key>\n" + 
            "            <value xsi:type=\"xs:long\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">3</value>\n" + 
            "        </entry>\n" + 
            "        <entry>\n" + 
            "            <key>repeatLimit</key>\n" + 
            "            <value xsi:type=\"xs:int\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">0</value>\n" + 
            "        </entry>\n" + 
            "    </entries>\n" + 
            "</map-type>";
    
    public static final String ORG_ENTITIES_LIST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<org-entities>\n" + 
            "    <users>john</users>\n" + 
            "</org-entities>";
    public static final String EMAIL_NOTIFICATION_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<email-notification>\n" + 
            "    <from>test@jbpm.org</from>\n" + 
            "    <reply-to>no-reply@jbpm.org</reply-to>\n" + 
            "    <users>john</users>\n" + 
            "    <subject>reminder</subject>\n" + 
            "    <body>my test content</body>\n" + 
            "</email-notification>";
    
    
    /*
     * 
     * JSON sample responses
     * 
     */

    public static final String LONG_RESPONSE_JSON = "10";
    
    
    /*
     * 
     * XML sample responses
     * 
     */

    public static final String LONG_RESPONSE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<long-type>\n" + 
            "    <value>10</value>\n" + 
            "</long-type>";
}
