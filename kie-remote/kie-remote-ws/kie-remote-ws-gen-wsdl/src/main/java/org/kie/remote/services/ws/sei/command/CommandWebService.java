
package org.kie.remote.services.ws.sei.command;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kie.remote.services.ws.sei.ServicesVersion;


/**
 * A simple service example.
 * 
 */
@WebService(name = "CommandService", targetNamespace = CommandWebService.NAMESPACE)
public interface CommandWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    
    @WebMethod(action = "urn:Execute")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "executeRequest", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperCommandRequest")
    @ResponseWrapper(localName = "executeResponse", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperCommandResponse")
    public WebServiceCommandsResponse execute(@WebParam(name = "arg0", targetNamespace = "") WebServiceCommandsRequest arg0) throws CommandWebServiceException;

}
