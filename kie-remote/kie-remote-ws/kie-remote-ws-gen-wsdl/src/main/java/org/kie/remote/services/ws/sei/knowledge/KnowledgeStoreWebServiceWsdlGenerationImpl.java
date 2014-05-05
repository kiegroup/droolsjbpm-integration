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