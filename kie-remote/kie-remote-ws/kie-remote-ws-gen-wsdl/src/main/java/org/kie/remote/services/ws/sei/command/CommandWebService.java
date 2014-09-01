
package org.kie.remote.services.ws.sei.command;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kie.remote.client.jaxb.JaxbCommandsRequest;
import org.kie.remote.client.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.ws.sei.ServicesVersion;
import org.kie.remote.services.ws.sei.process.ProcessWebService;

/**
 * Only used for initial WSDL generation
 */
@WebService(serviceName = "CommandService", targetNamespace = ProcessWebService.NAMESPACE)
public interface CommandWebService {

    final static String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";
    
    @WebMethod(action = "urn:Execute")
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "executeRequest", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperCommandRequest")
    @ResponseWrapper(localName = "executeResponse", targetNamespace = NAMESPACE, className = "org.kie.remote.services.ws.wsdl.generated.WrapperCommandResponse")
    public JaxbCommandsResponse execute(@WebParam(name = "request", targetNamespace = "") JaxbCommandsRequest request) throws CommandWebServiceException;

}
