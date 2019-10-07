/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.rest.RestURI.ABORT_PROCESS_INST_DEL_URI;
import static org.kie.server.api.rest.RestURI.CONTAINER_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_INST_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_URI;
import static org.kie.server.api.rest.RestURI.START_PROCESS_POST_URI;
import static org.kie.server.api.rest.RestURI.build;


public class IdentifierJbpmRestIntegrationTest extends RestJbpmBaseIntegrationTest {


    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "rest-processes", "1.0.0.Final");
   
    private static Logger logger = LoggerFactory.getLogger(IdentifierJbpmRestIntegrationTest.class);


    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/rest-processes");

        createContainer(CONTAINER, releaseId);

    }


    private static final String CONTAINER = "rest-processes";
    private static final String HUMAN_TASK_OWN_TYPE_ID = "org.test.kjar.HumanTaskWithOwnType";


    @Test
    public void testBasicJbpmRequest() throws Exception {
        KieContainerResource resource = new KieContainerResource(CONTAINER, releaseId);

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(CONTAINER_ID, resource.getContainerId());
        valuesMap.put(PROCESS_ID, HUMAN_TASK_OWN_TYPE_ID);

        Response response = null;
        try {
            WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + START_PROCESS_POST_URI, valuesMap));
            logger.info( "[POST] " + clientRequest.getUri());
            // we set strict json parameter
            MediaType type = getMediaType();
            type = new MediaType(type.getType(), type.getSubtype(), Collections.singletonMap("strict", "true"));
            logger.info("media type : {}", type);
            
            response = clientRequest.request(type).post(Entity.entity("", type));
            Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            Assertions.assertThat((String)response.getHeaders().getFirst("Content-Type")).startsWith(getMediaType().toString());

            String serialized = response.readEntity(String.class);
            response.close();
            Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, Thread.currentThread().getContextClassLoader());
            Object wrapId = null;
            Object id = null;
            logger.info("Value returned {} {}", serialized, getMediaType());
            if (marshallingFormat.equals(MarshallingFormat.JAXB)) {
                wrapId = marshaller.unmarshall(serialized, Long.class);
                id = ((Long) wrapId);
            } else {
                try {
                    wrapId = new JSONObject(serialized);
                    id = ((JSONObject) wrapId).getString("value");
                } catch (JSONException ex) {
                    Assert.fail("expected json object from kie server " + ex.getMessage());
                }
            }

            assertNotNull("object not returned", wrapId);
            valuesMap.put(PROCESS_INST_ID, id);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap));
            logger.info( "[DELETE] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).delete();
            int noContentStatusCode = Response.Status.NO_CONTENT.getStatusCode();
            int okStatusCode = Response.Status.OK.getStatusCode();
            assertTrue("Wrong status code returned: " + response.getStatus(),
                    response.getStatus() == noContentStatusCode || response.getStatus() == okStatusCode);

        } finally {
            if(response != null) {
                response.close();
            }
        }

    }

}
