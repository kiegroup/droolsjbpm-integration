package org.kie.remote.services.rest;

import static org.kie.remote.services.rest.DeploymentResource.convertKModuleDepUnitToJaxbDepUnit;

import java.util.ArrayList;
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
import javax.ws.rs.core.UriInfo;

import org.jbpm.kie.services.api.Kjar;
import org.jbpm.kie.services.api.RuntimeDataService;
import org.jbpm.kie.services.api.bpmn2.BPMN2DataService;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.kie.internal.deployment.DeployedUnit;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;

@Path("/deployment")
@RequestScoped
public class DeploymentsResource extends ResourceBase {

    @Context
    private HttpHeaders headers;
   
    @Context
    private UriInfo uriInfo;
    
    /* KIE resources */
   
    @Inject
    @Kjar
    private KModuleDeploymentService deploymentService;
   
    @Inject
    private DeploymentInfoBean deploymentInfoBean;
  
    @Inject
    private RuntimeDataService runtimeDataService;
   
    @Inject
    private BPMN2DataService bpmn2DataService;
    
    // REST operations -----------------------------------------------------------------------------------------------------------

    @GET
    // TODO: docs pagination
    public Response listDeployments() { 
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> params = getRequestParams(uriInfo);
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
        String oper = getRelativePath(uriInfo);
        Map<String, List<String>> params = getRequestParams(uriInfo);
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        List<String> deploymentIds = new ArrayList<String>(deploymentInfoBean.getDeploymentIds());
        Collections.sort(deploymentIds);
        
        JaxbProcessDefinitionList jaxbProcDefList = new JaxbProcessDefinitionList();
        List<JaxbProcessDefinition> procDefList = jaxbProcDefList.getProcessDefinitionList();
        DEPLOYMENTS: for( String deploymentId : deploymentIds ) {
            List<String> processIdList;
            try { 
                processIdList = new ArrayList<String>(runtimeDataService.getProcessIds(deploymentId));
                Collections.sort(processIdList);
            } catch( Exception e) { 
                // possibly because the deployment is being modified and not fully un/deployed.. (un/deploy*ing*) 
                logger.debug( "Unable to retrieve process ids for deployment '{}': {}", deploymentId, e.getMessage(), e);
                continue; 
            }
            for( String processId : processIdList ) { 
                ProcessAssetDesc processAssetDesc;
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
                    variables = bpmn2DataService.getProcessData(processId);
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
