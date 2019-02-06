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
    
    public static final String GET_SERVER_INF_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Kie Server info\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-server-info\": {\n" + 
            "          \"id\": \"default-kieserver\",\n" + 
            "          \"version\": \"7.11.0.Final-redhat-00001\",\n" + 
            "          \"name\": \"default-kieserver\",\n" + 
            "          \"location\": \"http://localhost:8080/kie-server/services/rest/server\",\n" + 
            "          \"capabilities\": [\n" + 
            "            \"KieServer\",\n" + 
            "            \"BRM\",\n" + 
            "            \"BPM\",\n" + 
            "            \"CaseMgmt\",\n" + 
            "            \"BPM-UI\",\n" + 
            "            \"BRP\",\n" + 
            "            \"DMN\",\n" + 
            "            \"Swagger\"\n" + 
            "          ],\n" + 
            "          \"messages\": [\n" + 
            "            {\n" + 
            "              \"severity\": \"INFO\",\n" + 
            "              \"timestamp\": {\n" + 
            "                \"java.util.Date\": 1538996199184\n" + 
            "              },\n" + 
            "              \"content\": [\n" + 
            "                \"Server KieServerInfo{serverId='default-kieserver', version='7.11.0.Final-redhat-00001', name='default-kieserver', location='http://localhost:8080/kie-server/services/rest/server', capabilities=[KieServer, BRM, BPM, CaseMgmt, BPM-UI, BRP, DMN, Swagger], messages=null}started successfully at Mon Oct 08 06:56:39 EDT 2018\"\n" + 
            "              ]\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_CONTAINERS_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"List of created containers\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-containers\": {\n" + 
            "          \"kie-container\": [\n" + 
            "            {\n" + 
            "              \"container-id\": \"MyProjectContainer\",\n" + 
            "              \"release-id\": {\n" + 
            "                \"group-id\": \"com.redhat\",\n" + 
            "                \"artifact-id\": \"Project1\",\n" + 
            "                \"version\": \"1.0\"\n" + 
            "              },\n" + 
            "              \"resolved-release-id\": {\n" + 
            "                \"group-id\": \"com.redhat\",\n" + 
            "                \"artifact-id\": \"Project1\",\n" + 
            "                \"version\": \"1.0\"\n" + 
            "              },\n" + 
            "              \"status\": \"STARTED\",\n" + 
            "              \"scanner\": {\n" + 
            "                \"status\": \"DISPOSED\",\n" + 
            "                \"poll-interval\": null\n" + 
            "              },\n" + 
            "              \"config-items\": [\n" + 
            "                {\n" + 
            "                  \"itemName\": \"KBase\",\n" + 
            "                  \"itemValue\": \"\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"KSession\",\n" + 
            "                  \"itemValue\": \"\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"MergeMode\",\n" + 
            "                  \"itemValue\": \"MERGE_COLLECTIONS\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"RuntimeStrategy\",\n" + 
            "                  \"itemValue\": \"SINGLETON\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                }\n" + 
            "              ],\n" + 
            "              \"messages\": [\n" + 
            "                {\n" + 
            "                  \"severity\": \"INFO\",\n" + 
            "                  \"timestamp\": {\n" + 
            "                    \"java.util.Date\": 1538996205681\n" + 
            "                  },\n" + 
            "                  \"content\": [\n" + 
            "                    \"Container MyProjectContainer successfully created with module com.redhat:Project1:1.0.\"\n" + 
            "                  ]\n" + 
            "                }\n" + 
            "              ],\n" + 
            "              \"container-alias\": \"MyProjectContainer\"\n" + 
            "            },\n" + 
            "            {\n" + 
            "              \"container-id\": \"employe-rostering\",\n" + 
            "              \"release-id\": {\n" + 
            "                \"group-id\": \"employeerostering\",\n" + 
            "                \"artifact-id\": \"employeerostering\",\n" + 
            "                \"version\": \"1.0.0-SNAPSHOT\"\n" + 
            "              },\n" + 
            "              \"resolved-release-id\": {\n" + 
            "                \"group-id\": \"employeerostering\",\n" + 
            "                \"artifact-id\": \"employeerostering\",\n" + 
            "                \"version\": \"1.0.0-SNAPSHOT\"\n" + 
            "              },\n" + 
            "              \"status\": \"STARTED\",\n" + 
            "              \"scanner\": {\n" + 
            "                \"status\": \"DISPOSED\",\n" + 
            "                \"poll-interval\": null\n" + 
            "              },\n" + 
            "              \"config-items\": [\n" + 
            "                {\n" + 
            "                  \"itemName\": \"KBase\",\n" + 
            "                  \"itemValue\": \"\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"KSession\",\n" + 
            "                  \"itemValue\": \"\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"MergeMode\",\n" + 
            "                  \"itemValue\": \"MERGE_COLLECTIONS\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"RuntimeStrategy\",\n" + 
            "                  \"itemValue\": \"SINGLETON\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                }\n" + 
            "              ],\n" + 
            "              \"messages\": [\n" + 
            "                {\n" + 
            "                  \"severity\": \"INFO\",\n" + 
            "                  \"timestamp\": {\n" + 
            "                    \"java.util.Date\": 1539029260330\n" + 
            "                  },\n" + 
            "                  \"content\": [\n" + 
            "                    \"Container employee-rostering successfully created with module employeerostering:employeerostering:1.0.0-SNAPSHOT.\"\n" + 
            "                  ]\n" + 
            "                }\n" + 
            "              ],\n" + 
            "              \"container-alias\": \"employeerostering\"\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
      
    public static final String CREATE_CONTAINER_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Container MyProjectContainer successfully deployed with module com.redhat:Project1:1.0.\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-container\": {\n" + 
            "          \"container-id\": \"MyProjectContainer\",\n" + 
            "          \"release-id\": {\n" + 
            "            \"artifact-id\": \"Project1\",\n" + 
            "            \"group-id\": \"com.redhat\",\n" + 
            "            \"version\": \"1.0\"\n" + 
            "          },\n" + 
            "          \"resolved-release-id\": {\n" + 
            "            \"artifact-id\": \"Project1\",\n" + 
            "            \"group-id\": \"com.redhat\",\n" + 
            "            \"version\": \"1.0\"\n" + 
            "          },\n" + 
            "          \"status\": \"STARTED\",\n" + 
            "          \"scanner\": {\n" + 
            "            \"status\": \"DISPOSED\",\n" + 
            "            \"poll-interval\": null\n" + 
            "          },\n" + 
            "          \"config-items\": [\n" + 
            "\n" + 
            "          ],\n" + 
            "          \"messages\": [\n" + 
            "            {\n" + 
            "              \"severity\": \"INFO\",\n" + 
            "              \"timestamp\": {\n" + 
            "                \"java.util.Date\": 1538756503852\n" + 
            "              },\n" + 
            "              \"content\": [\n" + 
            "                \"Container MyProjectContainer successfully created with module com.redhat:Project1:1.0.\"\n" + 
            "              ]\n" + 
            "            }\n" + 
            "          ],\n" + 
            "          \"container-alias\": null\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String ACTIVATE_CONTAINER_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Container MyProjectContainer successfully deployed with module com.redhat:Project1:1.0.\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-container\": {\n" + 
            "          \"container-id\": \"MyProjectContainer\",\n" + 
            "          \"release-id\": {\n" + 
            "            \"artifact-id\": \"Project1\",\n" + 
            "            \"group-id\": \"com.redhat\",\n" + 
            "            \"version\": \"1.0\"\n" + 
            "          },\n" + 
            "          \"resolved-release-id\": {\n" + 
            "            \"artifact-id\": \"Project1\",\n" + 
            "            \"group-id\": \"com.redhat\",\n" + 
            "            \"version\": \"1.0\"\n" + 
            "          },\n" + 
            "          \"status\": \"STARTED\",\n" + 
            "          \"scanner\": {\n" + 
            "            \"status\": \"DISPOSED\",\n" + 
            "            \"poll-interval\": null\n" + 
            "          },\n" + 
            "          \"config-items\": [\n" + 
            "\n" + 
            "          ],\n" + 
            "          \"messages\": [\n" + 
            "            {\n" + 
            "              \"severity\": \"INFO\",\n" + 
            "              \"timestamp\": {\n" + 
            "                \"java.util.Date\": 1538756503852\n" + 
            "              },\n" + 
            "              \"content\": [\n" + 
            "                \"Container MyProjectContainer successfully created with module com.redhat:Project1:1.0.\"\n" + 
            "              ]\n" + 
            "            }\n" + 
            "          ],\n" + 
            "          \"container-alias\": null\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String DEACTIVATE_CONTAINER_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Container MyProjectContainer successfully deployed with module com.redhat:Project1:1.0.\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-container\": {\n" + 
            "          \"container-id\": \"MyProjectContainer\",\n" + 
            "          \"release-id\": {\n" + 
            "            \"artifact-id\": \"Project1\",\n" + 
            "            \"group-id\": \"com.redhat\",\n" + 
            "            \"version\": \"1.0\"\n" + 
            "          },\n" + 
            "          \"resolved-release-id\": {\n" + 
            "            \"artifact-id\": \"Project1\",\n" + 
            "            \"group-id\": \"com.redhat\",\n" + 
            "            \"version\": \"1.0\"\n" + 
            "          },\n" + 
            "          \"status\": \"DEACTIVATED\",\n" + 
            "          \"scanner\": {\n" + 
            "            \"status\": \"DISPOSED\",\n" + 
            "            \"poll-interval\": null\n" + 
            "          },\n" + 
            "          \"config-items\": [\n" + 
            "\n" + 
            "          ],\n" + 
            "          \"messages\": [\n" + 
            "            {\n" + 
            "              \"severity\": \"INFO\",\n" + 
            "              \"timestamp\": {\n" + 
            "                \"java.util.Date\": 1538756503852\n" + 
            "              },\n" + 
            "              \"content\": [\n" + 
            "                \"Container MyProjectContainer successfully created with module com.redhat:Project1:1.0.\"\n" + 
            "              ]\n" + 
            "            }\n" + 
            "          ],\n" + 
            "          \"container-alias\": null\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_CONTAINER_RESPONSE_JSON = "⁠{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Info for container MyProjectContainer\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-containers\": {\n" + 
            "          \"kie-container\": [\n" + 
            "            {\n" + 
            "              \"container-id\": \"MyProjectContainer\",\n" + 
            "              \"release-id\": {\n" + 
            "                \"group-id\": \"com.redhat\",\n" + 
            "                \"artifact-id\": \"Project1\",\n" + 
            "                \"version\": \"1.0\"\n" + 
            "              },\n" + 
            "              \"resolved-release-id\": {\n" + 
            "                \"group-id\": \"com.redhat\",\n" + 
            "                \"artifact-id\": \"Project1\",\n" + 
            "                \"version\": \"1.0\"\n" + 
            "              },\n" + 
            "              \"status\": \"STARTED\",\n" + 
            "              \"scanner\": {\n" + 
            "                \"status\": \"DISPOSED\",\n" + 
            "                \"poll-interval\": null\n" + 
            "              },\n" + 
            "              \"config-items\": [\n" + 
            "                {\n" + 
            "                  \"itemName\": \"KBase\",\n" + 
            "                  \"itemValue\": \"\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"KSession\",\n" + 
            "                  \"itemValue\": \"\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"MergeMode\",\n" + 
            "                  \"itemValue\": \"MERGE_COLLECTIONS\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"RuntimeStrategy\",\n" + 
            "                  \"itemValue\": \"SINGLETON\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                }\n" + 
            "              ],\n" + 
            "              \"messages\": [\n" + 
            "                {\n" + 
            "                  \"severity\": \"INFO\",\n" + 
            "                  \"timestamp\": {\n" + 
            "                    \"java.util.Date\": 1538996205681\n" + 
            "                  },\n" + 
            "                  \"content\": [\n" + 
            "                    \"Container MyProjectContainer successfully created with module com.redhat:Project1:1.0.\"\n" + 
            "                  ]\n" + 
            "                }\n" + 
            "              ],\n" + 
            "              \"container-alias\": \"MyProjectContainer\"\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String DISPOSE_CONTAINER_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Container MyProjectContainer successfully disposed.\",\n" + 
            "      \"result\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_SCANNER_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Scanner info successfully retrieved\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-scanner\": {\n" + 
            "          \"status\": \"DISPOSED\",\n" + 
            "          \"poll-interval\": null\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String UPDATE_SCANNER_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Kie scanner successfully created.\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-scanner\": {\n" + 
            "          \"status\": \"STARTED\",\n" + 
            "          \"poll-interval\": 20\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_RELEASE_ID_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"ReleaseId for container MyProjectContainer\",\n" + 
            "      \"result\": {\n" + 
            "        \"release-id\": {\n" + 
            "          \"group-id\": \"com.redhat\",\n" + 
            "          \"artifact-id\": \"Project1\",\n" + 
            "          \"version\": \"1.0\"\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String UPDATE_RELEASE_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"-type\": \"SUCCESS\",\n" + 
            "      \"-msg\": \"Release id successfully updated.\",\n" + 
            "      \"release-id\": {\n" + 
            "        \"artifact-id\": \"Project1\",\n" + 
            "        \"group-id\": \"com.redhat\",\n" + 
            "        \"version\": \"1.1\"\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    public static final String GET_SERVER_STATE_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Successfully loaded server state for server id default-kieserver\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-server-state-info\": {\n" + 
            "          \"controller\": [\n" + 
            "            \"http://localhost:8080/business-central/rest/controller\"\n" + 
            "          ],\n" + 
            "          \"config\": {\n" + 
            "            \"config-items\": [\n" + 
            "              {\n" + 
            "                \"itemName\": \"org.kie.server.location\",\n" + 
            "                \"itemValue\": \"http://localhost:8080/kie-server/services/rest/server\",\n" + 
            "                \"itemType\": \"java.lang.String\"\n" + 
            "              },\n" + 
            "              {\n" + 
            "                \"itemName\": \"org.kie.server.controller.user\",\n" + 
            "                \"itemValue\": \"controllerUser\",\n" + 
            "                \"itemType\": \"java.lang.String\"\n" + 
            "              },\n" + 
            "              {\n" + 
            "                \"itemName\": \"org.kie.server.controller\",\n" + 
            "                \"itemValue\": \"http://localhost:8080/business-central/rest/controller\",\n" + 
            "                \"itemType\": \"java.lang.String\"\n" + 
            "              }\n" + 
            "            ]\n" + 
            "          },\n" + 
            "          \"containers\": [\n" + 
            "            {\n" + 
            "              \"container-id\": \"employee-rostering\",\n" + 
            "              \"release-id\": {\n" + 
            "                \"group-id\": \"employeerostering\",\n" + 
            "                \"artifact-id\": \"employeerostering\",\n" + 
            "                \"version\": \"1.0.0-SNAPSHOT\"\n" + 
            "              },\n" + 
            "              \"resolved-release-id\": null,\n" + 
            "              \"status\": \"STARTED\",\n" + 
            "              \"scanner\": {\n" + 
            "                \"status\": \"STOPPED\",\n" + 
            "                \"poll-interval\": null\n" + 
            "              },\n" + 
            "              \"config-items\": [\n" + 
            "                {\n" + 
            "                  \"itemName\": \"KBase\",\n" + 
            "                  \"itemValue\": \"\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"KSession\",\n" + 
            "                  \"itemValue\": \"\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"MergeMode\",\n" + 
            "                  \"itemValue\": \"MERGE_COLLECTIONS\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                },\n" + 
            "                {\n" + 
            "                  \"itemName\": \"RuntimeStrategy\",\n" + 
            "                  \"itemValue\": \"SINGLETON\",\n" + 
            "                  \"itemType\": \"BPM\"\n" + 
            "                }\n" + 
            "              ],\n" + 
            "              \"messages\": [],\n" + 
            "              \"container-alias\": \"employeerostering\"\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    public static final String EXECUTE_CMD_RESPONSE_JSON = "{\n" + 
            "  \"response\": [\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Container command-script-container successfully deployed with module com.redhat:Project1:1.0.\",\n" + 
            "      \"result\": {\n" + 
            "        \"kie-container\": {\n" + 
            "          \"container-id\": \"command-script-container\",\n" + 
            "          \"release-id\": {\n" + 
            "            \"group-id\": \"com.redhat\",\n" + 
            "            \"artifact-id\": \"Project1\",\n" + 
            "            \"version\": \"1.0\"\n" + 
            "          },\n" + 
            "          \"resolved-release-id\": {\n" + 
            "            \"group-id\": \"com.redhat\",\n" + 
            "            \"artifact-id\": \"Project1\",\n" + 
            "            \"version\": \"1.0\"\n" + 
            "          },\n" + 
            "          \"status\": \"DISPOSING\",\n" + 
            "          \"scanner\": {\n" + 
            "            \"status\": \"DISPOSED\",\n" + 
            "            \"poll-interval\": null\n" + 
            "          },\n" + 
            "          \"config-items\": [],\n" + 
            "          \"messages\": [\n" + 
            "            {\n" + 
            "              \"severity\": \"INFO\",\n" + 
            "              \"timestamp\": {\n" + 
            "                \"java.util.Date\": 1538768011150\n" + 
            "              },\n" + 
            "              \"content\": [\n" + 
            "                \"Container command-script-container successfully created with module com.redhat:Project1:1.0.\"\n" + 
            "              ]\n" + 
            "            }\n" + 
            "          ],\n" + 
            "          \"container-alias\": null\n" + 
            "        }\n" + 
            "      }\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Container command-script-container successfully called.\",\n" + 
            "      \"result\": \"{\\n  \\\"results\\\" : [ ],\\n  \\\"facts\\\" : [ ]\\n}\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"type\": \"SUCCESS\",\n" + 
            "      \"msg\": \"Container command-script-container successfully disposed.\",\n" + 
            "      \"result\": null\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}";
    
    public static final String EXECUTE_CMD_JSON = "{\n" + 
            "  \"commands\": [\n" + 
            "    {\n" + 
            "      \"create-container\": {\n" + 
            "        \"container\": {\n" + 
            "          \"status\": \"STARTED\",\n" + 
            "          \"container-id\": \"command-script-container\",\n" + 
            "          \"release-id\": {\n" + 
            "            \"version\": \"1.0\",\n" + 
            "            \"group-id\": \"com.redhat\",\n" + 
            "            \"artifact-id\": \"Project1\"\n" + 
            "          }\n" + 
            "        }\n" + 
            "      }\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"call-container\": {\n" + 
            "        \"payload\": \"{\\n  \\\"commands\\\" : [ {\\n    \\\"fire-all-rules\\\" : {\\n      \\\"max\\\" : -1,\\n      \\\"out-identifier\\\" : null\\n    }\\n  } ]\\n}\",\n" + 
            "        \"container-id\": \"command-script-container\"\n" + 
            "      }\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"dispose-container\": {\n" + 
            "        \"container-id\": \"command-script-container\"\n" + 
            "      }\n" + 
            "    }\n" + 
            "  ]\n" + 
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
