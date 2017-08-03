/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.extension.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.basetests.RestOnlyBaseIntegrationTest;

public class CustomExtensionIntegrationTest extends RestOnlyBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "stateless-session-kjar", "1.0.0-SNAPSHOT");

    private static final String CONTAINER_ID = "stateless-kjar1";
    private static final String KIE_SESSION = "stateless";

    private static final String PERSON_CLASS_NAME = "org.kie.server.testing.Person";
    private static final String PERSON_SURNAME_FIELD = "surname";

    private static final String JOHN_NAME = "john";
    private static final String JOHN_SURNAME = "doe";
    private static final String JOHN_EXPECTED_SURNAME = "smith";
    private static final String JOHN_OUT_IDENTIFIER = "john doe";
    private static final String MARY_NAME = "mary";
    private static final String MARY_SURNAME = "doe";
    private static final String MARY_OUT_IDENTIFIER = "mary doe";

    private static ClassLoader kjarClassLoader;

    @BeforeClass
    public static void deployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/stateless-session-kjar").getFile());

        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();

        KieServerBaseIntegrationTest.createContainer(CONTAINER_ID, releaseId);
    }

    @AfterClass
    public static void disposeContainers() {
        disposeAllContainers();
    }

    @Test
    public void testStatelessCall() throws Exception {
        Set<Class<?>> kjarClasses = new HashSet<>(Arrays.asList(kjarClassLoader.loadClass(PERSON_CLASS_NAME)));
        Marshaller marshaller = MarshallerFactory.getMarshaller(kjarClasses, marshallingFormat, kjarClassLoader);

        Object john = createPersonInstance(JOHN_NAME, JOHN_SURNAME, kjarClassLoader);
        Object mary = createPersonInstance(MARY_NAME, MARY_SURNAME, kjarClassLoader);
        List<Object> persons = new ArrayList<>(Arrays.asList(john, mary));

        String requestBody = marshaller.marshall(persons);

        Entity<?> entity = Entity.entity(requestBody, getMediaType());
        WebTarget clientRequest = newRequest(getCustomEndpointUrl());
        Response response = clientRequest.request(getMediaType())
                .header(KieServerConstants.KIE_CONTENT_TYPE_HEADER, marshallingFormat)
                .post(entity);

        String marshalledResponse = response.readEntity(String.class);
        ExecutionResults result = marshaller.unmarshall(marshalledResponse, ExecutionResultImpl.class);

        Object john2 = result.getValue(JOHN_OUT_IDENTIFIER);
        Assertions.assertThat(KieServerReflections.valueOf(john2, PERSON_SURNAME_FIELD)).isEqualTo(JOHN_EXPECTED_SURNAME);
        Object mary2 = result.getValue(MARY_OUT_IDENTIFIER);
        Assertions.assertThat(KieServerReflections.valueOf(mary2, PERSON_SURNAME_FIELD)).isEqualTo(MARY_SURNAME);
    }

    private String getCustomEndpointUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append(TestConfig.getKieServerHttpUrl());
        sb.append("/containers/instances/");
        sb.append(CONTAINER_ID);
        sb.append("/ksession/");
        sb.append(KIE_SESSION);
        return sb.toString();
    }

    private Object createPersonInstance(String firstname, String surname, ClassLoader loader) {
        return KieServerReflections.createInstance(PERSON_CLASS_NAME, loader, firstname, surname);
    }
}
