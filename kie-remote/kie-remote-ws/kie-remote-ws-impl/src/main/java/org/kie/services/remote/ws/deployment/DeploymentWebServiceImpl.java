package org.kie.services.remote.ws.deployment;

import javax.jws.WebService;

import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.ws.deployment.generated.DeploymentIdRequest;
import org.kie.remote.services.ws.deployment.generated.DeploymentInfoResponse;
import org.kie.remote.services.ws.deployment.generated.DeploymentWebService;
import org.kie.remote.services.ws.deployment.generated.DeploymentWebServiceException;
import org.kie.remote.services.ws.deployment.generated.ProcessIdsResponse;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
import org.kie.services.shared.ServicesVersion;


@WebService(
        serviceName = "DeploymentService", 
        portName = "DeploymentServicePort", 
        name = "DeploymentService", 
        endpointInterface = "org.kie.remote.services.ws.deployment.generated.DeploymentWebService",
        targetNamespace = DeploymentWebServiceImpl.NAMESPACE)
public class DeploymentWebServiceImpl extends ResourceBase implements DeploymentWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";

    @Override
    public DeploymentInfoResponse manage( DeploymentIdRequest arg0 ) throws DeploymentWebServiceException {
        // DBG Auto-generated method stub
        return null;
    }

    @Override
    public ProcessIdsResponse getProcessDefinitionIds( DeploymentIdRequest arg0 ) throws DeploymentWebServiceException {
        // DBG Auto-generated method stub
        return null;
    }

    @Override
    public JaxbProcessDefinitionList getProcessDefinitionInfo( DeploymentIdRequest arg0 ) throws DeploymentWebServiceException {
        // DBG Auto-generated method stub
        return null;
    }

    @Override
    public JaxbDeploymentUnitList getDeploymentInfo( DeploymentIdRequest arg0 ) throws DeploymentWebServiceException {
        // DBG Auto-generated method stub
        return null;
    }
    
}
