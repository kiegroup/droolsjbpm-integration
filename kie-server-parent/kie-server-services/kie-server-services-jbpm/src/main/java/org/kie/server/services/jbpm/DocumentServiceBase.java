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

package org.kie.server.services.jbpm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jbpm.document.Document;
import org.jbpm.document.service.DocumentStorageService;
import org.jbpm.document.service.DocumentStorageServiceProvider;
import org.jbpm.document.service.impl.DocumentStorageServiceImpl;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.api.model.instance.DocumentInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.KieServerRuntimeException;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocumentServiceBase {

    private static final Logger logger = LoggerFactory.getLogger(DocumentServiceBase.class);

    private DocumentStorageService documentStorageService = DocumentStorageServiceProvider.get().getStorageService();
    private MarshallerHelper marshallerHelper;

    public DocumentServiceBase(KieServerRegistry context) {
        this.marshallerHelper = new MarshallerHelper(context);
    }

    public DocumentServiceBase(DocumentStorageService documentStorageService, KieServerRegistry context) {
        this.documentStorageService = documentStorageService;
        this.marshallerHelper = new MarshallerHelper(context);
    }

    public DocumentInstance getDocument(String documentId) {
        logger.debug("About to load document with id {}", documentId);
        final Document document = documentStorageService.getDocument(documentId);
        logger.debug("Document loaded from repository {}", document);
        if (document == null) {
            throw new KieServerRuntimeException("No document found with id " + documentId);
        }
        return convertDocument(document, true);
    }

    public String storeDocument(String documentPayload, String marshallingType) {
        logger.debug("About to unmarshal document payload '{}' with marshaling type {}", documentPayload, marshallingType);
        DocumentInstance documentInstance = marshallerHelper.unmarshal(documentPayload, marshallingType, DocumentInstance.class);

        logger.debug("Document created from payload {}", documentInstance);
        Document document = documentStorageService.buildDocument(documentInstance.getName(), documentInstance.getSize(), documentInstance.getLastModified(), new HashMap<String, String>());
        logger.debug("Document created by the service {}", document);
        documentStorageService.saveDocument(document, documentInstance.getContent());

        logger.debug("Document {} stored successfully", document);

        return document.getIdentifier();
    }

    public void updateDocument(String documentId, String documentPayload, String marshallingType) {
        logger.debug("About to unmarshal document payload '{}' with marshaling type {}", documentPayload, marshallingType);
        DocumentInstance documentInstance = marshallerHelper.unmarshal(documentPayload, marshallingType, DocumentInstance.class);

        logger.debug("Document created from payload {}", documentInstance);
        Document document = documentStorageService.getDocument(documentId);
        logger.debug("Document found {}", documentInstance != null);
        if (document == null) {
            throw new KieServerRuntimeException("No document found with id " + documentId);
        }

        documentStorageService.saveDocument(document, documentInstance.getContent());
        logger.debug("Document {} updated successfully", document);
    }

    public void deleteDocument(String documentId) {
        logger.debug("About to delete document with id {}", documentId);
        Document document = documentStorageService.getDocument(documentId);
        logger.debug("Document found {}", document != null);
        if (document == null) {
            throw new KieServerRuntimeException("No document found with id " + documentId);
        }

        documentStorageService.deleteDocument(document);
        logger.debug("Document {} deleted successfully", document);
    }

    public DocumentInstanceList listDocuments(Integer page, Integer pageSize) {
        logger.debug("About to list documents with page {} and pageSize {}", page, pageSize);
        final List<Document> documents = documentStorageService.listDocuments(page, pageSize);
        logger.debug("Documents loaded from repository {}", documents);
        DocumentInstanceList result = new DocumentInstanceList(Collections.emptyList());
        if (documents == null) {
            return result;
        }
        List<DocumentInstance> list = convertDocumentList(documents);
        result.setDocumentInstances(list.toArray(new DocumentInstance[list.size()]));

        return result;
    }

    protected List<DocumentInstance> convertDocumentList(List<Document> documents) {

        List<DocumentInstance> list = new ArrayList<DocumentInstance>();
        for (Document doc : documents) {
            list.add(convertDocument(doc, false));
        }

        return list;
    }

    protected DocumentInstance convertDocument(Document document, boolean withContent) {
        if (document == null) {
            return null;
        }
        DocumentInstance documentInstance = DocumentInstance.builder()
                .id(document.getIdentifier())
                .name(document.getName())
                .link(document.getLink())
                .size(document.getSize())
                .lastModified(document.getLastModified())
                .content(document.getContent())
                .build();
        if (!withContent) {
            documentInstance.setContent(null);
        }
        return documentInstance;
    }
}
