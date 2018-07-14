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
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_PARENT_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_NODE_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_VAR_INSTANCES_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_VAR_INSTANCE_BY_VAR_NAME_GET_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INST_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_URI;
import static org.kie.server.api.rest.RestURI.START_PROCESS_POST_URI;
import static org.kie.server.api.rest.RestURI.VAR_NAME;
import static org.kie.server.api.rest.RestURI.WORK_ITEM_ID;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCE_GET_URI;
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
            response.close();

            // abort process instance
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, result);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap));
            logger.debug( "[DELETE] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).delete();
            assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
            response.close();

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
    
    @Test
    public void testProcessWhichBelongsToAContainer() {
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
            response.close();

            // find process instance which is deployed in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, result);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCES_BY_PARENT_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            response.close();

            // find process instance which doesn't exist in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "processIdNotFound");

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCES_BY_PARENT_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).get();
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }
    
    @Test
    public void testProcessVariablesWhichBelongsToAContainer() {
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
            response.close();

            // find process instance which is deployed in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, result);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_INSTANCES_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            response.close();

            // find process instance which doesn't exist in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "processIdNotFound");

            
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_INSTANCES_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).get();
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }
    
    @Test
    public void testProcessVariablesHistoryWhichBelongsToAContainer() {
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
            response.close();

            // find process instance which is deployed in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, result);
            valuesMap.put(VAR_NAME, "stringData");
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_INSTANCE_BY_VAR_NAME_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            response.close();

            // find process instance which doesn't exist in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "processIdNotFound");
            valuesMap.put(VAR_NAME, "stringData");
            
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_INSTANCE_BY_VAR_NAME_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }
    
    @Test
    public void testProcessDefinitionWhichBelongsToAContainer() {
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
            response.close();

            // find process instance which is deployed in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, result);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            response.close();

            // find process instance which doesn't exist in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "99999");
            
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).delete();
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }
    
    @Test
    public void testNodeInstancesWhichBelongsToAProcess() {
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
            response.close();

            // find process instance which is deployed in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, result);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCES_NODE_INSTANCES_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
            response.close();

            // find process instance which doesn't exist in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "processIdNotFound");            
            
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCES_NODE_INSTANCES_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }
    
    @Test
    public void testAbortWorkItemWhichBelongsToAProcess() {
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
            response.close();

                        // find process instance which doesn't exist in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "99999");
            valuesMap.put(WORK_ITEM_ID, 1);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI, valuesMap));
            logger.debug( "[PUT] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).put(createEntity(""));
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }
}