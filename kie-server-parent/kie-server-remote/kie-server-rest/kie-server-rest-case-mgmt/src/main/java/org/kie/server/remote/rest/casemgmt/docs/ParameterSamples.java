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

package org.kie.server.remote.rest.casemgmt.docs;


public class ParameterSamples {
    
    public static final String JSON = "application/json";
    public static final String XML = "application/xml";
    
    /*
     * 
     * JSON sample payloads
     * 
     */

    public static final String CASE_MIGRATION_MAP_JSON = "{\n" + 
            "  \"NodeMapping\" : { },\n" + 
            "  \"ProcessMapping\" : {\n" + 
            "    \"insurance-claims.CarInsuranceClaimCase\" : \"insurance-claims.CarInsuranceClaimCase2\"\n" + 
            "  }\n" + 
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
    
    public static final String CASE_FILE_JSON = "{\n" + 
            "  \"case-data\" : {\n" + 
            "    \"car\" : \"ford\"\n" + 
            "  },\n" + 
            "  \"case-user-assignments\" : {\n" + 
            "    \"insured\" : \"yoda\",\n" + 
            "    \"insuranceRepresentative\" : \"john\"\n" + 
            "  },\n" + 
            "  \"case-group-assignments\" : { },\n" + 
            "  \"case-data-restrictions\" : { }\n" + 
            "}";
    
    public static final String CASE_INSTANCES_JSON = "{\n" + 
            "  \"instances\": [\n" + 
            "    {\n" + 
            "      \"case-id\": \"IT-0000000006\",\n" + 
            "      \"case-description\": \"Order for IT hardware\",\n" + 
            "      \"case-owner\": \"baAdmin\",\n" + 
            "      \"case-status\": 1,\n" + 
            "      \"case-definition-id\": \"itorders.orderhardware\",\n" + 
            "      \"container-id\": \"itorders_1.0.0-SNAPSHOT\",\n" + 
            "      \"case-started-at\": 1540474204152,\n" + 
            "      \"case-completed-at\": null,\n" + 
            "      \"case-completion-msg\": \"\",\n" + 
            "      \"case-sla-compliance\": 0,\n" + 
            "      \"case-sla-due-date\": null,\n" + 
            "      \"case-file\": null,\n" + 
            "      \"case-milestones\": null,\n" + 
            "      \"case-stages\": null,\n" + 
            "      \"case-roles\": null\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"case-id\": \"IT-0000000011\",\n" + 
            "      \"case-description\": \"Order for IT hardware\",\n" + 
            "      \"case-owner\": \"baAdmin\",\n" + 
            "      \"case-status\": 1,\n" + 
            "      \"case-definition-id\": \"itorders.orderhardware\",\n" + 
            "      \"container-id\": \"itorders_1.0.0-SNAPSHOT6\",\n" + 
            "      \"case-started-at\": 1540496734746,\n" + 
            "      \"case-completed-at\": null,\n" + 
            "      \"case-completion-msg\": \"\",\n" + 
            "      \"case-sla-compliance\": 0,\n" + 
            "      \"case-sla-due-date\": null,\n" + 
            "      \"case-file\": null,\n" + 
            "      \"case-milestones\": null,\n" + 
            "      \"case-stages\": null,\n" + 
            "      \"case-roles\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String CASE_MIGRATION_REPORT_JSON = "{\n" + 
            "  \"case-id\": \"IT-0000000006\",\n" + 
            "  \"case-migration-successful\": true,\n" + 
            "  \"case-migration-start\": {\n" + 
            "    \"java.util.Date\": 1540526940760\n" + 
            "  },\n" + 
            "  \"case-migration-end\": {\n" + 
            "    \"java.util.Date\": 1540526940867\n" + 
            "  },\n" + 
            "  \"case-migration-reports\": [\n" + 
            "    {\n" + 
            "      \"migration-successful\": true,\n" + 
            "      \"migration-start\": {\n" + 
            "        \"java.util.Date\": 1540526940760\n" + 
            "      },\n" + 
            "      \"migration-end\": {\n" + 
            "        \"java.util.Date\": 1540526940863\n" + 
            "      },\n" + 
            "      \"migration-logs\": [\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Variable instances updated = 2 for process instance id 27\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Node instances updated = 4 for process instance id 27\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Process instances updated = 1 for process instance id 27\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Task variables updated = 6 for process instance id 27\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Task audit updated = 1 for process instance id 27\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Tasks updated = 1 for process instance id 27\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Context info updated = 1 for process instance id 27\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Node instance logs to be updated  = [0]\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Node instance logs updated = 1 for node instance id 0\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Task audit updated = 1 for task id 33\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Task updated = 1 for task id 33\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Node instance logs to be updated  = [1]\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Node instance logs updated = 1 for node instance id 1\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Node instance logs to be updated  = [2]\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Node instance logs updated = 1 for node instance id 2\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Node instance logs to be updated  = [3]\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Mapping: Node instance logs updated = 1 for node instance id 3\",\n" + 
            "        \"INFO Fri Oct 26 00:09:00 EDT 2018 Migration of process instance (27) completed successfully to process itorders.orderhardware2\"\n" + 
            "      ],\n" + 
            "      \"migration-process-instance\": 27\n" + 
            "    }\n" + 
            "  ]\n" + 
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
    
    public static final String GET_CASE_FILE_DATA_RESPONSE_JSON = "{\n" + 
            "  \"instances\": [\n" + 
            "    {\n" + 
            "      \"case-id\": \"IT-0000000006\",\n" + 
            "      \"name\": \"hwSpecNew\",\n" + 
            "      \"value\": \"New content for hardware specification.\",\n" + 
            "      \"type\": \"java.lang.String\",\n" + 
            "      \"last-modified-by\": \"baAdmin\",\n" + 
            "      \"last-modified\": 1540502077279\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"case-id\": \"IT-0000000006\",\n" + 
            "      \"name\": \"milestone-mandatory\",\n" + 
            "      \"value\": \"false\",\n" + 
            "      \"type\": \"java.lang.Boolean\",\n" + 
            "      \"last-modified-by\": \"baAdmin\",\n" + 
            "      \"last-modified\": 1540499389943\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    public static final String CASE_DEFINITIONS_JSON = "{\n" + 
            "  \"definitions\": [\n" + 
            "    {\n" + 
            "      \"name\": \"Order for IT hardware\",\n" + 
            "      \"id\": \"itorders.orderhardware\",\n" + 
            "      \"version\": \"1.0\",\n" + 
            "      \"case-id-prefix\": \"IT\",\n" + 
            "      \"container-id\": \"itorders_1.0.0-SNAPSHOT2\",\n" + 
            "      \"adhoc-fragments\": [\n" + 
            "        {\n" + 
            "          \"name\": \"Prepare hardware spec\",\n" + 
            "          \"type\": \"HumanTaskNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Milestone 1: Order placed\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Milestone 2: Order shipped\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Milestone 3: Delivered to customer\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Hardware spec ready\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Manager decision\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        }\n" + 
            "      ],\n" + 
            "      \"roles\": {\n" + 
            "        \"owner\": 1,\n" + 
            "        \"manager\": 1,\n" + 
            "        \"supplier\": 2\n" + 
            "      },\n" + 
            "      \"milestones\": [\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Milestone 1: Order placed\",\n" + 
            "          \"milestone-id\": \"_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Milestone 2: Order shipped\",\n" + 
            "          \"milestone-id\": \"_343B90CD-AA19-4894-B63C-3CE1906E6FD1\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Milestone 3: Delivered to customer\",\n" + 
            "          \"milestone-id\": \"_52AFA23F-C087-4519-B8F2-BABCC31D68A6\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Hardware spec ready\",\n" + 
            "          \"milestone-id\": \"_483CF785-96DD-40C1-9148-4CFAFAE5778A\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Manager decision\",\n" + 
            "          \"milestone-id\": \"_79953D58-25DB-4FD6-94A0-DFC6EA2D0339\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        }\n" + 
            "      ],\n" + 
            "      \"stages\": []\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Order for IT hardware\",\n" + 
            "      \"id\": \"itorders.orderhardware\",\n" + 
            "      \"version\": \"1.0\",\n" + 
            "      \"case-id-prefix\": \"IT\",\n" + 
            "      \"container-id\": \"itorders_1.0.0-SNAPSHOT3\",\n" + 
            "      \"adhoc-fragments\": [\n" + 
            "        {\n" + 
            "          \"name\": \"Prepare hardware spec\",\n" + 
            "          \"type\": \"HumanTaskNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Milestone 1: Order placed\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Milestone 2: Order shipped\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Milestone 3: Delivered to customer\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Hardware spec ready\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"name\": \"Manager decision\",\n" + 
            "          \"type\": \"MilestoneNode\"\n" + 
            "        }\n" + 
            "      ],\n" + 
            "      \"roles\": {\n" + 
            "        \"owner\": 1,\n" + 
            "        \"manager\": 1,\n" + 
            "        \"supplier\": 2\n" + 
            "      },\n" + 
            "      \"milestones\": [\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Milestone 1: Order placed\",\n" + 
            "          \"milestone-id\": \"_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Milestone 2: Order shipped\",\n" + 
            "          \"milestone-id\": \"_343B90CD-AA19-4894-B63C-3CE1906E6FD1\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Milestone 3: Delivered to customer\",\n" + 
            "          \"milestone-id\": \"_52AFA23F-C087-4519-B8F2-BABCC31D68A6\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Hardware spec ready\",\n" + 
            "          \"milestone-id\": \"_483CF785-96DD-40C1-9148-4CFAFAE5778A\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        },\n" + 
            "        {\n" + 
            "          \"milestone-name\": \"Manager decision\",\n" + 
            "          \"milestone-id\": \"_79953D58-25DB-4FD6-94A0-DFC6EA2D0339\",\n" + 
            "          \"milestone-mandatory\": false\n" + 
            "        }\n" + 
            "      ],\n" + 
            "      \"stages\": []\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    public static final String CASE_ID_JSON = "\"CASE-0000000012\"";
    
    public static final String CASE_INSTANCE_JSON = "{\n" + 
            "  \"case-id\": \"IT-0000000006\",\n" + 
            "  \"case-description\": \"Order for IT hardware\",\n" + 
            "  \"case-owner\": \"baAdmin\",\n" + 
            "  \"case-status\": 1,\n" + 
            "  \"case-definition-id\": \"itorders.orderhardware\",\n" + 
            "  \"container-id\": \"itorders_1.0.0-SNAPSHOT\",\n" + 
            "  \"case-started-at\": 1540474204152,\n" + 
            "  \"case-completed-at\": null,\n" + 
            "  \"case-completion-msg\": \"\",\n" + 
            "  \"case-sla-compliance\": 0,\n" + 
            "  \"case-sla-due-date\": null,\n" + 
            "  \"case-file\": {\n" + 
            "    \"case-data\": {},\n" + 
            "    \"case-user-assignments\": {},\n" + 
            "    \"case-group-assignments\": {},\n" + 
            "    \"case-data-restrictions\": {}\n" + 
            "  },\n" + 
            "  \"case-milestones\": null,\n" + 
            "  \"case-stages\": null,\n" + 
            "  \"case-roles\": [\n" + 
            "    {\n" + 
            "      \"name\": \"owner\",\n" + 
            "      \"users\": [\n" + 
            "        \"baAdmin\"\n" + 
            "      ],\n" + 
            "      \"groups\": []\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"manager\",\n" + 
            "      \"users\": [\n" + 
            "        \"baAdmin\"\n" + 
            "      ],\n" + 
            "      \"groups\": []\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"supplier\",\n" + 
            "      \"users\": [],\n" + 
            "      \"groups\": [\n" + 
            "        \"IT\"\n" + 
            "      ]\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String CASE_MILESTONES_JSON = "{\n" + 
            "  \"milestones\": [\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Milestone 1: Order placed\",\n" + 
            "      \"milestone-id\": \"1\",\n" + 
            "      \"milestone-achieved\": false,\n" + 
            "      \"milestone-achieved-at\": null,\n" + 
            "      \"milestone-status\": \"Available\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Hardware spec ready\",\n" + 
            "      \"milestone-id\": \"2\",\n" + 
            "      \"milestone-achieved\": false,\n" + 
            "      \"milestone-achieved-at\": null,\n" + 
            "      \"milestone-status\": \"Available\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Manager decision\",\n" + 
            "      \"milestone-id\": \"3\",\n" + 
            "      \"milestone-achieved\": false,\n" + 
            "      \"milestone-achieved-at\": null,\n" + 
            "      \"milestone-status\": \"Available\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Milestone 2: Order shipped\",\n" + 
            "      \"milestone-id\": \"_343B90CD-AA19-4894-B63C-3CE1906E6FD1\",\n" + 
            "      \"milestone-achieved\": false,\n" + 
            "      \"milestone-achieved-at\": null,\n" + 
            "      \"milestone-status\": \"Available\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Milestone 3: Delivered to customer\",\n" + 
            "      \"milestone-id\": \"_52AFA23F-C087-4519-B8F2-BABCC31D68A6\",\n" + 
            "      \"milestone-achieved\": false,\n" + 
            "      \"milestone-achieved-at\": null,\n" + 
            "      \"milestone-status\": \"Available\"\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String CASE_STAGES_JSON = "{\n" + 
            "  \"stages\": [\n" + 
            "    {\n" + 
            "      \"stage-name\": \"string\",\n" + 
            "      \"stage-id\": \"string\",\n" + 
            "      \"stage-status\": \"string\",\n" + 
            "      \"adhoc-fragments\": [\n" + 
            "        {\n" + 
            "          \"name\": \"string\",\n" + 
            "          \"type\": \"string\"\n" + 
            "        }\n" + 
            "      ],\n" + 
            "      \"active-nodes\": [\n" + 
            "        {\n" + 
            "          \"node-instance-id\": 0,\n" + 
            "          \"node-name\": \"string\",\n" + 
            "          \"process-instance-id\": 0,\n" + 
            "          \"work-item-id\": 0,\n" + 
            "          \"container-id\": \"string\",\n" + 
            "          \"start-date\": \"2018-10-25T18:34:44.456Z\",\n" + 
            "          \"node-id\": \"string\",\n" + 
            "          \"node-type\": \"string\",\n" + 
            "          \"node-connection\": \"string\",\n" + 
            "          \"node-completed\": true,\n" + 
            "          \"reference-id\": 0,\n" + 
            "          \"sla-compliance\": 0,\n" + 
            "          \"sla-due-date\": \"2018-10-25T18:34:44.456Z\"\n" + 
            "        }\n" + 
            "      ]\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String CASE_ADHOC_FRAGMENTS_JSON = "{\n" + 
            "  \"fragments\": [\n" + 
            "    {\n" + 
            "      \"name\": \"Prepare hardware spec\",\n" + 
            "      \"type\": \"HumanTaskNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Milestone 1: Order placed\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Milestone 2: Order shipped\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Milestone 3: Delivered to customer\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Hardware spec ready\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Manager decision\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String PROCESS_INSTANCES_JSON = "{\n" + 
            "  \"process-instance\": [\n" + 
            "    {\n" + 
            "      \"process-instance-id\": 26,\n" + 
            "      \"process-id\": \"itorders.orderhardware\",\n" + 
            "      \"process-name\": \"Order for IT hardware\",\n" + 
            "      \"process-version\": \"1.0\",\n" + 
            "      \"process-instance-state\": 1,\n" + 
            "      \"container-id\": \"itorders_1.0.0-SNAPSHOT\",\n" + 
            "      \"initiator\": \"baAdmin\",\n" + 
            "      \"start-date\": {\n" + 
            "        \"java.util.Date\": 1540474204152\n" + 
            "      },\n" + 
            "      \"process-instance-desc\": \"Order for IT hardware\",\n" + 
            "      \"correlation-key\": \"IT-0000000006\",\n" + 
            "      \"parent-instance-id\": -1,\n" + 
            "      \"sla-compliance\": 0,\n" + 
            "      \"sla-due-date\": null,\n" + 
            "      \"active-user-tasks\": null,\n" + 
            "      \"process-instance-variables\": null\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"process-instance-id\": 34,\n" + 
            "      \"process-id\": \"itorders-data.place-order\",\n" + 
            "      \"process-name\": \"place-order\",\n" + 
            "      \"process-version\": \"1.0\",\n" + 
            "      \"process-instance-state\": 1,\n" + 
            "      \"container-id\": \"itorders_1.0.0-SNAPSHOT\",\n" + 
            "      \"initiator\": \"baAdmin\",\n" + 
            "      \"start-date\": {\n" + 
            "        \"java.util.Date\": 1540504523492\n" + 
            "      },\n" + 
            "      \"process-instance-desc\": \"Order IT-0000000006\",\n" + 
            "      \"correlation-key\": \"IT-0000000006:itorders-data.place-order:1540504523491\",\n" + 
            "      \"parent-instance-id\": 26,\n" + 
            "      \"sla-compliance\": 0,\n" + 
            "      \"sla-due-date\": null,\n" + 
            "      \"active-user-tasks\": null,\n" + 
            "      \"process-instance-variables\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String NODE_INSTANCES_JSON = "{\n" + 
            "  \"node-instance\": [\n" + 
            "    {\n" + 
            "      \"node-instance-id\": 0,\n" + 
            "      \"node-name\": \"Prepare hardware spec\",\n" + 
            "      \"process-instance-id\": 26,\n" + 
            "      \"work-item-id\": 35,\n" + 
            "      \"container-id\": \"itorders_1.0.0-SNAPSHOT\",\n" + 
            "      \"start-date\": {\n" + 
            "        \"java.util.Date\": 1540474204152\n" + 
            "      },\n" + 
            "      \"node-id\": \"_BFA6002D-0917-42CE-81AD-2A15EC814684\",\n" + 
            "      \"node-type\": \"HumanTaskNode\",\n" + 
            "      \"node-connection\": null,\n" + 
            "      \"node-completed\": false,\n" + 
            "      \"reference-id\": null,\n" + 
            "      \"sla-compliance\": 0,\n" + 
            "      \"sla-due-date\": null\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"node-instance-id\": 1,\n" + 
            "      \"node-name\": \"Milestone 1: Order placed\",\n" + 
            "      \"process-instance-id\": 26,\n" + 
            "      \"work-item-id\": null,\n" + 
            "      \"container-id\": \"itorders_1.0.0-SNAPSHOT\",\n" + 
            "      \"start-date\": {\n" + 
            "        \"java.util.Date\": 1540474204156\n" + 
            "      },\n" + 
            "      \"node-id\": \"_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED\",\n" + 
            "      \"node-type\": \"MilestoneNode\",\n" + 
            "      \"node-connection\": null,\n" + 
            "      \"node-completed\": false,\n" + 
            "      \"reference-id\": null,\n" + 
            "      \"sla-compliance\": 0,\n" + 
            "      \"sla-due-date\": null\n" + 
            "    },\n" +
            "    {\n" + 
            "      \"node-instance-id\": 1,\n" + 
            "      \"node-name\": \"Place order\",\n" + 
            "      \"process-instance-id\": 34,\n" + 
            "      \"work-item-id\": 46,\n" + 
            "      \"container-id\": \"itorders_1.0.0-SNAPSHOT\",\n" + 
            "      \"start-date\": {\n" + 
            "        \"java.util.Date\": 1540504523493\n" + 
            "      },\n" + 
            "      \"node-id\": \"_FCADC388-8A68-40E1-94A7-4F8A7D2B21C2\",\n" + 
            "      \"node-type\": \"HumanTaskNode\",\n" + 
            "      \"node-connection\": \"_384EFF84-BB14-4917-89C3-32DF4ACA1878\",\n" + 
            "      \"node-completed\": false,\n" + 
            "      \"reference-id\": null,\n" + 
            "      \"sla-compliance\": 0,\n" + 
            "      \"sla-due-date\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String CASE_ROLES_ASSIGNMENTS_JSON = "{\n" + 
            "  \"role-assignments\": [\n" + 
            "    {\n" + 
            "      \"name\": \"owner\",\n" + 
            "      \"users\": [\n" + 
            "        \"baAdmin\"\n" + 
            "      ],\n" + 
            "      \"groups\": []\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"manager\",\n" + 
            "      \"users\": [\n" + 
            "        \"baAdmin\"\n" + 
            "      ],\n" + 
            "      \"groups\": []\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"supplier\",\n" + 
            "      \"users\": [],\n" + 
            "      \"groups\": [\n" + 
            "        \"IT\"\n" + 
            "      ]\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String CASE_COMMENTS_JSON = "{\n" + 
            "  \"comments\": [\n" + 
            "    {\n" + 
            "      \"id\": \"f15419b6-1967-479b-8509-066f579c59e1\",\n" + 
            "      \"author\": \"baAdmin\",\n" + 
            "      \"text\": \"Updated case with new ad hoc fragment.\",\n" + 
            "      \"added-at\": {\n" + 
            "        \"java.util.Date\": 1540494739762\n" + 
            "      },\n" + 
            "      \"restricted-to\": [\n" + 
            "        \"owner\"\n" + 
            "      ]\n" + 
            "    }\n" + 
            "      {\n" + 
            "        \"id\": \"2b072823-7448-4819-9560-01165bc7e805\",\n" + 
            "        \"author\": \"baAdmin\",\n" + 
            "        \"text\": \"Removed hardware preparation requirement.\",\n" + 
            "        \"added-at\": {\n" + 
            "          \"java.util.Date\": 1540494802456\n" + 
            "        },\n" + 
            "        \"restricted-to\": [\n" + 
            "          \"supplier\"\n" + 
            "        ]\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String CASE_DEFINITION_JSON = "{\n" + 
            "  \"name\": \"Order for IT hardware\",\n" + 
            "  \"id\": \"itorders.orderhardware\",\n" + 
            "  \"version\": \"1.0\",\n" + 
            "  \"case-id-prefix\": \"IT\",\n" + 
            "  \"container-id\": \"itorders_1.0.0-SNAPSHOT\",\n" + 
            "  \"adhoc-fragments\": [\n" + 
            "    {\n" + 
            "      \"name\": \"Prepare hardware spec\",\n" + 
            "      \"type\": \"HumanTaskNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Milestone 1: Order placed\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Milestone 2: Order shipped\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Milestone 3: Delivered to customer\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Hardware spec ready\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"name\": \"Manager decision\",\n" + 
            "      \"type\": \"MilestoneNode\"\n" + 
            "    }\n" + 
            "  ],\n" + 
            "  \"roles\": {\n" + 
            "    \"owner\": 1,\n" + 
            "    \"manager\": 1,\n" + 
            "    \"supplier\": 2\n" + 
            "  },\n" + 
            "  \"milestones\": [\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Milestone 1: Order placed\",\n" + 
            "      \"milestone-id\": \"_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED\",\n" + 
            "      \"milestone-mandatory\": false\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Milestone 2: Order shipped\",\n" + 
            "      \"milestone-id\": \"_343B90CD-AA19-4894-B63C-3CE1906E6FD1\",\n" + 
            "      \"milestone-mandatory\": false\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Milestone 3: Delivered to customer\",\n" + 
            "      \"milestone-id\": \"_52AFA23F-C087-4519-B8F2-BABCC31D68A6\",\n" + 
            "      \"milestone-mandatory\": false\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Hardware spec ready\",\n" + 
            "      \"milestone-id\": \"_483CF785-96DD-40C1-9148-4CFAFAE5778A\",\n" + 
            "      \"milestone-mandatory\": false\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"milestone-name\": \"Manager decision\",\n" + 
            "      \"milestone-id\": \"_79953D58-25DB-4FD6-94A0-DFC6EA2D0339\",\n" + 
            "      \"milestone-mandatory\": false\n" + 
            "    }\n" + 
            "  ],\n" + 
            "  \"stages\": []\n" + 
            "}";
    /*
     * 
     * XML (JAXB) sample payloads
     * 
     */
    
    
    public static final String CASE_MIGRATION_MAP_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<map-type>\n" + 
            "    <entries>\n" + 
            "        <entry>\n" + 
            "            <key>ProcessMapping</key>\n" + 
            "            <value xsi:type=\"jaxbMap\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
            "                <entries>\n" + 
            "                    <entry>\n" + 
            "                        <key>insurance-claims.CarInsuranceClaimCase</key>\n" + 
            "                        <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">insurance-claims.CarInsuranceClaimCase2</value>\n" + 
            "                    </entry>\n" + 
            "                </entries>\n" + 
            "            </value>\n" + 
            "        </entry>\n" + 
            "        <entry>\n" + 
            "            <key>NodeMapping</key>\n" + 
            "            <value xsi:type=\"jaxbMap\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
            "                <entries/>\n" + 
            "            </value>\n" + 
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
    
    public static final String CASE_FILE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<case-file>\n" + 
            "    <data>\n" + 
            "        <entry>\n" + 
            "            <key>car</key>\n" + 
            "            <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">ford</value>\n" + 
            "        </entry>\n" + 
            "    </data>\n" + 
            "    <userAssignments>\n" + 
            "        <entry>\n" + 
            "            <key>insured</key>\n" + 
            "            <value>yoda</value>\n" + 
            "        </entry>\n" + 
            "        <entry>\n" + 
            "            <key>insuranceRepresentative</key>\n" + 
            "            <value>john</value>\n" + 
            "        </entry>\n" + 
            "    </userAssignments>\n" + 
            "    <groupAssignments/>\n" + 
            "    <accessRestrictions/>\n" + 
            "</case-file>";
 
}
