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

package org.kie.remote.services.ws.sei.deployment;

import javax.jws.WebService;

import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;

/**
 * Only used for initial WSDL generation
 */
@WebService(
        serviceName = "DeploymentService", 
        portName = "DeploymentServicePort", 
        name = "DeploymentService", 
        targetNamespace = DeploymentWebService.NAMESPACE)
public class DeploymentWebServiceWsdlGenerationImpl implements DeploymentWebService {

    @Override
    public DeploymentInfoResponse manage( DeploymentIdRequest request ) throws DeploymentWebServiceException {
        return null;
    }

    @Override
    public ProcessIdsResponse getProcessDefinitionIds( DeploymentIdRequest request ) throws DeploymentWebServiceException {
        return null;
    }

    @Override
    public JaxbProcessDefinitionList getProcessDefinitionInfo( DeploymentIdRequest request ) throws DeploymentWebServiceException {
        return null;
    }

    @Override
    public JaxbDeploymentUnitList getDeploymentInfo( DeploymentIdRequest request ) throws DeploymentWebServiceException {
        return null;
    }
    
}