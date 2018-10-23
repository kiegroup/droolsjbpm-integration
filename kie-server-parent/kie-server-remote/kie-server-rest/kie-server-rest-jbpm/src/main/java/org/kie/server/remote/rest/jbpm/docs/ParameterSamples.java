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
