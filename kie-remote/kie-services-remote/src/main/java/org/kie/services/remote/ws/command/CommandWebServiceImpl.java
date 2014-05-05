package org.kie.services.remote.ws.command;

import javax.jws.WebService;

import org.kie.remote.ServicesVersion;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.remote.rest.ResourceBase;
import org.kie.services.remote.ws.wsdl.generated.CommandService;
import org.kie.services.remote.ws.wsdl.generated.CommandWebServiceException;

@WebService(
        serviceName = "CommandService", 
        portName = "CommandServicePort", 
        name = "CommandService", 
        targetNamespace = CommandWebServiceImpl.NAMESPACE)
public class CommandWebServiceImpl extends ResourceBase implements CommandService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    
    @Override
    public JaxbCommandsResponse execute(JaxbCommandsRequest request) throws CommandWebServiceException {
        return restProcessJaxbCommandsRequest(request);
    }

}
