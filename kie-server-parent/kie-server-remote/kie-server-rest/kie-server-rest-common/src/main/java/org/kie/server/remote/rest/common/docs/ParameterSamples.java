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

package org.kie.server.remote.rest.common.docs;


public class ParameterSamples {

    public static final String JSON = "application/json";
    public static final String XML = "application/xml";

    /*
     * 
     * JSON sample payloads
     * 
     */

    public static final String CREATE_CONTAINER_JSON = "{\n" +
            "    \"container-id\" : \"baz\",\n" +
            "    \"release-id\" : {\n" + 
            "        \"group-id\" : \"foo\",\n" +
            "        \"artifact-id\" : \"bar\",\n" +
            "        \"version\" : \"1.0\"\n" +
            "    }\n" +
            "}";

    public static final String UPDATE_RELEASE_ID_JSON = "{\n" +
            "    \"group-id\" : \"foo\",\n" +
            "    \"artifact-id\" : \"bar\",\n" +
            "    \"version\" : \"1.0\"\n" +
            "}";

    public static final String UPDATE_SCANNER_JSON = "{\n" +
            "    \"status\" : \"STARTED\",\n" +
            "    \"poll-interval\" : \"10000\"\n" +
            "}";

    /*
     * 
     * XML (JAXB) sample payloads
     * 
     */

    public static final String CREATE_CONTAINER_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<kie-container container-id=\"baz\">\n" +
            "    <release-id>\n" + 
            "        <group-id>foo</group-id>\n" +
            "        <artifact-id>bar</artifact-id>\n" + 
            "        <version>1.0</version>\n" +
            "    </release-id>\n" +
            "</kie-container>\n";

    public static final String UPDATE_RELEASE_ID_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<release-id>\n" +
            "    <group-id>foo</group-id>\n" + 
            "    <artifact-id>bar</artifact-id>\n" +
            "    <version>1.0</version>\n" +
            "</release-id>\n";

    public static final String UPDATE_SCANNER_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
            "<kie-scanner poll-interval=\"10000\" status=\"STARTED\"/>\n";
}
