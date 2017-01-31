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

import static org.junit.Assert.*;
import static org.kie.server.api.rest.RestURI.*;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.DocumentInstanceList;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.model.type.JaxbString;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JbpmRestIntegrationTest extends RestJbpmBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "rest-processes", "1.0.0.Final");
   
    private static Logger logger = LoggerFactory.getLogger(JbpmRestIntegrationTest.class);

    private static Map<MarshallingFormat, String> acceptHeadersByFormat = new HashMap<MarshallingFormat, String>();

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/rest-processes").getFile());
        // set the accepted formats with quality param to express preference
        acceptHeadersByFormat.put(MarshallingFormat.JAXB, "application/xml;q=0.9,application/json;q=0.3");// xml is preferred over json
        acceptHeadersByFormat.put(MarshallingFormat.JSON, "application/json;q=0.9,application/xml;q=0.3");// json is preferred over xml

        createContainer(CONTAINER, releaseId);
    }

    /**
     * Process ids
     */
    
//    private static final String HUMAN_TASK_PROCESS_ID        = "org.test.kjar.writedocument";
//    private static final String HUMAN_TASK_VAR_PROCESS_ID    = "org.test.kjar.HumanTaskWithForm";
//    private static final String SCRIPT_TASK_PROCESS_ID       = "org.test.kjar.scripttask";
//    private static final String SCRIPT_TASK_VAR_PROCESS_ID   = "org.test.kjar.scripttask.var";
//    private static final String SINGLE_HUMAN_TASK_PROCESS_ID = "org.test.kjar.singleHumanTask";
//    private static final String OBJECT_VARIABLE_PROCESS_ID   = "org.test.kjar.ObjectVariableProcess";
//    private static final String RULE_TASK_PROCESS_ID         = "org.test.kjar.RuleTask";
//    private static final String TASK_CONTENT_PROCESS_ID      = "org.test.kjar.UserTask";
//    private static final String EVALUTAION_PROCESS_ID        = "org.test.kjar.evaluation";
//    private static final String GROUP_ASSSIGNMENT_PROCESS_ID = "org.test.kjar.GroupAssignmentHumanTask";
//    private static final String GROUP_ASSSIGN_VAR_PROCESS_ID = "org.test.kjar.groupAssign";
//    private static final String CLASSPATH_OBJECT_PROCESS_ID  = "org.test.kjar.classpath.process";
    
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
            response = clientRequest.request().post(createEntity(""));
            Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            Assert.assertEquals(getMediaType().toString(), response.getHeaders().getFirst("Content-Type"));

            JaxbLong pId = response.readEntity(JaxbLong.class);
            valuesMap.put(PROCESS_INST_ID, pId.unwrap());
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

    @Test
    public void testKieModuleMetaDataGetProcesses() throws Exception {

        final KieModuleMetaData kieModuleMetaData = KieModuleMetaData.Factory.newKieModuleMetaData( releaseId );

        assertNotNull(kieModuleMetaData);

        assertFalse( kieModuleMetaData.getProcesses().isEmpty() );
        assertTrue( kieModuleMetaData.getProcesses().containsKey("humanTaskWithOwnType.bpmn" ) );
    }

    @Test
    public void testBasicJbpmRequestWithSingleAcceptHeader() throws Exception {
        KieContainerResource resource = new KieContainerResource(CONTAINER, releaseId);

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(CONTAINER_ID, resource.getContainerId());
        valuesMap.put(PROCESS_ID, HUMAN_TASK_OWN_TYPE_ID);

        Response response = null;
        try {
            WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + START_PROCESS_POST_URI, valuesMap));
            logger.info( "[POST] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).post(createEntity(""));
            Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            Assert.assertEquals(getMediaType().toString(), response.getHeaders().getFirst("Content-Type"));

            JaxbLong pId = response.readEntity(JaxbLong.class);
            valuesMap.put(PROCESS_INST_ID, pId.unwrap());
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

    @Test
    public void testBasicJbpmRequestManyAcceptHeaders() throws Exception {
        KieContainerResource resource = new KieContainerResource(CONTAINER, releaseId);

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(CONTAINER_ID, resource.getContainerId());
        valuesMap.put(PROCESS_ID, HUMAN_TASK_OWN_TYPE_ID);

        Response response = null;
        try {
            WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), PROCESS_URI + "/" + START_PROCESS_POST_URI, valuesMap));
            logger.info( "[POST] " + clientRequest.getUri());
            response = clientRequest.request(acceptHeadersByFormat.get(marshallingFormat)).post(createEntity(""));
            Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
            Assert.assertEquals(getMediaType().toString(), response.getHeaders().getFirst("Content-Type"));

            JaxbLong pId = response.readEntity(JaxbLong.class);
            valuesMap.put(PROCESS_INST_ID, pId.unwrap());
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

    @Test
    public void testUploadListDownloadDocument() throws Exception {
        KieServerUtil.deleteDocumentStorageFolder();

        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<Class<?>>(extraClasses.values()), marshallingFormat, client.getClassLoader());

        DocumentInstance documentInstance = DocumentInstance.builder().name("test file.txt").size(50).content("test content".getBytes()).lastModified(new Date()).build();
        String documentEntity = marshaller.marshall(documentInstance);

        Map<String, Object> empty = new HashMap<>();
        Response response = null;
        try {
            // create document
            WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), DOCUMENT_URI, empty));
            logger.info( "[POST] " + clientRequest.getUri());
            response = clientRequest.request(acceptHeadersByFormat.get(marshallingFormat)).post(createEntity(documentEntity));
            Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

            String documentId = response.readEntity(JaxbString.class).unwrap();
            assertNotNull(documentId);

            // list available documents without paging info
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(DOCUMENT_ID, documentId);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), DOCUMENT_URI, valuesMap));
            logger.info( "[GET] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            DocumentInstanceList docList = marshaller.unmarshall(response.readEntity(String.class), DocumentInstanceList.class);
            assertNotNull(docList);

            List<DocumentInstance> docs = docList.getItems();
            assertNotNull(docs);
            assertEquals(1, docs.size());
            DocumentInstance doc = docs.get(0);
            assertNotNull(doc);
            assertEquals(documentInstance.getName(), doc.getName());
            assertEquals(documentId, doc.getIdentifier());

            // download document content
            valuesMap = new HashMap<String, Object>();
            valuesMap.put(DOCUMENT_ID, documentId);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), DOCUMENT_URI + "/" + DOCUMENT_INSTANCE_CONTENT_GET_URI, valuesMap));
            logger.info( "[GET] " + clientRequest.getUri());
            response = clientRequest.request(getMediaType()).accept(MediaType.APPLICATION_OCTET_STREAM_TYPE).get();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String contentDisposition = response.getHeaderString("Content-Disposition");
            assertTrue(contentDisposition.contains(documentInstance.getName()));

            byte[] content = response.readEntity(byte[].class);
            assertNotNull(content);
            String stringContent = new String(content);
            assertEquals("test content", stringContent);
            response.close();

            // delete document
            valuesMap = new HashMap<String, Object>();
            valuesMap.put(DOCUMENT_ID, documentId);
            clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), DOCUMENT_URI + "/" + DOCUMENT_INSTANCE_DELETE_URI, valuesMap));
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
