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

import static org.assertj.core.api.Assertions.*;
import static org.kie.server.api.rest.RestURI.ABORT_PROCESS_INST_DEL_URI;
import static org.kie.server.api.rest.RestURI.PROCESS_INSTANCES_BY_CONTAINER_GET_URI;
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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.assertj.core.api.Assertions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;


public class ProcessServiceRestOnlyIntegrationTest extends RestJbpmBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    protected static final String CONTAINER_ID = "definition-project";
    protected static final String NON_EXISTING_CONTAINER_ID = "non-existing-container";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");

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
            assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

            Long result = response.readEntity(JaxbLong.class).unwrap();
            assertThat(result).isNotNull();
            response.close();

            // abort process instance
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, result);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap));
            logger.debug( "[DELETE] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).delete();
            assertThat(response.getStatus()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
            response.close();

            // abort process instance again
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + ABORT_PROCESS_INST_DEL_URI, valuesMap));
            logger.debug( "[DELETE] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).delete();
            assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
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
            assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

            Long pid = response.readEntity(JaxbLong.class).unwrap();
            assertThat(pid).isNotNull();
            response.close();

            // find process instances which are deployed in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCES_BY_CONTAINER_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, Thread.currentThread().getContextClassLoader());
            ProcessInstanceList processInstanceList = marshaller.unmarshall(response.readEntity(String.class), ProcessInstanceList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(processInstanceList.getItems()).hasSize(1);
            response.close();

            // find process instances of non-existing container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, NON_EXISTING_CONTAINER_ID);

            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCES_BY_CONTAINER_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).get();
            processInstanceList = marshaller.unmarshall(response.readEntity(String.class), ProcessInstanceList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(processInstanceList.getItems()).isEmpty();
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
            assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

            Long pid = response.readEntity(JaxbLong.class).unwrap();
            assertThat(pid).isNotNull();
            response.close();

            // find process instance variables which belong to a process in a deployed container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, pid);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_INSTANCES_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, Thread.currentThread().getContextClassLoader());
            VariableInstanceList variableInstanceList = marshaller.unmarshall(response.readEntity(String.class), VariableInstanceList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(variableInstanceList.getItems()).hasSize(1);
            response.close();

            // find process instance variables of non-existing process instance
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "-1");

            
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_INSTANCES_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).get();
            variableInstanceList = marshaller.unmarshall(response.readEntity(String.class), VariableInstanceList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(variableInstanceList.getItems()).isEmpty();
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
            assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

            Long pid = response.readEntity(JaxbLong.class).unwrap();
            assertThat(pid).isNotNull();
            response.close();

            // find process instance variable which belongs to a process instance in a deployed container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, pid);
            valuesMap.put(VAR_NAME, "stringData");
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_INSTANCE_BY_VAR_NAME_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, Thread.currentThread().getContextClassLoader());
            VariableInstanceList variableInstanceList = marshaller.unmarshall(response.readEntity(String.class), VariableInstanceList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(variableInstanceList.getItems()).isEmpty();
            response.close();

            // find process instance variable which belongs to a non-existing process instance
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "-1");
            valuesMap.put(VAR_NAME, "stringData");
            
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_VAR_INSTANCE_BY_VAR_NAME_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            variableInstanceList = marshaller.unmarshall(response.readEntity(String.class), VariableInstanceList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(variableInstanceList.getItems()).isEmpty();
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

        Response response = null;
        try {
            // find process definitions which belong to the given container
            WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).get();
            Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, Thread.currentThread().getContextClassLoader());
            ProcessDefinitionList processDefinitionList = marshaller.unmarshall(response.readEntity(String.class), ProcessDefinitionList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(processDefinitionList.getItems()).hasSize(10); //we didn't specify pageSize, so it will be 10 by default
            response.close();

            // find process definitions of non-existing container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, NON_EXISTING_CONTAINER_ID);
            
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).get();
            processDefinitionList = marshaller.unmarshall(response.readEntity(String.class), ProcessDefinitionList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(processDefinitionList.getItems()).isEmpty();
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
            assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

            Long pid = response.readEntity(JaxbLong.class).unwrap();
            assertThat(pid).isNotNull();
            response.close();

            // find node instances of process instance which is deployed in the given container
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, pid);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCES_NODE_INSTANCES_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            Marshaller marshaller = MarshallerFactory.getMarshaller(marshallingFormat, Thread.currentThread().getContextClassLoader());
            NodeInstanceList nodeInstanceList = marshaller.unmarshall(response.readEntity(String.class), NodeInstanceList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(nodeInstanceList.getItems()).hasSize(3);
            response.close();

            // find node instances of non-existing process instance
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "-1");
            
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCES_NODE_INSTANCES_GET_URI, valuesMap));
            logger.debug( "[GET] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).get();
            nodeInstanceList = marshaller.unmarshall(response.readEntity(String.class), NodeInstanceList.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            assertThat(nodeInstanceList.getItems()).isEmpty();
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
            assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

            Long pid = response.readEntity(JaxbLong.class).unwrap();
            assertThat(pid).isNotNull();
            response.close();

            // abort work item which does not exist
            valuesMap.clear();
            valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
            valuesMap.put(PROCESS_INST_ID, "99999");
            valuesMap.put(WORK_ITEM_ID, 1);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + PROCESS_INSTANCE_WORK_ITEM_ABORT_PUT_URI, valuesMap));
            logger.debug( "[PUT] " + clientRequest.getUri());

            response = clientRequest.request(getMediaType()).put(createEntity(""));
            String message = response.readEntity(String.class);

            assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
            assertThat(message).contains(String.format("Could not find work item instance with id \"%s\"", "1"));
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }
}