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

package org.kie.services.remote.ws.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.jws.WebService;

import org.jbpm.services.api.RuntimeDataService;
import org.kie.internal.query.QueryContext;
import org.kie.remote.services.rest.DeployResourceBase;
import org.kie.remote.services.rest.ResourceBase;
import org.kie.remote.services.ws.common.ExceptionType;
import org.kie.remote.services.ws.common.WebServiceFaultInfo;
import org.kie.remote.services.ws.deployment.generated.DeploymentIdRequest;
import org.kie.remote.services.ws.deployment.generated.DeploymentInfoResponse;
import org.kie.remote.services.ws.deployment.generated.DeploymentWebService;
import org.kie.remote.services.ws.deployment.generated.DeploymentWebServiceException;
import org.kie.remote.services.ws.deployment.generated.ProcessIdsResponse;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
import org.kie.services.remote.ws.PaginationUtil;
import org.kie.services.shared.ServicesVersion;

// TODO: validation of input
// TODO: catchinge and translating exceptions

@WebService(
        serviceName = "DeploymentService", 
        portName = "DeploymentServicePort", 
        name = "DeploymentService", 
        endpointInterface = "org.kie.remote.services.ws.deployment.generated.DeploymentWebService",
        targetNamespace = DeploymentWebServiceImpl.NAMESPACE)
public class DeploymentWebServiceImpl extends ResourceBase implements DeploymentWebService {

    static final String NAMESPACE = "http://services.remote.kie.org/" + ServicesVersion.VERSION + "/command";

    @Inject 
    private DeployResourceBase deployBase;
   
    @Inject
    private RuntimeDataService runtimeDataService;
    
    @Override
    public DeploymentInfoResponse manage( DeploymentIdRequest request ) throws DeploymentWebServiceException {
        String strategy = request.getStrategy() == null ? null : request.getStrategy().toString();
        String mergeMode = request.getMergeMode() == null ? null : request.getMergeMode().toString();
        
        JaxbDeploymentJobResult jobResult= null;
        JaxbDeploymentUnit jaxbDepUnit;
        switch( request.getOperation() ) { 
        case DEPLOY:
            jobResult = deployBase.submitDeployJob(request.getDeploymentId(), strategy, mergeMode, request.getDescriptor());
            jaxbDepUnit = jobResult.getDeploymentUnit();
            break;
        case UNDEPLOY:
            jobResult = deployBase.submitUndeployJob(request.getDeploymentId());
            jaxbDepUnit = jobResult.getDeploymentUnit();
            break;
        case GET_INFO:
            jaxbDepUnit = deployBase.determineStatus(request.getDeploymentId(), true);
            break;
        default:
            WebServiceFaultInfo faultInfo = new WebServiceFaultInfo();
            // TODO: faultInfo.setCorrelationId(?)
            faultInfo.setType(ExceptionType.VALIDATION);
            throw new DeploymentWebServiceException("Unknown operation type: " + request.getOperation(), faultInfo);
        }
        
        // TODO: check job result
        if( jobResult != null ) { 
            
        }
        
        DeploymentInfoResponse response = new DeploymentInfoResponse();
        response.setOperationRequested(request.getOperation()); 
        response.setDeploymentUnit(jaxbDepUnit);
        return response;
    }

    @Override
    public ProcessIdsResponse getProcessDefinitionIds( DeploymentIdRequest request ) throws DeploymentWebServiceException {
        int [] pageInfo = PaginationUtil.getPageInfo(request.getPageNumber(), request.getPageSize());
        String deploymentId = request.getDeploymentId();
        
        List<String> processIdList = null;
        try { 
            processIdList = new ArrayList<String>(runtimeDataService.getProcessIds(deploymentId, new QueryContext(pageInfo[0], pageInfo[1])));
            Collections.sort(processIdList);
        } catch( Exception e) { 
            // possibly because the deployment is being modified and not fully un/deployed.. (un/deploy*ing*) 
            logger.debug( "Unable to retrieve process ids for deployment '{}': {}", deploymentId, e.getMessage(), e);
        }
        
        ProcessIdsResponse response = new ProcessIdsResponse();
        response.setDeploymentId(deploymentId);
        if( processIdList != null ) { 
            response.getProcessId().addAll(processIdList);
        }
        return response;
    }

    @Override
    public JaxbProcessDefinitionList getProcessDefinitionInfo( DeploymentIdRequest request ) throws DeploymentWebServiceException {
        List<JaxbProcessDefinition> jaxbProcDefList = new ArrayList<JaxbProcessDefinition>();
        int [] pageInfo = PaginationUtil.getPageInfo(request.getPageNumber(), request.getPageSize());
        
        // retrieve info
        deployBase.fillProcessDefinitionList( request.getDeploymentId(), pageInfo, getMaxNumResultsNeeded(pageInfo), jaxbProcDefList);
       
        // pagination
        JaxbProcessDefinitionList resultList = paginateAndCreateResult(pageInfo, jaxbProcDefList, new JaxbProcessDefinitionList());
        return resultList;
    }

    @Override
    public JaxbDeploymentUnitList getDeploymentInfo( DeploymentIdRequest request ) throws DeploymentWebServiceException {
        int [] pageInfo = PaginationUtil.getPageInfo(request.getPageNumber(), request.getPageSize());
        int maxNumResults = getMaxNumResultsNeeded(pageInfo);
        JaxbDeploymentUnitList jaxbDepUnitList = deployBase.getDeploymentList(pageInfo, maxNumResults);
        return jaxbDepUnitList;
    }
    
}
