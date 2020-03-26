/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.remote.rest.taskassigning.runtime.docs;

/**
 * Example values for the TaskAssigning API.
 */
public class ParameterSamples {

    private ParameterSamples() {
    }

    public static final String TASKS_QUERY_PARAMS_MAP_EXAMPLE_JSON = "{\n" +
            "  \"fromTaskId\" : 0,\n" +
            "  \"page\" : 0,\n" +
            "  \"pageSize\" : 30,\n" +
            "  \"status\" : [ \"Reserved\", \"InProgress\", \"Suspended\" ]\n" +
            "}";

    public static final String TASKS_QUERY_PARAMS_MAP_EXAMPLE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<map-type>\n" +
            "    <entries>\n" +
            "        <entry>\n" +
            "            <key>fromTaskId</key>\n" +
            "            <value xsi:type=\"xs:long\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">0</value>\n" +
            "        </entry>\n" +
            "        <entry>\n" +
            "            <key>page</key>\n" +
            "            <value xsi:type=\"xs:int\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">0</value>\n" +
            "        </entry>\n" +
            "        <entry>\n" +
            "            <key>pageSize</key>\n" +
            "            <value xsi:type=\"xs:int\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">30</value>\n" +
            "        </entry>\n" +
            "        <entry>\n" +
            "            <key>status</key>\n" +
            "            <value xsi:type=\"jaxbList\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
            "                <items>\n" +
            "                    <items xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">Reserved</items>\n" +
            "                    <items xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">InProgress</items>\n" +
            "                    <items xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">Suspended</items>\n" +
            "                </items>\n" +
            "            </value>\n" +
            "        </entry>\n" +
            "    </entries>\n" +
            "</map-type>";
}


