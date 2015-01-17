package org.kie.remote.services.ws.command.test;

import javax.jws.WebService;

import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.ws.command.CommandServiceTest;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.remote.services.ws.command.generated.CommandWebServiceException;

@WebService(
        portName="CommandServiceBasicAuthPort",
        serviceName = "CommandServiceBasicAuth", 
        wsdlLocation="wsdl/CommandService.wsdl",
        targetNamespace = CommandServiceTest.NAMESPACE,
        endpointInterface = "org.kie.remote.services.ws.command.generated.CommandWebService"
        )
public class TestCommandBasicAuthImpl implements CommandWebService {

    @Override
    public JaxbCommandsResponse execute( JaxbCommandsRequest request ) throws CommandWebServiceException {
        JaxbCommandsResponse resp = new JaxbCommandsResponse(request);
        
        return resp;
    }

}
