/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
