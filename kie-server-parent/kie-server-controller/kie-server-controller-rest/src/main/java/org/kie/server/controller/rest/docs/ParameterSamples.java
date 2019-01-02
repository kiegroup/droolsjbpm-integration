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

package org.kie.server.controller.rest.docs;

public class ParameterSamples {

    public static final String JSON = "application/json";
    public static final String XML = "application/xml";

    /*
     *
     * JSON sample payloads
     *
     */
    public static final String CONTAINER_SPEC_LIST_JSON = "{\n" +
            " \"container-spec\" : [ {\n" +
            "       \"container-id\" : \"evaluation_1.0.0-SNAPSHOT\",\n" +
            "       \"container-name\" : \"evaluation\",\n" +
            "       \"server-template-key\" : {\n" +
            "           \"server-id\" : \"test-kie-server\",\n" +
            "           \"server-name\" : \"test-kie-server\"\n" +
            "       },\n" +
            "       \"release-id\" : {\n" +
            "           \"group-id\" : \"evaluation\",\n" +
            "           \"artifact-id\" : \"evaluation\",\n" +
            "           \"version\" : \"1.0.0-SNAPSHOT\"\n" +
            "       },\n" +
            "       \"configuration\" : {\n" +
            "           \"PROCESS\" : {\n" +
            "               \"org.kie.server.controller.api.model.spec.ProcessConfig\" : {\n" +
            "                   \"runtimeStrategy\" : \"SINGLETON\",\n" +
            "                   \"kbase\" : \"\",\n" +
            "                   \"ksession\" : \"\",\n" +
            "                   \"mergeMode\" : \"MERGE_COLLECTIONS\"\n" +
            "               }\n" +
            "           },\n" +
            "           \"RULE\" : {\n" +
            "               \"org.kie.server.controller.api.model.spec.RuleConfig\" : {\n" +
            "                   \"pollInterval\" : null,\n" +
            "                   \"scannerStatus\" : \"STOPPED\"\n" +
            "               }\n" +
            "           }\n" +
            "       },\n" +
            "       \"status\" : \"STARTED\"\n" +
            "   } ]\n" +
            "}";

    public static final String SERVER_TEMPLATE_LIST_JSON = "{\n" +
            "  \"server-template\" : [ {\n" +
            "       \"server-id\" : \"test-kie-server\",\n" +
            "       \"server-name\" : \"test-kie-server\",\n" +
            "       \"container-specs\" : [ {\n" +
            "           \"container-id\" : \"evaluation_1.0.0-SNAPSHOT\",\n" +
            "           \"container-name\" : \"evaluation\",\n" +
            "              \"server-template-key\" : {\n" +
            "                   \"server-id\" : \"test-kie-server\",\n" +
            "                   \"server-name\" : \"test-kie-server\"\n" +
            "               },\n" +
            "           \"release-id\" : {\n" +
            "                   \"group-id\" : \"evaluation\",\n" +
            "                   \"artifact-id\" : \"evaluation\",\n" +
            "                   \"version\" : \"1.0.0-SNAPSHOT\"\n" +
            "           },\n" +
            "           \"configuration\" : {\n" +
            "                   \"PROCESS\" : {\n" +
            "                       \"org.kie.server.controller.api.model.spec.ProcessConfig\" : {\n" +
            "                               \"runtimeStrategy\" : \"SINGLETON\",\n" +
            "                               \"kbase\" : \"\",\n" +
            "                               \"ksession\" : \"\",\n" +
            "                               \"mergeMode\" : \"MERGE_COLLECTIONS\"\n" +
            "                       }\n" +
            "                   },\n" +
            "                   \"RULE\" : {\n" +
            "                       \"org.kie.server.controller.api.model.spec.RuleConfig\" : {\n" +
            "                           \"pollInterval\" : null,\n" +
            "                           \"scannerStatus\" : \"STOPPED\"\n" +
            "                       }\n" +
            "                   }\n" +
            "           },\n" +
            "           \"status\" : \"STARTED\"\n" +
            "       } ],\n" +
            "       \"server-config\" : { },\n" +
            "       \"server-instances\" : [ {\n" +
            "               \"server-instance-id\" : \"test-kie-server@localhost:8080\",\n" +
            "                       \"server-name\" : \"test-kie-server@localhost:8080\",\n" +
            "                       \"server-template-id\" : \"test-kie-server\",\n" +
            "                       \"server-url\" : \"http://localhost:8080/kie-server/services/rest/server\"\n" +
            "               } ],\n" +
            "       \"capabilities\" : [ \"RULE\", \"PROCESS\", \"PLANNING\" ]\n" +
            "   } ]\n" +
            "}";

    public static final String SERVER_TEMPLATE_GET_JSON = "{\n" +
            "       \"server-id\" : \"test-kie-server\",\n" +
            "       \"server-name\" : \"test-kie-server\",\n" +
            "       \"container-specs\" : [ {\n" +
            "           \"container-id\" : \"evaluation_1.0.0-SNAPSHOT\",\n" +
            "           \"container-name\" : \"evaluation\",\n" +
            "              \"server-template-key\" : {\n" +
            "                   \"server-id\" : \"test-kie-server\",\n" +
            "                   \"server-name\" : \"test-kie-server\"\n" +
            "               },\n" +
            "           \"release-id\" : {\n" +
            "                   \"group-id\" : \"evaluation\",\n" +
            "                   \"artifact-id\" : \"evaluation\",\n" +
            "                   \"version\" : \"1.0.0-SNAPSHOT\"\n" +
            "           },\n" +
            "           \"configuration\" : {\n" +
            "                   \"PROCESS\" : {\n" +
            "                       \"org.kie.server.controller.api.model.spec.ProcessConfig\" : {\n" +
            "                               \"runtimeStrategy\" : \"SINGLETON\",\n" +
            "                               \"kbase\" : \"\",\n" +
            "                               \"ksession\" : \"\",\n" +
            "                               \"mergeMode\" : \"MERGE_COLLECTIONS\"\n" +
            "                       }\n" +
            "                   },\n" +
            "                   \"RULE\" : {\n" +
            "                       \"org.kie.server.controller.api.model.spec.RuleConfig\" : {\n" +
            "                           \"pollInterval\" : null,\n" +
            "                           \"scannerStatus\" : \"STOPPED\"\n" +
            "                       }\n" +
            "                   }\n" +
            "           },\n" +
            "           \"status\" : \"STARTED\"\n" +
            "       } ],\n" +
            "       \"server-config\" : { },\n" +
            "       \"server-instances\" : [ {\n" +
            "               \"server-instance-id\" : \"test-kie-server@localhost:8080\",\n" +
            "                       \"server-name\" : \"test-kie-server@localhost:8080\",\n" +
            "                       \"server-template-id\" : \"test-kie-server\",\n" +
            "                       \"server-url\" : \"http://localhost:8080/kie-server/services/rest/server\"\n" +
            "               } ],\n" +
            "       \"capabilities\" : [ \"RULE\", \"PROCESS\", \"PLANNING\" ]\n" +
            "}";

    public static final String SERVER_INSTANCE_GET_JSON = "{\n" +
            "  \"server-instance-key\" : [ {\n" +
            "    \"server-instance-id\" : \"test-kie-server@localhost:8080\",\n" +
            "    \"server-name\" : \"test-kie-server@localhost:8080\",\n" +
            "    \"server-template-id\" : \"test-kie-server\",\n" +
            "    \"server-url\" : \"http://localhost:8080/kie-server/services/rest/server\"\n" +
            "  } ]\n" +
            "}";

    public static final String SERVER_TEMPLATE_SAVE_JSON = "{\n" +
            "  \"type\" : \"SUCCESS\",\n" +
            "  \"msg\" : \"Server template sample-server successfully created\",\n" +
            "  \"result\" : null\n" +
            "}";

    public static final String CONTAINER_CONFIG_JSON = "{\n" +
            "\"org.kie.server.controller.api.model.spec.ProcessConfig\" : {\n" +
            "       \"runtimeStrategy\" : \"SINGLETON\",\n" +
            "       \"kbase\" : null,\n" +
            "       \"ksession\" : null,\n" +
            "       \"mergeMode\" : \"MERGE_COLLECTIONS\"\n" +
            "  }\n" +
            "}";

    public static final String CONTAINER_INSTANCE_LIST_JSON = "{\n" +
            "  \"container-details\" : [ {\n" +
            "    \"sever-template-id\" : \"test-kie-server\",\n" +
            "    \"container-id\" : \"evaluation_1.0.0-SNAPSHOT\",\n" +
            "    \"container-name\" : \"evaluation\",\n" +
            "    \"url\" : \"http://localhost:8080/kie-server/services/rest/server/containers/evaluation_1.0.0-SNAPSHOT\",\n" +
            "    \"sever-instance-id\" : \"test-kie-server@localhost:8080\",\n" +
            "    \"container-release-id\" : {\n" +
            "      \"group-id\" : \"evaluation\",\n" +
            "      \"artifact-id\" : \"evaluation\",\n" +
            "      \"version\" : \"1.0.0-SNAPSHOT\"\n" +
            "    },\n" +
            "    \"messages\" : [ {\n" +
            "      \"severity\" : \"INFO\",\n" +
            "      \"timestamp\" : {\n" +
            "           \"java.util.Date\" : 1541568199713\n" +
            "       },\n" +
            "      \"content\" : [ \"Container evaluation_1.0.0-SNAPSHOT successfully created with module evaluation:evaluation:1.0.0-SNAPSHOT.\" ]\n" +
            "    } ],\n" +
            "    \"status\" : \"STARTED\"\n" +
            "  } ]\n" +
            "}";

    public static final String SERVER_TEMPLATE_JSON = "{\n" +
            "  \"server-id\" : \"sample-server\",\n" +
            "  \"server-name\" : \"sample-server\",\n" +
            "  \"capabilities\" : [\n" +
            "       \"RULE\"," +
            "       \"PROCESS\"," +
            "       \"PLANNING\"" +
            "    ],\n" +
            "  \"container-specs\" : [ ],\n" +
            "  \"server-config\" : { }\n" +
            "}";

    public static final String CONTAINER_SPEC_GET_JSON = "{\n" +
            "  \"container-id\" : \"evaluation_1.0.0-SNAPSHOT\",\n" +
            "  \"container-name\" : \"evaluation\",\n" +
            "  \"server-template-key\" : {\n" +
            "    \"server-id\" : \"test-kie-server\",\n" +
            "    \"server-name\" : \"test-kie-server\"\n" +
            "  },\n" +
            "  \"release-id\" : {\n" +
            "    \"group-id\" : \"evaluation\",\n" +
            "    \"artifact-id\" : \"evaluation\",\n" +
            "    \"version\" : \"1.0.0-SNAPSHOT\"\n" +
            "  },\n" +
            "  \"configuration\" : {\n" +
            "    \"PROCESS\" : {\n" +
            "      \"org.kie.server.controller.api.model.spec.ProcessConfig\" : {\n" +
            "        \"runtimeStrategy\" : \"SINGLETON\",\n" +
            "        \"kbase\" : \"\",\n" +
            "        \"ksession\" : \"\",\n" +
            "        \"mergeMode\" : \"MERGE_COLLECTIONS\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"RULE\" : {\n" +
            "      \"org.kie.server.controller.api.model.spec.RuleConfig\" : {\n" +
            "        \"pollInterval\" : null,\n" +
            "        \"scannerStatus\" : \"STOPPED\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"status\" : \"STARTED\"\n" +
            "}";


    public static final String CONTAINER_SPEC_JSON = "{\n" +
            "  \"container-id\" : \"evaluation_1.0.0-SNAPSHOT\",\n" +
            "  \"container-name\" : \"evaluation\",\n" +
            "  \"server-template-key\" : null,\n" +
            "  \"release-id\" : {\"\n" +
            "    \"group-id\" : \"evaluation\",\n" +
            "    \"artifact-id\" : \"evaluation\",\n" +
            "    \"version\" : \"1.0.0-SNAPSHOT\"\n" +
            "  },\n" +
            "  \"configuration\" : {\n" +
            "    \"RULE\" : {\n" +
            "      \"org.kie.server.controller.api.model.spec.RuleConfig\" : {\n" +
            "        \"pollInterval\" : null,\n" +
            "        \"scannerStatus\" : \"STOPPED\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"PROCESS\" : {\n" +
            "      \"org.kie.server.controller.api.model.spec.ProcessConfig\" : {\n" +
            "        \"runtimeStrategy\" : \"SINGLETON\",\n" +
            "        \"kbase\" : \"\",\n" +
            "        \"ksession\" : \"\",\n" +
            "        \"mergeMode\" : \"MERGE_COLLECTIONS\"\n" +
            "      }\n" +
            "    }\n" +
            "  },\n" +
            "  \"status\" : \"STARTED\"\n" +
            "}";

    /*
     *
     * XML (JAXB) sample payloads
     *
     */
    public static final String SERVER_TEMPLATE_SAVE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<response type=\"SUCCESS\" msg=\"Server template sample-server successfully created\"/>";


    public static final String SERVER_TEMPLATE_LIST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<server-template-list>\n" +
            "    <server-template>\n" +
            "        <server-id>test-kie-server</server-id>\n" +
            "        <server-name>test-kie-server</server-name>\n" +
            "        <container-specs>\n" +
            "            <container-id>evaluation_1.0.0-SNAPSHOT</container-id>\n" +
            "            <container-name>evaluation</container-name>\n" +
            "            <server-template-key>\n" +
            "                <server-id>test-kie-server</server-id>\n" +
            "                <server-name>test-kie-server</server-name>\n" +
            "            </server-template-key>\n" +
            "            <release-id>\n" +
            "                <artifact-id>evaluation</artifact-id>\n" +
            "                <group-id>evaluation</group-id>\n" +
            "                <version>1.0.0-SNAPSHOT</version>\n" +
            "            </release-id>\n" +
            "            <configs>\n" +
            "                <entry>\n" +
            "                    <key>PROCESS</key>\n" +
            "                    <value xsi:type=\"processConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                        <runtimeStrategy>SINGLETON</runtimeStrategy>\n" +
            "                        <kbase></kbase>\n" +
            "                        <ksession></ksession>\n" +
            "                        <mergeMode>MERGE_COLLECTIONS</mergeMode>\n" +
            "                    </value>\n" +
            "                </entry>\n" +
            "                <entry>\n" +
            "                    <key>RULE</key>\n" +
            "                    <value xsi:type=\"ruleConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                        <scannerStatus>STOPPED</scannerStatus>\n" +
            "                    </value>\n" +
            "                </entry>\n" +
            "            </configs>\n" +
            "            <status>STARTED</status>\n" +
            "        </container-specs>\n" +
            "        <configs/>\n" +
            "        <server-instances>\n" +
            "            <server-instance-id>test-kie-server@localhost:8080</server-instance-id>\n" +
            "            <server-name>test-kie-server@localhost:8080</server-name>\n" +
            "            <server-template-id>test-kie-server</server-template-id>\n" +
            "            <server-url>http://localhost:8080/kie-server/services/rest/server</server-url>\n" +
            "        </server-instances>\n" +
            "        <capabilities>RULE</capabilities>\n" +
            "        <capabilities>PROCESS</capabilities>\n" +
            "        <capabilities>PLANNING</capabilities>\n" +
            "    </server-template>\n" +
            "</server-template-list>";

    public static final String CONTAINER_SPEC_LIST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<container-spec-list>\n" +
            "    <container-spec>\n" +
            "        <container-id>evaluation_1.0.0-SNAPSHOT</container-id>\n" +
            "        <container-name>evaluation</container-name>\n" +
            "        <server-template-key>\n" +
            "            <server-id>test-kie-server</server-id>\n" +
            "            <server-name>test-kie-server</server-name>\n" +
            "        </server-template-key>\n" +
            "        <release-id>\n" +
            "            <artifact-id>evaluation</artifact-id>\n" +
            "            <group-id>evaluation</group-id>\n" +
            "            <version>1.0.0-SNAPSHOT</version>\n" +
            "        </release-id>\n" +
            "        <configs>\n" +
            "            <entry>\n" +
            "                <key>PROCESS</key>\n" +
            "                <value xsi:type=\"processConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                    <runtimeStrategy>SINGLETON</runtimeStrategy>\n" +
            "                    <kbase></kbase>\n" +
            "                    <ksession></ksession>\n" +
            "                    <mergeMode>MERGE_COLLECTIONS</mergeMode>\n" +
            "                </value>\n" +
            "            </entry>\n" +
            "            <entry>\n" +
            "                <key>RULE</key>\n" +
            "                <value xsi:type=\"ruleConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                    <scannerStatus>STOPPED</scannerStatus>\n" +
            "                </value>\n" +
            "            </entry>\n" +
            "        </configs>\n" +
            "        <status>STARTED</status>\n" +
            "    </container-spec>\n" +
            "</container-spec-list>";


    public static final String SERVER_TEMPLATE_GET_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<server-template>\n" +
            "   <server-id>test-kie-server</server-id>\n" +
            "   <server-name>test-kie-server</server-name>\n" +
            "   <container-specs>\n" +
            "       <container-id>evaluation_1.0.0-SNAPSHOT</container-id>\n" +
            "       <container-name>evaluation</container-name>\n" +
            "       <server-template-key>\n" +
            "           <server-id>test-kie-server</server-id>\n" +
            "           <server-name>test-kie-server</server-name>\n" +
            "       </server-template-key>\n" +
            "       <release-id>\n" +
            "           <artifact-id>evaluation</artifact-id>\n" +
            "           <group-id>evaluation</group-id>\n" +
            "           <version>1.0.0-SNAPSHOT</version>\n" +
            "       </release-id>\n" +
            "       <configs>\n" +
            "           <entry>\n" +
            "               <key>PROCESS</key>\n" +
            "               <value xsi:type=\"processConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                   <runtimeStrategy>SINGLETON</runtimeStrategy>\n" +
            "                   <kbase></kbase>\n" +
            "                   <ksession></ksession>\n" +
            "                   <mergeMode>MERGE_COLLECTIONS</mergeMode>\n" +
            "               </value>\n" +
            "           </entry>\n" +
            "           <entry>\n" +
            "               <key>RULE</key>\n" +
            "               <value xsi:type=\"ruleConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                   <scannerStatus>STOPPED</scannerStatus>\n" +
            "               </value>\n" +
            "           </entry>\n" +
            "       </configs>\n" +
            "       <status>STARTED</status>\n" +
            "   </container-specs>\n" +
            "   <configs/>\n" +
            "   <server-instances>\n" +
            "       <server-instance-id>test-kie-server@localhost:8080</server-instance-id>\n" +
            "       <server-name>test-kie-server@localhost:8080</server-name>\n" +
            "       <server-template-id>test-kie-server</server-template-id>\n" +
            "       <server-url>http://localhost:8080/kie-server/services/rest/server</server-url>\n" +
            "   </server-instances>\n" +
            "   <capabilities>RULE</capabilities>\n" +
            "   <capabilities>PROCESS</capabilities>\n" +
            "   <capabilities>PLANNING</capabilities>\n" +
            "</server-template>";

    public static final String CONTAINER_CONFIG_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<process-config>\n" +
            "    <runtimeStrategy>SINGLETON</runtimeStrategy>\n" +
            "    <mergeMode>MERGE_COLLECTIONS</mergeMode>\n" +
            "</process-config>\n";

    public static final String SERVER_TEMPLATE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<server-template-details>\n" +
            "  <server-id>sample-server</server-id>\n" +
            "  <server-name>sample-server</server-name>\n" +
            "  <configs/>\n" +
            "  <capabilities>RULE</capabilities>\n" +
            "  <capabilities>PROCESS</capabilities>\n" +
            "  <capabilities>PLANNING</capabilities>\n" +
            "</server-template-details>";

    public static final String CONTAINER_SPEC_GET_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<container-spec-details>\n" +
            "    <container-id>evaluation_1.0.0-SNAPSHOT</container-id>\n" +
            "    <container-name>evaluation</container-name>\n" +
            "    <server-template-key>\n" +
            "        <server-id>test-kie-server</server-id>\n" +
            "        <server-name>test-kie-server</server-name>\n" +
            "    </server-template-key>\n" +
            "    <release-id>\n" +
            "        <artifact-id>evaluation</artifact-id>\n" +
            "        <group-id>evaluation</group-id>\n" +
            "        <version>1.0.0-SNAPSHOT</version>\n" +
            "    </release-id>\n" +
            "    <configs>\n" +
            "        <entry>\n" +
            "            <key>PROCESS</key>\n" +
            "            <value xsi:type=\"processConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                <runtimeStrategy>SINGLETON</runtimeStrategy>\n" +
            "                <kbase></kbase>\n" +
            "                <ksession></ksession>\n" +
            "                <mergeMode>MERGE_COLLECTIONS</mergeMode>\n" +
            "            </value>\n" +
            "        </entry>\n" +
            "        <entry>\n" +
            "            <key>RULE</key>\n" +
            "            <value xsi:type=\"ruleConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                <scannerStatus>STOPPED</scannerStatus>\n" +
            "            </value>\n" +
            "        </entry>\n" +
            "    </configs>\n" +
            "    <status>STARTED</status>\n" +
            "</container-spec-details>";

    public static final String CONTAINER_SPEC_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<container-spec-details>\n" +
            "    <container-id>evaluation_1.0.0-SNAPSHOT</container-id>\n" +
            "    <container-name>evaluation</container-name>\n" +
            "    <release-id>\n" +
            "        <artifact-id>evaluation</artifact-id>\n" +
            "        <group-id>evaluation</group-id>\n" +
            "        <version>1.0.0-SNAPSHOT</version>\n" +
            "    </release-id>\n" +
            "    <configs>\n" +
            "        <entry>\n" +
            "            <key>RULE</key>\n" +
            "            <value xsi:type=\"ruleConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                <scannerStatus>STOPPED</scannerStatus>\n" +
            "            </value>\n" +
            "        </entry>\n" +
            "        <entry>\n" +
            "            <key>PROCESS</key>\n" +
            "            <value xsi:type=\"processConfig\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                <runtimeStrategy>SINGLETON</runtimeStrategy>\n" +
            "                <kbase></kbase>\n" +
            "                <ksession></ksession>\n" +
            "                <mergeMode>MERGE_COLLECTIONS</mergeMode>\n" +
            "            </value>\n" +
            "        </entry>\n" +
            "    </configs>\n" +
            "    <status>STARTED</status>\n" +
            "</container-spec-details>";

    public static final String SERVER_INSTANCE_GET_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<server-instance-key-list>\n" +
            "    <server-instance-key>\n" +
            "        <server-instance-id>test-kie-server@localhost:8080</server-instance-id>\n" +
            "        <server-name>test-kie-server@localhost:8080</server-name>\n" +
            "        <server-template-id>test-kie-server</server-template-id>\n" +
            "        <server-url>http://localhost:8080/kie-server/services/rest/server</server-url>\n" +
            "    </server-instance-key>\n" +
            "</server-instance-key-list>";

    public static final String CONTAINER_INSTANCE_LIST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<container-details-list>\n" +
            "    <container-details>\n" +
            "        <sever-template-id>test-kie-server</sever-template-id>\n" +
            "        <container-id>evaluation_1.0.0-SNAPSHOT</container-id>\n" +
            "        <container-name>evaluation</container-name>\n" +
            "        <url>http://localhost:8080/kie-server/services/rest/server/containers/evaluation_1.0.0-SNAPSHOT</url>\n" +
            "        <sever-instance-id>test-kie-server@localhost:8080</sever-instance-id>\n" +
            "        <container-release-id>\n" +
            "            <artifact-id>evaluation</artifact-id>\n" +
            "            <group-id>evaluation</group-id>\n" +
            "            <version>1.0.0-SNAPSHOT</version>\n" +
            "        </container-release-id>\n" +
            "        <messages>\n" +
            "            <content>Container evaluation_1.0.0-SNAPSHOT successfully created with module evaluation:evaluation:1.0.0-SNAPSHOT.</content>\n" +
            "            <severity>INFO</severity>\n" +
            "            <timestamp>2018-11-07T05:23:19.713Z</timestamp>\n" +
            "        </messages>\n" +
            "        <status>STARTED</status>\n" +
            "    </container-details>\n" +
            "</container-details-list>";
}
