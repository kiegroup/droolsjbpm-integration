package org.kie.remote.services.ws.sei.command;

import javax.jws.WebService;

import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.ws.sei.ServicesVersion;

/**
 * Only used for initial WSDL generation
 */
@WebService(
        serviceName = "CommandService", 
        portName = "CommandServiceClient", 
        name = "CommandService", 
        targetNamespace = CommandWebService.NAMESPACE)
public class CommandWebServiceWsdlGenerationImpl implements CommandWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    
    @Override
    public JaxbCommandsResponse execute(JaxbCommandsRequest arg0) throws CommandWebServiceException {
        return null;
    }

}