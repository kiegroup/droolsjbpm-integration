/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/


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
