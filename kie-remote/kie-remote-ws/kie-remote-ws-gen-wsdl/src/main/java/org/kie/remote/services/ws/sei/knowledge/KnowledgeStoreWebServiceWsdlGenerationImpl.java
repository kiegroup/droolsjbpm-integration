/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.services.ws.sei.knowledge;

import javax.jws.WebService;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;

/**
 * Only used for WSDL generation
 */
@WebService(
        serviceName = "KnowledgeStoreService", 
        portName = "KnowledgeStoreServicePort", 
        name = "KnowledgeStoreService", 
        targetNamespace = KnowledgeStoreWebService.NAMESPACE)
public class KnowledgeStoreWebServiceWsdlGenerationImpl implements KnowledgeStoreWebService {

    @Override
    public RepositoryResponse getRepositories(RepositoriesRequest arg0) throws KieRemoteWebServiceException {
        return null;
    }

    @Override
    public void manageRepositories(RepositoryOperationRequest arg0) throws KieRemoteWebServiceException {
    }

    @Override
    public ProjectsResponse getProjects(ProjectsResponse arg0) throws KieRemoteWebServiceException {
        return null;
    }

    @Override
    public void manageProjects(ProjectOperationRequest arg0) throws KieRemoteWebServiceException {
    }
}