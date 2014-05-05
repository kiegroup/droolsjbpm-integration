package org.kie.remote.services.ws.sei.deployment;

import javax.jws.WebService;

import org.kie.remote.services.ws.common.KieRemoteWebServiceException;

/**
 * Only used for WSDL generation
 */
@WebService(
        serviceName = "DeploymentService", 
        portName = "DeploymentServicePort", 
        name = "DeploymentService", 
        targetNamespace = DeploymentWebService.NAMESPACE)
public class DeploymentWebServiceWsdlGenerationImpl implements DeploymentWebService {

    @Override
    public DeploymentInfoResponse manage(DeploymentIdRequest arg0) throws KieRemoteWebServiceException {
        return null;
    }

    @Override
    public ProcessIdsResponse getProcessDefinitionIds(DeploymentIdRequest arg0) throws KieRemoteWebServiceException {
        return null;
    }

    @Override
    public ProcessDefinitionResponse getProcessDefinition(DeploymentIdRequest arg0) throws KieRemoteWebServiceException {
        return null;
    }

}