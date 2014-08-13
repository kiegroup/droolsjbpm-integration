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

import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.cdi.Kjar;
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
    // DOCS: (+ pagination)
    public Response listProcessDefinitions() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        List<String> deploymentIds = new ArrayList<String>(deploymentInfoBean.getDeploymentIds());
        Collections.sort(deploymentIds);
        
        JaxbProcessDefinitionList jaxbProcDefList = new JaxbProcessDefinitionList();
        List<JaxbProcessDefinition> procDefList = jaxbProcDefList.getProcessDefinitionList();
        for( String deploymentId : deploymentIds ) {
            fillProcessDefinitionList(deploymentId, pageInfo, maxNumResults, runtimeDataService, bpmn2DataService, procDefList);
                
            if( procDefList.size() == maxNumResults) { 
                // pagination parameters indicate that no more than current list is needed
                break;
            }
        }
       
        JaxbProcessDefinitionList resultList = paginateAndCreateResult(pageInfo, procDefList, new JaxbProcessDefinitionList());
        return createCorrectVariant(resultList, headers);
    }
}
