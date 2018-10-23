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
