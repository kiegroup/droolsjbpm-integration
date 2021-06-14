/*
 * Copyright 2014 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.drools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DroolsMetricIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static Logger logger = LoggerFactory.getLogger(DroolsMetricIntegrationTest.class);

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar",
            "1.0.0");

    private static final String CONTAINER_ID = "stateless-kjar1-metric";
    private static final String CONTAINER_ID_WITHOUT_LOOKUP = "stateless-kjar1-metric-without-lookup";

    private static final String KIE_SESSION = "kbase1.stateless";
    private static final String KIE_SESSION_WITHOUT_LOOKUP = "default";

    private static final String PERSON_OUT_IDENTIFIER = "person1";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_NAME = "Darth";
    private static final String PERSON_SURNAME_FIELD = "surname";
    private static final String PERSON_EXPECTED_SURNAME = "Vader";

    private static ClassLoader kjarClassLoader;

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/stateless-session-kjar");

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();

        createContainer(CONTAINER_ID, releaseId);
        createContainer(CONTAINER_ID_WITHOUT_LOOKUP, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kjarClassLoader));
    }

    private static Client httpClient;

    protected WebTarget newRequest(String uriString) {
        if (httpClient == null) {
            httpClient = new ResteasyClientBuilder()
                    .establishConnectionTimeout(10, TimeUnit.SECONDS)
                    .socketTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        WebTarget webTarget = httpClient.target(uriString);
        webTarget.register(new BasicAuthentication(TestConfig.getUsername(), TestConfig.getPassword()));
        return webTarget;
    }

    @Test
    public void testStatelessCall() {
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        ExecutionResults actualData = reply.getResult();
        Object value = actualData.getValue(PERSON_OUT_IDENTIFIER);

        assertEquals("Expected surname to be set to 'Vader'",
                PERSON_EXPECTED_SURNAME, KieServerReflections.valueOf(value, PERSON_SURNAME_FIELD));

        Response response = null;
        try {
            String uriString = TestConfig.getKieServerHttpUrl().replaceAll("/server", "") + "/metrics";

            WebTarget clientRequest = newRequest(uriString);

            response = clientRequest.request(MediaType.TEXT_PLAIN_TYPE).get();

            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String res = response.readEntity(String.class);
            logger.debug("response: " + res);
            Assert.assertThat(res, not(isEmptyOrNullString()));
            assertThat(res).contains(
                "drl_match_fired_nanosecond_bucket",
                "drl_match_fired_nanosecond_count",
                "drl_match_fired_nanosecond_sum",
                "ksessionId=\"" + KIE_SESSION + "\""
            );

            int firstCount = getCount(res, CONTAINER_ID, KIE_SESSION);
            response.close();

            // One more time
            ruleClient.executeCommandsWithResults(CONTAINER_ID, executionCommand);
            clientRequest = newRequest(uriString);
            response = clientRequest.request(MediaType.TEXT_PLAIN_TYPE).get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            res = response.readEntity(String.class);
            logger.debug("response: " + res);
            assertEquals(firstCount + 1, getCount(res, CONTAINER_ID, KIE_SESSION));
            response.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private int getCount(String res, String containerId, String ksessionId) {
        Optional<String> opt = Arrays.stream(res.split("\n"))
                .filter(s -> s.contains("drl_match_fired_nanosecond_count") &&
                        s.contains("container_id=\"" + containerId + "\"") &&
                        s.contains("ksessionId=\"" + ksessionId + "\""))
                .findFirst();
        assertTrue(opt.isPresent());
        String line = opt.get();
        String count = line.substring(line.lastIndexOf(" "));
        return (int)Double.parseDouble(count);
    }

    @Test
    public void testWithoutLookup() {
        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommand executionCommand = commandsFactory.newBatchExecution(commands);

        Object person = createInstance(PERSON_CLASS_NAME, PERSON_NAME, "");
        commands.add(commandsFactory.newInsert(person, PERSON_OUT_IDENTIFIER));

        ServiceResponse<ExecutionResults> reply = ruleClient.executeCommandsWithResults(CONTAINER_ID_WITHOUT_LOOKUP, executionCommand);
        assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        ExecutionResults actualData = reply.getResult();
        Object value = actualData.getValue(PERSON_OUT_IDENTIFIER);

        assertEquals("Expected surname to be set to 'Vader'",
                PERSON_EXPECTED_SURNAME, KieServerReflections.valueOf(value, PERSON_SURNAME_FIELD));

        Response response = null;
        try {
            String uriString = TestConfig.getKieServerHttpUrl().replaceAll("/server", "") + "/metrics";

            WebTarget clientRequest = newRequest(uriString);

            response = clientRequest.request(MediaType.TEXT_PLAIN_TYPE).get();

            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String res = response.readEntity(String.class);
            logger.debug("response: " + res);
            Assert.assertThat(res, not(isEmptyOrNullString()));
            assertThat(res).contains(
                "drl_match_fired_nanosecond_bucket",
                "drl_match_fired_nanosecond_count",
                "drl_match_fired_nanosecond_sum",
                "ksessionId=\"" + KIE_SESSION_WITHOUT_LOOKUP + "\""
            );

            int firstCount = getCount(res, CONTAINER_ID_WITHOUT_LOOKUP, KIE_SESSION_WITHOUT_LOOKUP);
            response.close();

            // One more time
            ruleClient.executeCommandsWithResults(CONTAINER_ID_WITHOUT_LOOKUP, executionCommand);
            clientRequest = newRequest(uriString);
            response = clientRequest.request(MediaType.TEXT_PLAIN_TYPE).get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            res = response.readEntity(String.class);
            logger.debug("response: " + res);
            assertEquals(firstCount + 1, getCount(res, CONTAINER_ID_WITHOUT_LOOKUP, KIE_SESSION_WITHOUT_LOOKUP));
            response.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }
}
