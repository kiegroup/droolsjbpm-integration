package org.kie.remote.services.ws.sei.command;

import javax.jws.WebService;

import org.kie.remote.services.ws.sei.ServicesVersion;

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
    public WebServiceCommandsResponse execute(WebServiceCommandsRequest arg0) throws CommandWebServiceException {
        return null;
    }

}