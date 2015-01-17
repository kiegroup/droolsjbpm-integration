package org.kie.remote.services.ws.command;

import javax.enterprise.context.RequestScoped;
import javax.jws.WebParam;
import javax.jws.WebService;

import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.ws.command.generated.CommandWebService;
import org.kie.remote.services.ws.command.generated.CommandWebServiceException;
import org.kie.services.shared.ServicesVersion;

@WebService(
        portName="CommandServiceBasicAuthPort",
        serviceName = "CommandServiceBasicAuth", 
        wsdlLocation="wsdl/CommandService.wsdl",
        targetNamespace = CommandWebServiceImpl.NAMESPACE,
        endpointInterface = "org.kie.remote.services.ws.command.generated.CommandWebService"
        )
@RequestScoped
public class CommandWebServiceImpl extends ResourceBase implements CommandWebService {

    public static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
  
    @Override
    public JaxbCommandsResponse execute( @WebParam(name = "request") JaxbCommandsRequest request ) throws CommandWebServiceException {
        return restProcessJaxbCommandsRequest(request);
    } 

}
