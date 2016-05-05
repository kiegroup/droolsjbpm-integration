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

package org.kie.server.client.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.instance.DocumentInstance;
import org.kie.server.client.DocumentServicesClient;
import org.kie.server.client.KieServicesConfiguration;

import static org.kie.server.api.rest.RestURI.*;

public class DocumentServicesClientImpl extends AbstractKieServicesClientImpl implements DocumentServicesClient {

    public DocumentServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public DocumentServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public String getDocumentLink(String identifier) {
        return loadBalancer.getUrl() + "/" + DOCUMENT_URI + "/" + identifier + "/content" ;
    }

    @Override
    public DocumentInstance getDocument(String identifier) {
        DocumentInstance result = null;
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(DOCUMENT_ID, identifier);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), DOCUMENT_URI + "/" + DOCUMENT_INSTANCE_GET_URI, valuesMap), DocumentInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("DocumentService", "getDocument", new Object[]{identifier})) );
            ServiceResponse<DocumentInstance> response = (ServiceResponse<DocumentInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            result = response.getResult();
        }

        return result;
    }

    @Override
    public String createDocument(DocumentInstance documentInstance) {
        Object result = null;
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), DOCUMENT_URI, valuesMap), documentInstance, Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("DocumentService", "storeDocument", serialize(documentInstance), marshaller.getFormat().getType(), new Object[]{})) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            result = response.getResult();
        }
        if (result instanceof Wrapped) {
            return (String) ((Wrapped) result).unwrap();
        }
        return (String) result;
    }

    @Override
    public void updateDocument(DocumentInstance documentInstance) {
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(DOCUMENT_ID, documentInstance.getIdentifier());

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), DOCUMENT_URI + "/" + DOCUMENT_INSTANCE_PUT_URI, valuesMap), documentInstance, Object.class, new HashMap<String, String>());

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("DocumentService", "updateDocument", serialize(documentInstance), marshaller.getFormat().getType(), new Object[]{documentInstance.getIdentifier()})) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void deleteDocument(String identifier) {
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(DOCUMENT_ID, identifier);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), DOCUMENT_URI + "/" + DOCUMENT_INSTANCE_DELETE_URI, valuesMap), Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand("DocumentService", "deleteDocument", new Object[]{identifier})) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }
}
