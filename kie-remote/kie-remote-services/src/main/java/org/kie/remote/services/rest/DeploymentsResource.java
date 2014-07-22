package org.kie.remote.services.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.QueryContextImpl;
import org.jbpm.services.cdi.Kjar;

import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;

import static org.kie.remote.services.rest.DeploymentResource.*;

@Path("/deployment")
@RequestScoped
public class DeploymentsResource extends ResourceBase {

    @Context
    private HttpHeaders headers;
   
    /* KIE information and processing */
   
    @Inject
    @Kjar
    private KModuleDeploymentService deploymentService;
   
    @Inject
    private DeploymentInfoBean deploymentInfoBean;
  
    @Inject
    private RuntimeDataService runtimeDataService;
   
    @Inject
    private DefinitionService bpmn2DataService;
    
    // REST operations -----------------------------------------------------------------------------------------------------------

    @GET
    // TODO: docs pagination
    public Response listDeployments() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        List<String> deploymentIds = new ArrayList<String>(deploymentInfoBean.getDeploymentIds());
        Collections.sort(deploymentIds);
        
        JaxbDeploymentUnitList jaxbDepUnitList = new JaxbDeploymentUnitList();
        List<JaxbDeploymentUnit> depUnitList = jaxbDepUnitList.getDeploymentUnitList();
        for( String deploymentId : deploymentIds ) { 
            DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
            if( deployedUnit != null ) { 
                JaxbDeploymentUnit jaxbDepUnit = convertKModuleDepUnitToJaxbDepUnit((KModuleDeploymentUnit) deployedUnit.getDeploymentUnit());
                jaxbDepUnit.setStatus(JaxbDeploymentStatus.DEPLOYED);
                depUnitList.add(jaxbDepUnit);
                if( depUnitList.size() == maxNumResults) { 
                    // pagination parameters indicate that no more than current list is needed
                    break;
                }
            }
        }
      
        JaxbDeploymentUnitList resultList = paginateAndCreateResult(pageInfo, depUnitList, new JaxbDeploymentUnitList());
        
        return createCorrectVariant(resultList, headers);
    }
   
    @GET
    @Path("/processes")
    // TODO: docs (+ pagination)
    public Response listProcessDefinitions() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        List<String> deploymentIds = new ArrayList<String>(deploymentInfoBean.getDeploymentIds());
        Collections.sort(deploymentIds);
        
        JaxbProcessDefinitionList jaxbProcDefList = new JaxbProcessDefinitionList();
        List<JaxbProcessDefinition> procDefList = jaxbProcDefList.getProcessDefinitionList();
        DEPLOYMENTS: for( String deploymentId : deploymentIds ) {
            List<String> processIdList;
            try { 
                processIdList = new ArrayList<String>(runtimeDataService.getProcessIds(deploymentId, new QueryContextImpl(pageInfo[0], pageInfo[1])));
                Collections.sort(processIdList);
            } catch( Exception e) { 
                // possibly because the deployment is being modified and not fully un/deployed.. (un/deploy*ing*) 
                logger.debug( "Unable to retrieve process ids for deployment '{}': {}", deploymentId, e.getMessage(), e);
                continue; 
            }
            for( String processId : processIdList ) { 
                ProcessDefinition processAssetDesc;
                try { 
                    processAssetDesc = runtimeDataService.getProcessesByDeploymentIdProcessId(deploymentId, processId); 
                    if( processAssetDesc == null ) { 
                       logger.error( "No process definition information available for process definition '{}' in deployment '{}'!", 
                               processId, deploymentId); 
                       continue;
                    }
                } catch( Exception e ) {
                    // possibly because the deployment is being modified and not fully un/deployed.. (un/deploy*ing*) 
                    logger.debug( "Unable to retrieve process definition for process '{}' in deployment '{}': {}", 
                            processId, deploymentId, e.getMessage(), e);
                    continue; 
                }
                JaxbProcessDefinition jaxbProcDef = convertProcAssetDescToJaxbProcDef(processAssetDesc);
                Map<String, String> variables; 
                try { 
                    variables = bpmn2DataService.getProcessVariables(deploymentId, processId);
                } catch( Exception e) { 
                    // possibly because the deployment is being modified and not fully un/deployed.. (un/deploy*ing*) 
                    logger.debug( "Unable to retrieve process definition data for process '{}' in deployment '{}': {}", 
                            processId, deploymentId, e.getMessage(), e);
                    continue; 
                }
                jaxbProcDef.setVariables(variables);
                procDefList.add(jaxbProcDef);
                
                if( procDefList.size() == maxNumResults) { 
                    // pagination parameters indicate that no more than current list is needed
                    break DEPLOYMENTS;
                }
            }
        }
       
        JaxbProcessDefinitionList resultList = paginateAndCreateResult(pageInfo, procDefList, new JaxbProcessDefinitionList());
        return createCorrectVariant(resultList, headers);
    }
}
