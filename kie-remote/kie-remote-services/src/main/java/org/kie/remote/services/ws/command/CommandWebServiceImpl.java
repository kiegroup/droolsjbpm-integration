package org.kie.remote.services.ws.command;

import javax.jws.WebService;

import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.remote.services.ws.command.generated.CommandWebServiceException;
import org.kie.services.shared.ServicesVersion;

@WebService(
        serviceName = "CommandService", 
        portName = "CommandServiceClient", 
        name = "CommandService", 
        targetNamespace = CommandWebServiceImpl.NAMESPACE)
public class CommandWebServiceImpl extends ResourceBase implements CommandWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    
    @Override
    public JaxbCommandsResponse execute( JaxbCommandsRequest request ) throws CommandWebServiceException {
        return restProcessJaxbCommandsRequest(request);
    } 

}
