package org.kie.remote.services.ws.sei.command;

import javax.jws.WebService;

import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.shared.ServicesVersion;

/**
 * Only used for WSDL generation
 */
@WebService(
        serviceName = "CommandService", 
        portName = "CommandServicePort", 
        name = "CommandService", 
        targetNamespace = CommandWebService.NAMESPACE)
public class CommandWebServiceWsdlGenerationImpl implements CommandWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    
    @Override
    public JaxbCommandsResponse execute(JaxbCommandsRequest arg0) throws CommandWebServiceException {
        return null;
    }

}