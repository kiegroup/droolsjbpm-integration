/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.kie.server.api.rest.RestURI.ABORT_PROCESS_INST_DEL_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INST_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_URI;
import static org.kie.server.api.rest.RestURI.START_PROCESS_POST_URI;
import static org.kie.server.api.rest.RestURI.build;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;


public class ProcessServiceRestOnlyIntegrationTest extends RestJbpmBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    protected static final String CONTAINER_ID = "definition-project";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        createContainer(CONTAINER_ID, releaseId);
    }

    @Test
    public void testAbortAlreadyAbortedProcess() {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.PROCESS_ID, PROCESS_ID_USERTASK);

        Response response = null;
        try {
            // start process instance
            WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + START_PROCESS_POST_URI, valuesMap));
            logger.debug("[POST] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).post(createEntity(""));
            Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

            Long result = response.readEntity(JaxbLong.class).unwrap();
            assertNotNull(result);

            // abort process instance
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, result);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap));
            logger.debug( "[DELETE] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).delete();
            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());

            // abort process instance again
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap));
            logger.debug( "[DELETE] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).delete();
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }
}