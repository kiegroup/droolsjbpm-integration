package org.kie.remote.services.ws.command;

import javax.jws.WebService;

import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.ws.wsdl.generated.CommandWebService;
import org.kie.remote.services.ws.wsdl.generated.CommandWebServiceException;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.shared.ServicesVersion;


@WebService(
        serviceName = "CommandService", 
        portName = "CommandServicePort", 
        name = "CommandService", 
        endpointInterface = "org.kie.remote.services.ws.wsdl.generated.CommandWebService",
        targetNamespace = CommandWebServiceImpl.NAMESPACE)
public class CommandWebServiceImpl extends ResourceBase implements CommandWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    
    @Override
    public JaxbCommandsResponse execute(JaxbCommandsRequest request) throws CommandWebServiceException {
        return restProcessJaxbCommandsRequest(request);
    }

}
