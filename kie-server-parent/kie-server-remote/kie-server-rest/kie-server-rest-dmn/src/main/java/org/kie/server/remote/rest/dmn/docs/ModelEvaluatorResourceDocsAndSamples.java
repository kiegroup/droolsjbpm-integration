/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.dmn.docs;


public class ModelEvaluatorResourceDocsAndSamples {

    public static final String REF_OAS_DOC =
            "Please reference container-specific generated Swagger/OAS definitions, as described in the documentation (doc ref: \"REST endpoints for specific DMN models\", req refs: BAPL-1787, DROOLS-6047).";
    public static final String REF_TE = "This is a techincal endpoint not directly documented here; this endpoint instead is documented in another Swagger/OAS definitions." + " " + REF_OAS_DOC;
    public static final String REF_GET_OAS = "Retrieves the Model-specific Swagger/OAS definitions, for the given Container id." + " " + REF_OAS_DOC;
    public static final String MODEL_SPECIFIC_DEFINITIONS_EP = "Model-specific definitions";
    public static final String MODEL_SPECIFIC_DMN_EVALUATION_EP = "Model-specific DMN evaluation";
    public static final String PARAM_CONTAINER_ID_EVAL = "Container id to be used to evaluate decisions on";
    public static final String PARAM_CONTAINER_ID_DEFS = "Container id to retrieve the Model-specific definitions";
    public static final String PARAM_REF_CONTAINER_SPECIFIC_OAS = "Reference container-specific Swagger/OAS definitions";

    // Kie Server REST API

    public static final String REF_KIESERVER_DMN_API_DOC =
    "Please reference the documentation for usage of the KIE Server REST API for DMN (doc ref: \"Executing a DMN service using the KIE Server REST API\" section in the Getting started with decision services).";
    public static final String EXAMPLE_CONTAINER_ID = "traffic-violation_1.0.0-SNAPSHOT";

    // EXAMPLES 

    public static final String EXAMPLE_KIESERVER_GET_RESPONSE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                                                    "<response type=\"SUCCESS\" msg=\"Ok models successfully retrieved from container 'traffic-violation_1.0.0-SNAPSHOT'\">\n" +
                                                                    "    <dmn-model-info-list>\n" +
                                                                    "        <model>\n" +
                                                                    "            <model-namespace>https://github.com/kiegroup/drools/kie-dmn/_60b01f4d-e407-43f7-848e-258723b5fac8</model-namespace>\n" +
                                                                    "            <model-name>Traffic Violation</model-name>\n" +
                                                                    "            <model-id>_2CD7D1AA-BD84-4B43-AD21-B0342ADE655A</model-id>\n" +
                                                                    "            <decisions>\n" +
                                                                    "                <dmn-decision-info>\n" +
                                                                    "                    <decision-id>_23428EE8-DC8B-4067-8E67-9D7C53EC975F</decision-id>\n" +
                                                                    "                    <decision-name>Fine</decision-name>\n" +
                                                                    "                </dmn-decision-info>\n" +
                                                                    "                <dmn-decision-info>\n" +
                                                                    "                    <decision-id>_B5EEE2B1-915C-44DC-BE43-C244DC066FD8</decision-id>\n" +
                                                                    "                    <decision-name>Should the driver be suspended?</decision-name>\n" +
                                                                    "                </dmn-decision-info>\n" +
                                                                    "            </decisions>\n" +
                                                                    "            <inputs>\n" +
                                                                    "                <dmn-inputdata-info>\n" +
                                                                    "                    <inputdata-id>_CEB959CD-3638-4A87-93BA-03CD0FB63AE3</inputdata-id>\n" +
                                                                    "                    <inputdata-name>Violation</inputdata-name>\n" +
                                                                    "                    <inputdata-typeref>\n" +
                                                                    "                        <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                        <local-part>tViolation</local-part>\n" +
                                                                    "                        <prefix></prefix>\n" +
                                                                    "                    </inputdata-typeref>\n" +
                                                                    "                </dmn-inputdata-info>\n" +
                                                                    "                <dmn-inputdata-info>\n" +
                                                                    "                    <inputdata-id>_B0E810E6-7596-430A-B5CF-67CE16863B6C</inputdata-id>\n" +
                                                                    "                    <inputdata-name>Driver</inputdata-name>\n" +
                                                                    "                    <inputdata-typeref>\n" +
                                                                    "                        <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                        <local-part>tDriver</local-part>\n" +
                                                                    "                        <prefix></prefix>\n" +
                                                                    "                    </inputdata-typeref>\n" +
                                                                    "                </dmn-inputdata-info>\n" +
                                                                    "            </inputs>\n" +
                                                                    "            <itemdefinitions>\n" +
                                                                    "                <dmn-itemdefinition-info>\n" +
                                                                    "                    <itemdefinition-id>_9C758F4A-7D72-4D0F-B63F-2F5B8405980E</itemdefinition-id>\n" +
                                                                    "                    <itemdefinition-name>tViolation</itemdefinition-name>\n" +
                                                                    "                    <itemdefinition-itemcomponent>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_0B6FF1E2-ACE9-4FB3-876B-5BB30B88009B</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>Code</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60b01f4d-e407-43f7-848e-258723b5fac8</namespace-uri>\n" +
                                                                    "                                <local-part>string</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_27A5DA18-3CA7-4C06-81B7-CF7F2F050E29</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>date</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>date</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_8961969A-8A80-4F12-B568-346920C0F038</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>type</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>string</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_7450F12A-3E95-4D5E-8DCE-2CB1FAC2BDD4</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>speed limit</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60b01f4d-e407-43f7-848e-258723b5fac8</namespace-uri>\n" +
                                                                    "                                <local-part>number</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_0A9A6F26-6C14-414D-A9BF-765E5850429A</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>Actual Speed</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>number</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                    </itemdefinition-itemcomponent>\n" +
                                                                    "                    <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                </dmn-itemdefinition-info>\n" +
                                                                    "                <dmn-itemdefinition-info>\n" +
                                                                    "                    <itemdefinition-id>_13C7EFD8-B85C-43BF-94D3-14FABE39A4A0</itemdefinition-id>\n" +
                                                                    "                    <itemdefinition-name>tDriver</itemdefinition-name>\n" +
                                                                    "                    <itemdefinition-itemcomponent>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_EC11744C-4160-4549-9610-2C757F40DFE8</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>Name</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>string</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_E95BE3DB-4A51-4658-A166-02493EAAC9D2</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>Age</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>number</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_7B3023E2-BC44-4BF3-BF7E-773C240FB9AD</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>State</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>string</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_3D4B49DD-700C-4925-99A7-3B2B873F7800</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>city</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>string</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_B37C49E8-B0D9-4B20-9DC6-D655BB1CA7B1</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>Points</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>number</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                    </itemdefinition-itemcomponent>\n" +
                                                                    "                    <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                </dmn-itemdefinition-info>\n" +
                                                                    "                <dmn-itemdefinition-info>\n" +
                                                                    "                    <itemdefinition-id>_A4077C7E-B57A-4DEE-9C65-7769636316F3</itemdefinition-id>\n" +
                                                                    "                    <itemdefinition-name>tFine</itemdefinition-name>\n" +
                                                                    "                    <itemdefinition-itemcomponent>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_79B152A8-DE83-4001-B88B-52DFF0D73B2D</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>Amount</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>number</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                        <dmn-itemdefinition-info>\n" +
                                                                    "                            <itemdefinition-id>_D7CB5F9C-9D55-48C2-83EE-D47045EC90D0</itemdefinition-id>\n" +
                                                                    "                            <itemdefinition-name>Points</itemdefinition-name>\n" +
                                                                    "                            <itemdefinition-typeref>\n" +
                                                                    "                                <namespace-uri>https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8</namespace-uri>\n" +
                                                                    "                                <local-part>number</local-part>\n" +
                                                                    "                                <prefix></prefix>\n" +
                                                                    "                            </itemdefinition-typeref>\n" +
                                                                    "                            <itemdefinition-itemcomponent/>\n" +
                                                                    "                            <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                        </dmn-itemdefinition-info>\n" +
                                                                    "                    </itemdefinition-itemcomponent>\n" +
                                                                    "                    <itemdefinition-iscollection>false</itemdefinition-iscollection>\n" +
                                                                    "                </dmn-itemdefinition-info>\n" +
                                                                    "            </itemdefinitions>\n" +
                                                                    "            <decisionservices/>\n" +
                                                                    "        </model>\n" +
                                                                    "    </dmn-model-info-list>\n" +
                                                                    "</response>";
    public static final String EXAMPLE_KIESERVER_GET_RESPONSE_JSON = "{\n" +
                                                                     "  \"type\" : \"SUCCESS\",\n" +
                                                                     "  \"msg\" : \"OK models successfully retrieved from container 'Traffic-Violation_1.0.0-SNAPSHOT'\",\n" +
                                                                     "  \"result\" : {\n" +
                                                                     "    \"dmn-model-info-list\" : {\n" +
                                                                     "      \"models\" : [ {\n" +
                                                                     "        \"model-namespace\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "        \"model-name\" : \"Traffic Violation\",\n" +
                                                                     "        \"model-id\" : \"_2CD7D1AA-BD84-4B43-AD21-B0342ADE655A\",\n" +
                                                                     "        \"decisions\" : [ {\n" +
                                                                     "          \"decision-id\" : \"_23428EE8-DC8B-4067-8E67-9D7C53EC975F\",\n" +
                                                                     "          \"decision-name\" : \"Fine\"\n" +
                                                                     "        }, {\n" +
                                                                     "          \"decision-id\" : \"_B5EEE2B1-915C-44DC-BE43-C244DC066FD8\",\n" +
                                                                     "          \"decision-name\" : \"Should the driver be suspended?\"\n" +
                                                                     "        } ],\n" +
                                                                     "        \"inputs\" : [ {\n" +
                                                                     "          \"inputdata-id\" : \"_CEB959CD-3638-4A87-93BA-03CD0FB63AE3\",\n" +
                                                                     "          \"inputdata-name\" : \"Violation\",\n" +
                                                                     "          \"inputdata-typeRef\" : {\n" +
                                                                     "            \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "            \"local-part\" : \"tViolation\",\n" +
                                                                     "            \"prefix\" : \"\"\n" +
                                                                     "          }\n" +
                                                                     "        }, {\n" +
                                                                     "          \"inputdata-id\" : \"_B0E810E6-7596-430A-B5CF-67CE16863B6C\",\n" +
                                                                     "          \"inputdata-name\" : \"Driver\",\n" +
                                                                     "          \"inputdata-typeRef\" : {\n" +
                                                                     "            \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "            \"local-part\" : \"tDriver\",\n" +
                                                                     "            \"prefix\" : \"\"\n" +
                                                                     "          }\n" +
                                                                     "        } ],\n" +
                                                                     "        \"itemDefinitions\" : [ {\n" +
                                                                     "          \"itemdefinition-id\" : \"_13C7EFD8-B85C-43BF-94D3-14FABE39A4A0\",\n" +
                                                                     "          \"itemdefinition-name\" : \"tDriver\",\n" +
                                                                     "          \"itemdefinition-typeRef\" : null,\n" +
                                                                     "          \"itemdefinition-itemComponent\" : [ {\n" +
                                                                     "            \"itemdefinition-id\" : \"_EC11744C-4160-4549-9610-2C757F40DFE8\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Name\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"string\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          }, {\n" +
                                                                     "            \"itemdefinition-id\" : \"_E95BE3DB-4A51-4658-A166-02493EAAC9D2\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Age\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"number\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          }, {\n" +
                                                                     "            \"itemdefinition-id\" : \"_7B3023E2-BC44-4BF3-BF7E-773C240FB9AD\",\n" +
                                                                     "            \"itemdefinition-name\" : \"State\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"string\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          }, {\n" +
                                                                     "            \"itemdefinition-id\" : \"_3D4B49DD-700C-4925-99A7-3B2B873F7800\",\n" +
                                                                     "            \"itemdefinition-name\" : \"City\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"string\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          }, {\n" +
                                                                     "            \"itemdefinition-id\" : \"_B37C49E8-B0D9-4B20-9DC6-D655BB1CA7B1\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Points\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"number\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          } ],\n" +
                                                                     "          \"itemdefinition-isCollection\" : false\n" +
                                                                     "        }, {\n" +
                                                                     "          \"itemdefinition-id\" : \"_A4077C7E-B57A-4DEE-9C65-7769636316F3\",\n" +
                                                                     "          \"itemdefinition-name\" : \"tFine\",\n" +
                                                                     "          \"itemdefinition-typeRef\" : null,\n" +
                                                                     "          \"itemdefinition-itemComponent\" : [ {\n" +
                                                                     "            \"itemdefinition-id\" : \"_79B152A8-DE83-4001-B88B-52DFF0D73B2D\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Amount\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"number\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          }, {\n" +
                                                                     "            \"itemdefinition-id\" : \"_D7CB5F9C-9D55-48C2-83EE-D47045EC90D0\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Points\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"number\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          } ],\n" +
                                                                     "          \"itemdefinition-isCollection\" : false\n" +
                                                                     "        }, {\n" +
                                                                     "          \"itemdefinition-id\" : \"_9C758F4A-7D72-4D0F-B63F-2F5B8405980E\",\n" +
                                                                     "          \"itemdefinition-name\" : \"tViolation\",\n" +
                                                                     "          \"itemdefinition-typeRef\" : null,\n" +
                                                                     "          \"itemdefinition-itemComponent\" : [ {\n" +
                                                                     "            \"itemdefinition-id\" : \"_0B6FF1E2-ACE9-4FB3-876B-5BB30B88009B\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Code\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"string\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          }, {\n" +
                                                                     "            \"itemdefinition-id\" : \"_27A5DA18-3CA7-4C06-81B7-CF7F2F050E29\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Date\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"date\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          }, {\n" +
                                                                     "            \"itemdefinition-id\" : \"_8961969A-8A80-4F12-B568-346920C0F038\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Type\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"string\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          }, {\n" +
                                                                     "            \"itemdefinition-id\" : \"_7450F12A-3E95-4D5E-8DCE-2CB1FAC2BDD4\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Speed Limit\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"number\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          }, {\n" +
                                                                     "            \"itemdefinition-id\" : \"_0A9A6F26-6C14-414D-A9BF-765E5850429A\",\n" +
                                                                     "            \"itemdefinition-name\" : \"Actual Speed\",\n" +
                                                                     "            \"itemdefinition-typeRef\" : {\n" +
                                                                     "              \"namespace-uri\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                     "              \"local-part\" : \"number\",\n" +
                                                                     "              \"prefix\" : \"\"\n" +
                                                                     "            },\n" +
                                                                     "            \"itemdefinition-itemComponent\" : [ ],\n" +
                                                                     "            \"itemdefinition-isCollection\" : false\n" +
                                                                     "          } ],\n" +
                                                                     "          \"itemdefinition-isCollection\" : false\n" +
                                                                     "        } ],\n" +
                                                                     "        \"decisionServices\" : [ ]\n" +
                                                                     "      } ]\n" +
                                                                     "    }\n" +
                                                                     "  }\n" +
                                                                     "}";

    public static final String EXAMPLE_KIESERVER_POST_REQ_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                                                "<dmn-evaluation-context>\n" +
                                                                "    <dmn-context xsi:type=\"jaxbListWrapper\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                                                                "        <type>MAP</type>\n" +
                                                                "        <element xsi:type=\"jaxbStringObjectPair\" key=\"Violation\">\n" +
                                                                "            <value xsi:type=\"jaxbListWrapper\">\n" +
                                                                "                <type>MAP</type>\n" +
                                                                "                <element xsi:type=\"jaxbStringObjectPair\" key=\"Type\">\n" +
                                                                "                    <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">speed</value>\n" +
                                                                "                </element>\n" +
                                                                "                <element xsi:type=\"jaxbStringObjectPair\" key=\"Speed Limit\">\n" +
                                                                "                    <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</value>\n" +
                                                                "                </element>\n" +
                                                                "                <element xsi:type=\"jaxbStringObjectPair\" key=\"Actual Speed\">\n" +
                                                                "                    <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">135</value>\n" +
                                                                "                </element>\n" +
                                                                "            </value>\n" +
                                                                "        </element>\n" +
                                                                "        <element xsi:type=\"jaxbStringObjectPair\" key=\"Driver\">\n" +
                                                                "            <value xsi:type=\"jaxbListWrapper\">\n" +
                                                                "                <type>MAP</type>\n" +
                                                                "                <element xsi:type=\"jaxbStringObjectPair\" key=\"Points\">\n" +
                                                                "                    <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">15</value>\n" +
                                                                "                </element>\n" +
                                                                "            </value>\n" +
                                                                "        </element>\n" +
                                                                "    </dmn-context>\n" +
                                                                "</dmn-evaluation-context>";
    public static final String EXAMPLE_KIESERVER_POST_REQ_JSON = "{\n" +
                                                                 "  \"model-namespace\" : \"https://github.com/kiegroup/drools/kie-dmn/_60B01F4D-E407-43F7-848E-258723B5FAC8\",\n" +
                                                                 "  \"model-name\" : \"Traffic Violation\",\n" +
                                                                 "  \"dmn-context\" :\n" +
                                                                 "  {\n" +
                                                                 "    \"Driver\" :\n" +
                                                                 "    {\n" +
                                                                 "       \"Points\" : 15\n" +
                                                                 "    },\n" +
                                                                 "    \"Violation\" :\n" +
                                                                 "    {\n" +
                                                                 "        \"Type\" : \"speed\",\n" +
                                                                 "        \"Actual Speed\" : 135,\n" +
                                                                 "        \"Speed Limit\" : 100\n" +
                                                                 "    }\n" +
                                                                 "  }\n" +
                                                                 "}";
    public static final String EXAMPLE_KIESERVER_POST_RESPONSE_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                                                     "<response type=\"SUCCESS\" msg=\"OK from container 'Traffic_1.0.0-SNAPSHOT'\">\n" +
                                                                     "    <dmn-evaluation-result>\n" +
                                                                     "        <model-namespace>https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF</model-namespace>\n" +
                                                                     "        <model-name>Traffic Violation</model-name>\n" +
                                                                     "        <dmn-context xsi:type=\"jaxbListWrapper\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                                                                     "            <type>MAP</type>\n" +
                                                                     "            <element xsi:type=\"jaxbStringObjectPair\" key=\"Violation\">\n" +
                                                                     "                <value xsi:type=\"jaxbListWrapper\">\n" +
                                                                     "                    <type>MAP</type>\n" +
                                                                     "                    <element xsi:type=\"jaxbStringObjectPair\" key=\"Type\">\n" +
                                                                     "                        <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">speed</value>\n" +
                                                                     "                    </element>\n" +
                                                                     "                    <element xsi:type=\"jaxbStringObjectPair\" key=\"Speed Limit\">\n" +
                                                                     "                        <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">100</value>\n" +
                                                                     "                    </element>\n" +
                                                                     "                    <element xsi:type=\"jaxbStringObjectPair\" key=\"Actual Speed\">\n" +
                                                                     "                        <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">135</value>\n" +
                                                                     "                    </element>\n" +
                                                                     "                </value>\n" +
                                                                     "            </element>\n" +
                                                                     "            <element xsi:type=\"jaxbStringObjectPair\" key=\"Driver\">\n" +
                                                                     "                <value xsi:type=\"jaxbListWrapper\">\n" +
                                                                     "                    <type>MAP</type>\n" +
                                                                     "                    <element xsi:type=\"jaxbStringObjectPair\" key=\"Points\">\n" +
                                                                     "                        <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">15</value>\n" +
                                                                     "                    </element>\n" +
                                                                     "                </value>\n" +
                                                                     "            </element>\n" +
                                                                     "            <element xsi:type=\"jaxbStringObjectPair\" key=\"Fine\">\n" +
                                                                     "                <value xsi:type=\"jaxbListWrapper\">\n" +
                                                                     "                    <type>MAP</type>\n" +
                                                                     "                    <element xsi:type=\"jaxbStringObjectPair\" key=\"Points\">\n" +
                                                                     "                        <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">7</value>\n" +
                                                                     "                    </element>\n" +
                                                                     "                    <element xsi:type=\"jaxbStringObjectPair\" key=\"Amount\">\n" +
                                                                     "                        <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">1000</value>\n" +
                                                                     "                    </element>\n" +
                                                                     "                </value>\n" +
                                                                     "            </element>\n" +
                                                                     "            <element xsi:type=\"jaxbStringObjectPair\" key=\"Should the driver be suspended?\">\n" +
                                                                     "                <value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">Yes</value>\n" +
                                                                     "            </element>\n" +
                                                                     "        </dmn-context>\n" +
                                                                     "        <messages/>\n" +
                                                                     "        <decisionResults>\n" +
                                                                     "            <entry>\n" +
                                                                     "                <key>_4055D956-1C47-479C-B3F4-BAEB61F1C929</key>\n" +
                                                                     "                <value>\n" +
                                                                     "                    <decision-id>_4055D956-1C47-479C-B3F4-BAEB61F1C929</decision-id>\n" +
                                                                     "                    <decision-name>Fine</decision-name>\n" +
                                                                     "                    <result xsi:type=\"jaxbListWrapper\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                                                                     "                        <type>MAP</type>\n" +
                                                                     "                        <element xsi:type=\"jaxbStringObjectPair\" key=\"Points\">\n" +
                                                                     "                            <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">7</value>\n" +
                                                                     "                        </element>\n" +
                                                                     "                        <element xsi:type=\"jaxbStringObjectPair\" key=\"Amount\">\n" +
                                                                     "                            <value xsi:type=\"xs:decimal\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">1000</value>\n" +
                                                                     "                        </element>\n" +
                                                                     "                    </result>\n" +
                                                                     "                    <messages/>\n" +
                                                                     "                    <status>SUCCEEDED</status>\n" +
                                                                     "                </value>\n" +
                                                                     "            </entry>\n" +
                                                                     "            <entry>\n" +
                                                                     "                <key>_8A408366-D8E9-4626-ABF3-5F69AA01F880</key>\n" +
                                                                     "                <value>\n" +
                                                                     "                    <decision-id>_8A408366-D8E9-4626-ABF3-5F69AA01F880</decision-id>\n" +
                                                                     "                    <decision-name>Should the driver be suspended?</decision-name>\n" +
                                                                     "                    <result xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">Yes</result>\n" +
                                                                     "                    <messages/>\n" +
                                                                     "                    <status>SUCCEEDED</status>\n" +
                                                                     "                </value>\n" +
                                                                     "            </entry>\n" +
                                                                     "        </decisionResults>\n" +
                                                                     "    </dmn-evaluation-result>\n" +
                                                                     "</response>";
    public static final String EXAMPLE_KIESERVER_POST_RESPONSE_JSON = "{\n" +
                                                                      "  \"type\": \"SUCCESS\",\n" +
                                                                      "  \"msg\": \"OK from container 'Traffic-Violation_1.0.0-SNAPSHOT'\",\n" +
                                                                      "  \"result\": {\n" +
                                                                      "      \"dmn-evaluation-result\": {\n" +
                                                                      "          \"messages\": [],\n" +
                                                                      "          \"model-namespace\": \"https://github.com/kiegroup/drools/kie-dmn/_7D8116DE-ADF5-4560-A116-FE1A2EAFFF48\",\n" +
                                                                      "          \"model-name\": \"Traffic Violation\",\n" +
                                                                      "          \"decision-name\": [],\n" +
                                                                      "          \"dmn-context\": {\n" +
                                                                      "              \"Violation\": {\n" +
                                                                      "                \"Type\": \"speed\",\n" +
                                                                      "                \"Speed Limit\": 100,\n" +
                                                                      "                \"Actual Speed\": 135\n" +
                                                                      "              },\n" +
                                                                      "              \"Should Driver be Suspended?\": \"Yes\",\n" +
                                                                      "                \"Driver\": {\n" +
                                                                      "                  \"Points\": 15\n" +
                                                                      "                },\n" +
                                                                      "                \"Fine\": {\n" +
                                                                      "                  \"Points\": 7,\n" +
                                                                      "                  \"Amount\": 1000\n" +
                                                                      "                }\n" +
                                                                      "            },\n" +
                                                                      "      \"decision-results\": {\n" +
                                                                      "          \"_E1AF5AC2-E259-455C-96E4-596E30D3BC86\": {\n" +
                                                                      "              \"messages\": [],\n" +
                                                                      "              \"decision-id\": \"_E1AF5AC2-E259-455C-96E4-596E30D3BC86\",\n" +
                                                                      "              \"decision-name\": \"Should the Driver be Suspended?\",\n" +
                                                                      "              \"result\": \"Yes\",\n" +
                                                                      "              \"status\": \"SUCCEEDED\"\n" +
                                                                      "            },\n" +
                                                                      "          \"_D7F02CE0-AF50-4505-AB80-C7D6DE257920\": {\n" +
                                                                      "              \"messages\": [],\n" +
                                                                      "              \"decision-id\": \"_D7F02CE0-AF50-4505-AB80-C7D6DE257920\",\n" +
                                                                      "              \"decision-name\": \"Fine\",\n" +
                                                                      "              \"result\": {\n" +
                                                                      "                \"Points\": 7,\n" +
                                                                      "                \"Amount\": 1000\n" +
                                                                      "              },\n" +
                                                                      "          \"status\": \"SUCCEEDED\"\n" +
                                                                      "        }\n" +
                                                                      "      }\n" +
                                                                      "    }\n" +
                                                                      "  }\n" +
                                                                      "}";

    private ModelEvaluatorResourceDocsAndSamples() {
        // only used for constants.
    }
}
