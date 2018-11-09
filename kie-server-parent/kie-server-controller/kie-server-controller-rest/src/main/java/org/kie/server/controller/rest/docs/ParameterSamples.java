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
    public static final String KIE_SERVER_INFO_JSON = "{\n" +
            "  \"id\" : \"sample-server\",\n" +
            "  \"version\" : \"7.15.0-SNAPSHOT\",\n" +
            "  \"name\" : \"sample-server\",\n" +
            "  \"location\" : \"http://localhost:8080/kie-server/services/rest/server\",\n" +
            "  \"capabilities\" : [\n" +
            "       \"KieServer\"," +
            "       \"BRM\"," +
            "       \"BPM\"," +
            "       \"CaseMgmt\"," +
            "       \"BPM-UI\"," +
            "       \"BRP\"," +
            "       \"DMN\"," +
            "       \"Swagger\"" +
            "    ],\n" +
            "  \"messages\" : null\n" +
            "}";

    public static final String CONTAINER_CONFIG_JSON = "{\n" +
            "\"org.kie.server.controller.api.model.spec.ProcessConfig\" : {\n" +
            "       \"runtimeStrategy\" : \"SINGLETON\",\n" +
            "       \"kbase\" : null,\n" +
            "       \"ksession\" : null,\n" +
            "       \"mergeMode\" : \"MERGE_COLLECTIONS\"\n" +
            "  }\n" +
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

    public static final String CONTAINER_SPEC_JSON = "{\n" +
            "  \"container-id\" : \"evaluation_1.0.0-SNAPSHOT\",\n" +
            "  \"container-name\" : \"evaluation\",\n" +
            "  \"release-id\" : {\n" +
            "       \"group-id\" : \"evaluation\",\n" +
            "        \"artifact-id\" : \"evaluation\",\n" +
            "        \"version\" : \"1.0.0-SNAPSHOT\"\n" +
            "    },\n" +
            "  \"configuration\" : null,\n" +
            "  \"status\" : \"STARTED\"\n" +
            "}";

    /*
     *
     * XML (JAXB) sample payloads
     *
     */
    public static final String KIE_SERVER_INFO_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<kie-server-info>\n" +
            "  <id>sample-server</id>\n" +
            "  <version>7.15.0-SNAPSHOT</version>\n" +
            "  <name>sample-server</name>\n" +
            "  <location>http://localhost:8080/kie-server/services/rest/server</location>\n" +
            "  <capabilities>KieServer</capabilities>\n" +
            "  <capabilities>BRM</capabilities>\n" +
            "  <capabilities>BPM</capabilities>\n" +
            "  <capabilities>CaseMgmt</capabilities>\n" +
            "  <capabilities>BPM-UI</capabilities>\n" +
            "  <capabilities>BRP</capabilities>\n" +
            "  <capabilities>DMN</capabilities>\n" +
            "  <capabilities>Swagger</capabilities>\n" +
            "</kie-server-info>";

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

    public static final String CONTAINER_SPEC_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<container-spec-details>\n" +
            "    <container-id>evaluation_1.0.0-SNAPSHOT</container-id>\n" +
            "    <container-name>evaluation</container-name>\n" +
            "    <release-id>\n" +
            "        <artifact-id>evaluation</artifact-id>\n" +
            "        <group-id>evaluation</group-id>\n" +
            "        <version>1.0.0-SNAPSHOT</version>\n" +
            "    </release-id>\n" +
            "    <status>STARTED</status>\n" +
            "</container-spec-details>";
}
