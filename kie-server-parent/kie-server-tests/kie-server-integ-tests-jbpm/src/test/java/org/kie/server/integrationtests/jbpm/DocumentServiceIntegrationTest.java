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

package org.kie.server.integrationtests.jbpm;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.document.Document;
import org.jbpm.document.service.impl.DocumentImpl;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.TaskSummary;
import org.kie.server.client.KieServicesException;
import org.kie.server.integrationtests.category.Smoke;

import static org.junit.Assert.*;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerUtil;

public class DocumentServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");

    private static final String PERSON_CLASS_NAME = "org.jbpm.data.Person";

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(CONTAINER_ID, releaseId);
    }

    private DocumentInstance document;
    private String content;
    private byte[] contentBytes;

    @Before
    public void createData() {

        KieServerUtil.deleteDocumentStorageFolder();

        content = "just text content";
        contentBytes = content.getBytes();

        document = DocumentInstance.builder()
                .name("first document")
                .size(contentBytes.length)
                .lastModified(new Date())
                .content(contentBytes)
                .build();
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
        extraClasses.put(DocumentImpl.class.getName(), DocumentImpl.class);
    }

    @Test
    @Category(Smoke.class)
    public void testCreateLoadDeleteDocument() throws Exception {

        String documentId = documentClient.createDocument(document);
        assertNotNull(documentId);

        DocumentInstance fromServer = documentClient.getDocument(documentId);
        assertEquals(documentId, fromServer.getIdentifier());
        assertDocumentInstances(document, fromServer, true);

        documentClient.deleteDocument(documentId);

        try {
            documentClient.getDocument(documentId);
            fail("Document with id " + documentId + " was deleted so should not be found anymore");
        } catch (KieServicesException e) {
            // expected
        }
    }


    @Test
    public void testCreateDocument() {
        String documentId = documentClient.createDocument(document);
        assertNotNull(documentId);

        DocumentInstance fromServer = documentClient.getDocument(documentId);
        assertEquals(documentId, fromServer.getIdentifier());
        assertDocumentInstances(document, fromServer, true);
    }

    @Test
    public void testCreateEmptyDocument() {
        content = "";
        contentBytes = content.getBytes();
        document = DocumentInstance.builder()
                .name("first document")
                .size(contentBytes.length)
                .lastModified(new Date())
                .content(contentBytes)
                .build();

        String documentId = documentClient.createDocument(document);
        assertNotNull(documentId);

        DocumentInstance fromServer = documentClient.getDocument(documentId);
        assertEquals(documentId, fromServer.getIdentifier());
        assertDocumentInstances(document, fromServer, true);
    }

    @Test(expected = KieServicesException.class)
    public void testGetNotExistingDocument() {

        documentClient.getDocument("not-existing");
    }

    @Test
    public void testUpdateDocument() {
        String documentId = documentClient.createDocument(document);
        assertNotNull(documentId);

        DocumentInstance fromServer = documentClient.getDocument(documentId);
        assertEquals(documentId, fromServer.getIdentifier());
        assertDocumentInstances(document, fromServer, true);


        String udpatedDoc = "here comes the update";
        byte[] updateDocBytes = udpatedDoc.getBytes();
        fromServer.setContent(updateDocBytes);
        fromServer.setSize(updateDocBytes.length);
        fromServer.setLastModified(new Date());

        documentClient.updateDocument(fromServer);

        DocumentInstance updatedFromServer = documentClient.getDocument(documentId);
        assertEquals(documentId, updatedFromServer.getIdentifier());
        assertDocumentInstances(fromServer, updatedFromServer, true);
    }

    @Test
    public void testDeleteDocument() throws Exception {

        String documentId = documentClient.createDocument(document);
        assertNotNull(documentId);

        DocumentInstance fromServer = documentClient.getDocument(documentId);
        assertNotNull(fromServer);

        documentClient.deleteDocument(documentId);

        try {
            documentClient.getDocument(documentId);
            fail("Document with id " + documentId + " was deleted so should not be found anymore");
        } catch (KieServicesException e) {
            // expected
        }
    }

    @Test(expected = KieServicesException.class)
    public void testDeleteNotExistingDocument() {

        documentClient.deleteDocument("not-existing");
    }

    @Test(expected = KieServicesException.class)
    public void testUpdateNotExistingDocument() {

        document.setIdentifier("not-existing");
        documentClient.updateDocument(document);
    }

    @Test
    public void testListDocuments() {
        List<DocumentInstance> docs = documentClient.listDocuments(0, 10);
        assertNotNull(docs);
        assertEquals(0, docs.size());

        String documentId = documentClient.createDocument(document);
        assertNotNull(documentId);

        docs = documentClient.listDocuments(0, 10);
        assertNotNull(docs);
        assertEquals(1, docs.size());

        DocumentInstance fromServer = docs.get(0);
        assertEquals(documentId, fromServer.getIdentifier());
        assertDocumentInstances(document, fromServer, false);
    }

    @Test
    public void testDocumentProcess() {

        DocumentImpl docToTranslate = new DocumentImpl();
        docToTranslate.setContent(document.getContent());
        docToTranslate.setLastModified(document.getLastModified());
        docToTranslate.setName(document.getName());
        docToTranslate.setSize(document.getSize());


        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("original_document", docToTranslate);

        Long processInstanceId = null;
        try {
            processInstanceId = processClient.startProcess("definition-project", "xyz-translations", parameters);

            assertNotNull(processInstanceId);
            assertTrue(processInstanceId.longValue() > 0);

            List<DocumentInstance> docs = documentClient.listDocuments(0, 10);
            assertNotNull(docs);
            assertEquals(1, docs.size());

            Object docVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "original_document");
            assertNotNull(docVar);
            assertTrue(docVar instanceof Document);

            Document doc = (Document) docVar;
            assertDocuments(docToTranslate, doc);

            List<TaskSummary> tasks = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            // review task
            long taskId = tasks.get(0).getId();

            taskClient.claimTask("definition-project", taskId, "yoda");
            taskClient.startTask("definition-project", taskId, "yoda");

            Map<String, Object> taskInputs = taskClient.getTaskInputContentByTaskId("definition-project", taskId);
            assertNotNull(taskInputs);
            assertEquals(6, taskInputs.size());

            docVar = taskInputs.get("in_doc");
            assertNotNull(docVar);
            assertTrue(docVar instanceof Document);

            doc = (Document) docVar;
            assertDocuments(docToTranslate, doc);

            Map<String, Object> result = new HashMap<String, Object>();
            result.put("out_comments", "ready to translate");
            result.put("out_status", "OK");

            taskClient.completeTask("definition-project", taskId, "yoda", result);

            tasks = taskClient.findTasksAssignedAsPotentialOwner("yoda", 0, 10);
            assertNotNull(tasks);
            assertEquals(1, tasks.size());
            // translate task
            taskId = tasks.get(0).getId();

            taskClient.claimTask("definition-project", taskId, "yoda");
            taskClient.startTask("definition-project", taskId, "yoda");

            taskInputs = taskClient.getTaskInputContentByTaskId("definition-project", taskId);
            assertNotNull(taskInputs);
            assertEquals(8, taskInputs.size());

            docVar = taskInputs.get("in_doc");
            assertNotNull(docVar);
            assertTrue(docVar instanceof Document);

            doc = (Document) docVar;
            assertDocuments(docToTranslate, doc);

            result = new HashMap<String, Object>();
            DocumentImpl translated = new DocumentImpl();
            translated.setContent("translated document content".getBytes());
            translated.setLastModified(new Date());
            translated.setName("translated document");
            translated.setSize(translated.getContent().length);
            result.put("out_translated", translated);
            result.put("out_comments", "translated");
            result.put("out_status", "DONE");

            taskClient.completeTask("definition-project", taskId, "yoda", result);

            // now lets check if the document was updated
            docVar = processClient.getProcessInstanceVariable("definition-project", processInstanceId, "translated_document");
            assertNotNull(docVar);
            assertTrue(docVar instanceof Document);

            doc = (Document) docVar;
            assertDocuments(translated, doc);

            docs = documentClient.listDocuments(0, 10);
            assertNotNull(docs);
            assertEquals(2, docs.size());
        } finally {
            if (processInstanceId != null) {
                processClient.abortProcessInstance("definition-project", processInstanceId);
            }
        }

    }

    private void assertDocumentInstances(DocumentInstance expected, DocumentInstance actual, boolean assertContent) {
        assertNotNull(actual);
        assertNotNull(actual.getIdentifier());
        assertNotNull(actual.getName());
        assertNotNull(actual.getLastModified());
        assertNotNull(actual.getSize());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getSize(), actual.getSize());
        if (assertContent) {
            assertEquals(new String(expected.getContent()), new String(actual.getContent()));
        }
    }

    private void assertDocuments(Document expected, Document actual) {
        assertNotNull(actual);
        assertNotNull(actual.getIdentifier());
        assertNotNull(actual.getName());
        assertNotNull(actual.getLastModified());
        assertNotNull(actual.getSize());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getSize(), actual.getSize());
        assertEquals(new String(expected.getContent()), new String(actual.getContent()));
    }
}
