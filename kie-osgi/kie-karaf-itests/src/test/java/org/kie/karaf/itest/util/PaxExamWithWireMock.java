/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.karaf.itest.util;

import java.lang.reflect.Field;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.ops4j.pax.exam.junit.PaxExam;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.*;

/**
 * Special extension to PaxExam JUNit runner to allow use of static server to return fixed responses to easily test
 * kie server client operations instead of relying on fully configured KieServer instance to be available for the tests.
 *
 * Since actual test runs within Karaf, bootstrapping server as part of beforeClass won't work as that is already in Karaf and thus will require
 * lots of dependencies and bundles.
 *
 * Note: There is some code duplication for testing all supported formats (JAXB, JSON, XSTREAM) and some might think that PaxExamParameterized
 * would be great fix to avoid this duplication. The issue with this approach is that we use static server that returns single content
 * and by that we only supports one format at a time. Since PaxExamParametrized is just a wrapper on top of actual Runner we cannot get hold
 * of parameters that are going to be used for given test. Thus relying on default one with some test duplication that changes the format to be used.
 */
public class PaxExamWithWireMock extends PaxExam {

    private WireMockServer wireMockServer;
    private Class<?> clazz;
    private Integer port;
    private String host;
    private String type;

    public PaxExamWithWireMock(Class<?> klass) throws InitializationError {
        super(klass);
        this.clazz = klass;

        port = (Integer) getFieldValue("PORT");
        host = (String) getFieldValue("HOST");
        type = (String) getFieldValue("TYPE");
    }

    @Override
    public void run(RunNotifier notifier) {

        this.wireMockServer = new WireMockServer(wireMockConfig().bindAddress(host).port(port));
        this.wireMockServer.start();

        if ("xstream".equals(type)) {
            setupMockServerXStream();
        } else if ("jaxb".equals(type)) {
            setupMockServerXML();
        } else if ("json".equals(type)) {
            setupMockServerJSON();
        }

        System.out.println("WireMock server started and bound to localhost:" + port);
        super.run(notifier);

        this.wireMockServer.stop();

    }

    protected Object getFieldValue(String name) {
        try {
            Field f = clazz.getField(name);
            f.setAccessible(true);
            return f.get(null);
        } catch (Exception e) {
            return null;
        }
    }

    public void setupMockServerXML() {
        configureFor("localhost", port);

        stubFor(get(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Kie Server info\">\n" +
                                "    <kie-server-info>\n" +
                                "        <capabilities>KieServer</capabilities>\n" +
                                "        <capabilities>BRM</capabilities>\n" +
                                "        <capabilities>BPM</capabilities>\n" +
                                "        <location>http://localhost:"+ port + "/kie-server/services/rest/server</location>\n" +
                                "        <name>kie-server</name>\n" +
                                "        <id>generated-id-kie-server</id>\n" +
                                "        <version>1.2.3</version>\n" +
                                "    </kie-server-info>\n" +
                                "</response>")));
        stubFor(get(urlEqualTo("/containers"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"List of created containers\">\n" +
                                "    <kie-containers>\n" +
                                "        <kie-container container-id=\"evaluationproject\" status=\"STARTED\">\n" +
                                "            <release-id>\n" +
                                "                <artifact-id>Evaluation</artifact-id>\n" +
                                "                <group-id>org.jbpm</group-id>\n" +
                                "                <version>1.0</version>\n" +
                                "            </release-id>\n" +
                                "            <resolved-release-id>\n" +
                                "                <artifact-id>Evaluation</artifact-id>\n" +
                                "                <group-id>org.jbpm</group-id>\n" +
                                "                <version>1.0</version>\n" +
                                "            </resolved-release-id>\n" +
                                "        </kie-container>\n" +
                                "    </kie-containers>\n" +
                                "</response>")));

        stubFor(get(urlPathEqualTo("/queries/processes/definitions"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("pageSize", equalTo("10"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<process-definitions>\n" +
                                "    <processes>\n" +
                                "        <process-id>evaluation</process-id>\n" +
                                "        <process-name>Evaluation</process-name>\n" +
                                "        <process-version>1</process-version>\n" +
                                "        <package>Evaluation.src.main.resources</package>\n" +
                                "        <container-id>evaluationproject</container-id>\n" +
                                "    </processes>" +
                                "</process-definitions>")));


        stubFor(get(urlEqualTo("/containers/evaluationproject/processes/definitions/evaluation"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<process-definition>\n" +
                                "    <process-id>evaluation</process-id>\n" +
                                "    <process-name>Evaluation</process-name>\n" +
                                "    <process-version>1</process-version>\n" +
                                "    <package>Evaluation.src.main.resources</package>\n" +
                                "    <container-id>evaluationproject</container-id>\n" +
                                "    <associated-entities>\n" +
                                "        <entry>\n" +
                                "            <key>PM Evaluation</key>\n" +
                                "            <value>\n" +
                                "                <item>PM</item>\n" +
                                "            </value>\n" +
                                "        </entry>\n" +
                                "        <entry>\n" +
                                "            <key>HR Evaluation</key>\n" +
                                "            <value>\n" +
                                "                <item>HR</item>\n" +
                                "            </value>\n" +
                                "        </entry>\n" +
                                "        <entry>\n" +
                                "            <key>Self Evaluation</key>\n" +
                                "            <value>\n" +
                                "                <item>#{employee}</item>\n" +
                                "            </value>\n" +
                                "        </entry>\n" +
                                "    </associated-entities>\n" +
                                "    <service-tasks/>\n" +
                                "    <process-variables>\n" +
                                "        <entry>\n" +
                                "            <key>reason</key>\n" +
                                "            <value>java.lang.String</value>\n" +
                                "        </entry>\n" +
                                "        <entry>\n" +
                                "            <key>performance</key>\n" +
                                "            <value>java.lang.String</value>\n" +
                                "        </entry>\n" +
                                "        <entry>\n" +
                                "            <key>employee</key>\n" +
                                "            <value>java.lang.String</value>\n" +
                                "        </entry>\n" +
                                "    </process-variables>\n" +
                                "    <process-subprocesses/>\n" +
                                "</process-definition>")));

            stubFor(post(urlEqualTo("/containers/evaluationproject/processes/evaluation/instances"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<long-type>\n" +
                                "    <value>2</value>\n" +
                                "</long-type>")));

        stubFor(get(urlPathEqualTo("/queries/tasks/instances"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("pageSize", equalTo("10"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<task-summary-list>\n" +
                                "    <task-summary>\n" +
                                "        <task-id>4</task-id>\n" +
                                "        <task-name>Self Evaluation</task-name>\n" +
                                "        <task-description>Please perform a self-evalutation.</task-description>\n" +
                                "        <task-status>Reserved</task-status>\n" +
                                "        <task-priority>0</task-priority>\n" +
                                "        <task-actual-owner>maciek</task-actual-owner>\n" +
                                "        <task-created-by>maciek</task-created-by>\n" +
                                "        <task-created-on>2015-08-03T13:34:17.774+02:00</task-created-on>\n" +
                                "        <task-activation-time>2015-08-03T13:34:17.774+02:00</task-activation-time>\n" +
                                "        <task-proc-inst-id>2</task-proc-inst-id>\n" +
                                "        <task-proc-def-id>evaluation</task-proc-def-id>\n" +
                                "        <task-container-id>evaluationproject</task-container-id>\n" +
                                "        <task-parent-id>-1</task-parent-id>\n" +
                                "    </task-summary>" +
                                "</task-summary-list>")));

            stubFor(put(urlEqualTo("/containers/evaluationproject/tasks/4/states/started"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("")));

            stubFor(put(urlEqualTo("/containers/evaluationproject/tasks/4/states/completed"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("")));

            stubFor(post(urlEqualTo("/containers/instances/evaluationproject"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<response type=\"SUCCESS\" msg=\"Container evaluationproject successfully called.\">\n" +
                                "    <results>&lt;?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?&gt;\n" +
                                "&lt;execution-results&gt;\n" +
                                "    &lt;results&gt;\n" +
                                "        &lt;item key=\"person\"&gt;\n" +
                                "            &lt;value xsi:type=\"xs:string\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"&gt;john&lt;/value&gt;\n" +
                                "        &lt;/item&gt;\n" +
                                "    &lt;/results&gt;\n" +
                                "    &lt;facts&gt;\n" +
                                "        &lt;item key=\"person\"&gt;\n" +
                                "            &lt;value xsi:type=\"defaultFactHandle\" external-form=\"0:2:604351499:3267851:2:DEFAULT:NON_TRAIT:java.lang.String\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"/&gt;\n" +
                                "        &lt;/item&gt;\n" +
                                "    &lt;/facts&gt;\n" +
                                "&lt;/execution-results&gt;\n" +
                                "</results>\n" +
                                "</response>")));

            stubFor(delete(urlEqualTo("/containers/evaluationproject/processes/instances/2"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("")));

        stubFor(get(urlEqualTo("/queries/processes/instances/2"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<process-instance>\n" +
                                "    <process-instance-id>2</process-instance-id>\n" +
                                "    <process-id>evaluation</process-id>\n" +
                                "    <process-name>Evaluation</process-name>\n" +
                                "    <process-version>1</process-version>\n" +
                                "    <process-instance-state>3</process-instance-state>\n" +
                                "    <container-id>evaluationproject</container-id>\n" +
                                "    <initiator>maciek</initiator>\n" +
                                "    <start-date>2015-08-03T13:34:17.771+02:00</start-date>\n" +
                                "    <process-instance-desc>Evaluation</process-instance-desc>\n" +
                                "    <correlation-key></correlation-key>\n" +
                                "    <parent-instance-id>-1</parent-instance-id>\n" +
                                "</process-instance>")));

    }

    public void setupMockServerJSON() {
        configureFor("localhost", port);

        stubFor(get(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"type\": \"SUCCESS\",\n" +
                                "  \"msg\": \"Kie Server info\",\n" +
                                "  \"result\": {\n" +
                                "    \"kie-server-info\": {\n" +
                                "      \"version\": \"1.2.3\",\n" +
                                "      \"name\": \"maciek-kie-server\",\n" +
                                "      \"location\": \"http://localhost:" + port +"/kie-server/services/rest/server\",\n" +
                                "      \"capabilities\": [\n" +
                                "        \"KieServer\",\n" +
                                "        \"BRM\",\n" +
                                "        \"BPM\"\n" +
                                "      ],\n" +
                                "      \"id\": \"maciek-kie-server\"\n" +
                                "    }\n" +
                                "  }\n" +
                                "}")));

        stubFor(get(urlEqualTo("/containers"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"type\": \"SUCCESS\",\n" +
                                "  \"msg\": \"List of created containers\",\n" +
                                "  \"result\": {\n" +
                                "    \"kie-containers\": {\n" +
                                "      \"kie-container\": [\n" +
                                "        {\n" +
                                "          \"status\": \"STARTED\",\n" +
                                "          \"container-id\": \"evaluationproject\",\n" +
                                "          \"release-id\": {\n" +
                                "            \"version\": \"1.0\",\n" +
                                "            \"group-id\": \"org.jbpm\",\n" +
                                "            \"artifact-id\": \"Evaluation\"\n" +
                                "          },\n" +
                                "          \"resolved-release-id\": {\n" +
                                "            \"version\": \"1.0\",\n" +
                                "            \"group-id\": \"org.jbpm\",\n" +
                                "            \"artifact-id\": \"Evaluation\"\n" +
                                "          }\n" +
                                "        }\n" +
                                "      ]\n" +
                                "    }\n" +
                                "  }\n" +
                                "}")));

        stubFor(get(urlPathEqualTo("/queries/processes/definitions"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("pageSize", equalTo("10"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"processes\": [\n" +
                                "    {\n" +
                                "      \"process-id\": \"evaluation\",\n" +
                                "      \"process-name\": \"Evaluation\",\n" +
                                "      \"process-version\": \"1\",\n" +
                                "      \"package\": \"Evaluation.src.main.resources\",\n" +
                                "      \"container-id\": \"evaluationproject\"\n" +
                                "    }\n" +
                                "  ]\n" +
                                "}")));


        stubFor(get(urlEqualTo("/containers/evaluationproject/processes/definitions/evaluation"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"process-id\": \"evaluation\",\n" +
                                "  \"process-name\": \"Evaluation\",\n" +
                                "  \"process-version\": \"1\",\n" +
                                "  \"package\": \"Evaluation.src.main.resources\",\n" +
                                "  \"container-id\": \"evaluationproject\",\n" +
                                "  \"associated-entities\": {\n" +
                                "    \"PM Evaluation\": [\"PM\"],\n" +
                                "    \"HR Evaluation\": [\"HR\"],\n" +
                                "    \"Self Evaluation\": [\"#{employee}\"]\n" +
                                "  },\n" +
                                "  \"service-tasks\": {},\n" +
                                "  \"process-variables\": {\n" +
                                "    \"reason\": \"java.lang.String\",\n" +
                                "    \"performance\": \"java.lang.String\",\n" +
                                "    \"employee\": \"java.lang.String\"\n" +
                                "  },\n" +
                                "  \"process-subprocesses\": []\n" +
                                "}")));

        stubFor(post(urlEqualTo("/containers/evaluationproject/processes/evaluation/instances"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("2")));

        stubFor(get(urlPathEqualTo("/queries/tasks/instances"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("pageSize", equalTo("10"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"task-summary\": [\n" +
                                "    {\n" +
                                "      \"task-id\": 4,\n" +
                                "      \"task-name\": \"Self Evaluation\",\n" +
                                "      \"task-description\": \"Please perform a self-evalutation.\",\n" +
                                "      \"task-status\": \"Completed\",\n" +
                                "      \"task-priority\": 0,\n" +
                                "      \"task-actual-owner\": \"maciek\",\n" +
                                "      \"task-created-by\": \"maciek\",\n" +
                                "      \"task-created-on\": 1438601657774,\n" +
                                "      \"task-activation-time\": 1438601657774,\n" +
                                "      \"task-proc-inst-id\": 2,\n" +
                                "      \"task-proc-def-id\": \"evaluation\",\n" +
                                "      \"task-container-id\": \"evaluationproject\",\n" +
                                "      \"task-parent-id\": -1\n" +
                                "    }\n" +
                                "  ]\n" +
                                "}")));

        stubFor(put(urlEqualTo("/containers/evaluationproject/tasks/4/states/started"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

        stubFor(put(urlEqualTo("/containers/evaluationproject/tasks/4/states/completed"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

        stubFor(post(urlEqualTo("/containers/instances/evaluationproject"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"type\": \"SUCCESS\",\n" +
                                "  \"msg\": \"Container evaluationproject successfully called.\",\n" +
                                "  \"result\": \"{\\\"results\\\":[{\\\"key\\\":\\\"person\\\",\\\"value\\\":\\\"john\\\"}],\\\"facts\\\":[{\\\"key\\\":\\\"person\\\",\\\"value\\\":{\\\"external-form\\\":\\\"0:3:1546737401:3267851:3:DEFAULT:NON_TRAIT:java.lang.String\\\"}}]}\"\n" +
                                "}")));

        stubFor(delete(urlEqualTo("/containers/evaluationproject/processes/instances/2"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withHeader("Content-Type", "application/json")
                        .withBody("")));

        stubFor(get(urlEqualTo("/queries/processes/instances/2"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"initiator\": \"maciek\",\n" +
                                "  \"process-instance-id\": 3,\n" +
                                "  \"process-id\": \"evaluation\",\n" +
                                "  \"process-name\": \"Evaluation\",\n" +
                                "  \"process-version\": \"1\",\n" +
                                "  \"process-instance-state\": 3,\n" +
                                "  \"container-id\": \"evaluationproject\",\n" +
                                "  \"start-date\": 1438602985335,\n" +
                                "  \"process-instance-desc\": \"Evaluation\",\n" +
                                "  \"correlation-key\": \"\",\n" +
                                "  \"parent-instance-id\": -1\n" +
                                "}")));
    }

    public void setupMockServerXStream() {
        configureFor("localhost", port);

        stubFor(get(urlEqualTo("/"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<org.kie.server.api.model.ServiceResponse>\n" +
                                "  <type>SUCCESS</type>\n" +
                                "  <msg>Kie Server info</msg>\n" +
                                "  <result class=\"kie-server-info\">\n" +
                                "    <serverId>maciek-kie-server</serverId>\n" +
                                "    <version>1.2.3</version>\n" +
                                "    <name>maciek-kie-server</name>\n" +
                                "    <location>http://localhost:" + port + "/kie-server/services/rest/server</location>\n" +
                                "    <capabilities>\n" +
                                "      <string>KieServer</string>\n" +
                                "      <string>BRM</string>\n" +
                                "      <string>BPM</string>\n" +
                                "    </capabilities>\n" +
                                "  </result>\n" +
                                "</org.kie.server.api.model.ServiceResponse>")));
        stubFor(get(urlEqualTo("/containers"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<org.kie.server.api.model.ServiceResponse>\n" +
                                "  <type>SUCCESS</type>\n" +
                                "  <msg>List of created containers</msg>\n" +
                                "  <result class=\"kie-containers\">\n" +
                                "    <kie-container>\n" +
                                "      <container-id>evaluationproject</container-id>\n" +
                                "      <release-id>\n" +
                                "        <group-id>org.jbpm</group-id>\n" +
                                "        <artifact-id>Evaluation</artifact-id>\n" +
                                "        <version>1.0</version>\n" +
                                "      </release-id>\n" +
                                "      <resolved-release-id>\n" +
                                "        <group-id>org.jbpm</group-id>\n" +
                                "        <artifact-id>Evaluation</artifact-id>\n" +
                                "        <version>1.0</version>\n" +
                                "      </resolved-release-id>\n" +
                                "      <status>STARTED</status>\n" +
                                "    </kie-container>\n" +
                                "  </result>\n" +
                                "</org.kie.server.api.model.ServiceResponse>")));

        stubFor(get(urlPathEqualTo("/queries/processes/definitions"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("pageSize", equalTo("10"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<org.kie.server.api.model.definition.ProcessDefinitionList>\n" +
                                "  <processes>\n" +
                                "    <org.kie.server.api.model.definition.ProcessDefinition>\n" +
                                "      <id>evaluation</id>\n" +
                                "      <name>Evaluation</name>\n" +
                                "      <version>1</version>\n" +
                                "      <packageName>Evaluation.src.main.resources</packageName>\n" +
                                "      <containerId>evaluationproject</containerId>\n" +
                                "    </org.kie.server.api.model.definition.ProcessDefinition>" +
                                "  </processes>\n" +
                                "</org.kie.server.api.model.definition.ProcessDefinitionList>")));


        stubFor(get(urlEqualTo("/containers/evaluationproject/processes/definitions/evaluation"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<org.kie.server.api.model.definition.ProcessDefinition>\n" +
                                "  <id>evaluation</id>\n" +
                                "  <name>Evaluation</name>\n" +
                                "  <version>1</version>\n" +
                                "  <packageName>Evaluation.src.main.resources</packageName>\n" +
                                "  <containerId>evaluationproject</containerId>\n" +
                                "  <associatedEntities>\n" +
                                "    <entry>\n" +
                                "      <string>PM Evaluation</string>\n" +
                                "      <string-array>\n" +
                                "        <string>PM</string>\n" +
                                "      </string-array>\n" +
                                "    </entry>\n" +
                                "    <entry>\n" +
                                "      <string>HR Evaluation</string>\n" +
                                "      <string-array>\n" +
                                "        <string>HR</string>\n" +
                                "      </string-array>\n" +
                                "    </entry>\n" +
                                "    <entry>\n" +
                                "      <string>Self Evaluation</string>\n" +
                                "      <string-array>\n" +
                                "        <string>#{employee}</string>\n" +
                                "      </string-array>\n" +
                                "    </entry>\n" +
                                "  </associatedEntities>\n" +
                                "  <serviceTasks/>\n" +
                                "  <processVariables>\n" +
                                "    <entry>\n" +
                                "      <string>reason</string>\n" +
                                "      <string>java.lang.String</string>\n" +
                                "    </entry>\n" +
                                "    <entry>\n" +
                                "      <string>performance</string>\n" +
                                "      <string>java.lang.String</string>\n" +
                                "    </entry>\n" +
                                "    <entry>\n" +
                                "      <string>employee</string>\n" +
                                "      <string>java.lang.String</string>\n" +
                                "    </entry>\n" +
                                "  </processVariables>\n" +
                                "  <reusableSubProcesses class=\"set\"/>\n" +
                                "</org.kie.server.api.model.definition.ProcessDefinition>")));

        stubFor(post(urlEqualTo("/containers/evaluationproject/processes/evaluation/instances"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<long>2</long>")));

        stubFor(get(urlPathEqualTo("/queries/tasks/instances"))
                .withQueryParam("page", equalTo("0"))
                .withQueryParam("pageSize", equalTo("10"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<org.kie.server.api.model.instance.TaskSummaryList>\n" +
                                "<tasks>\n" +
                                "<org.kie.server.api.model.instance.TaskSummary>\n" +
                                "<id>4</id>\n" +
                                "<name>Self Evaluation</name>\n" +
                                "<description>Please perform a self-evalutation.</description>\n" +
                                "<status>Completed</status>\n" +
                                "<priority>0</priority>\n" +
                                "<actualOwner>maciek</actualOwner>\n" +
                                "<createdBy>maciek</createdBy>\n" +
                                "<createdOn class=\"sql-timestamp\">2015-08-03 11:34:17.774</createdOn>\n" +
                                "<activationTime class=\"sql-timestamp\">2015-08-03 11:34:17.774</activationTime>\n" +
                                "<processInstanceId>2</processInstanceId>\n" +
                                "<processId>evaluation</processId>\n" +
                                "<containerId>evaluationproject</containerId>\n" +
                                "<parentId>-1</parentId>\n" +
                                "</org.kie.server.api.model.instance.TaskSummary>\n" +
                                "</tasks>\n" +
                                "</org.kie.server.api.model.instance.TaskSummaryList>")));

        stubFor(put(urlEqualTo("/containers/evaluationproject/tasks/4/states/started"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("")));

        stubFor(put(urlEqualTo("/containers/evaluationproject/tasks/4/states/completed"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("")));

        stubFor(post(urlEqualTo("/containers/instances/evaluationproject"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<org.kie.server.api.model.ServiceResponse>\n" +
                                "  <type>SUCCESS</type>\n" +
                                "  <msg>Container evaluationproject successfully called.</msg>\n" +
                                "  <result class=\"string\">&lt;execution-results&gt;\n" +
                                "  &lt;result identifier=&quot;person&quot;&gt;\n" +
                                "    &lt;string&gt;john&lt;/string&gt;\n" +
                                "  &lt;/result&gt;\n" +
                                "  &lt;fact-handle identifier=&quot;person&quot; external-form=&quot;0:4:2135026172:3267851:4:DEFAULT:NON_TRAIT:java.lang.String&quot;/&gt;\n" +
                                "&lt;/execution-results&gt;</result>\n" +
                                "</org.kie.server.api.model.ServiceResponse>")));

        stubFor(delete(urlEqualTo("/containers/evaluationproject/processes/instances/2"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(204)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("")));

        stubFor(get(urlEqualTo("/queries/processes/instances/2"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/xml")
                        .withBody("<org.kie.server.api.model.instance.ProcessInstance>\n" +
                                "  <id>4</id>\n" +
                                "  <processId>evaluation</processId>\n" +
                                "  <processName>Evaluation</processName>\n" +
                                "  <processVersion>1</processVersion>\n" +
                                "  <state>3</state>\n" +
                                "  <containerId>evaluationproject</containerId>\n" +
                                "  <initiator>maciek</initiator>\n" +
                                "  <date class=\"sql-timestamp\">2015-08-03 12:09:56.79</date>\n" +
                                "  <processInstanceDescription>Evaluation</processInstanceDescription>\n" +
                                "  <correlationKey></correlationKey>\n" +
                                "  <parentId>-1</parentId>\n" +
                                "</org.kie.server.api.model.instance.ProcessInstance>")));
    }

}
