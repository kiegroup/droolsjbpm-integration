/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
import java.util.Collection;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.exception.KieServicesHttpException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;

public class JaxbXMLExternalEntityIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0");

    private static final String CONTAINER_ID = "stateless-kjar1";
    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";

    private static ClassLoader kjarClassLoader;

    @Parameterized.Parameters(name = "{0} {1}")
    public static Collection<Object[]> data() {
        Collection<Object[]> parameterData = new ArrayList<>();
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();
        parameterData.addAll((Arrays.asList(new Object[][]{{MarshallingFormat.JAXB, restConfiguration}})));

        if (TestConfig.getRemotingUrl() != null && !TestConfig.skipJMS()) {
            KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
            parameterData.addAll(Arrays.asList(new Object[][]{{MarshallingFormat.JAXB, jmsConfiguration}}));
        }
        return parameterData;
    }

    @BeforeClass
    public static void deployArtifacts() {
        disposeAllContainers();
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/stateless-session-kjar");

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();

        createContainer(CONTAINER_ID, releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kjarClassLoader));
    }

    @Test
    public void testRawPayload() {
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                         "<!DOCTYPE foo\n" +
                         "[<!ENTITY xxe SYSTEM \"file:///etc/hostname\">]>\n" +
                         "<batch-execution lookup=\"kbase1.stateless\">\n" +
                         "    <insert out-identifier=\"person1\" return-object=\"true\" entry-point=\"DEFAULT\" disconnected=\"false\">\n" +
                         "        <object xsi:type=\"person\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                         "            <duplicated>false</duplicated>\n" +
                         "            <firstname>&xxe;</firstname>\n" +
                         "            <surname></surname>\n" +
                         "        </object>\n" +
                         "    </insert>\n" +
                         "</batch-execution>";

        if (configuration.isRest()) {
            assertThatThrownBy(() -> {
                ruleClient.executeCommandsWithResults(CONTAINER_ID, payload);
            }).isInstanceOf(KieServicesHttpException.class)
              .hasStackTraceContaining("Can't unmarshall input string");
        } else if (configuration.isJms()) {
            ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, payload);
            assertThat(response.getType()).isEqualTo(ServiceResponse.ResponseType.FAILURE);
        } else {
            fail("This test doesn't cover other transport");
        }
    }
}
