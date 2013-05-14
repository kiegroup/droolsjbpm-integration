package org.kie.services.remote.rest;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kie.services.client.message.serialization.impl.jaxb.JaxbOperation;
import org.kie.services.remote.ejb.ProcessRequestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/rest/runtime/{id: [a-zA-Z0-9-]+}")
@RequestScoped
public class RuntimeResource {

    private Logger logger = LoggerFactory.getLogger(RuntimeResource.class);

    @EJB
    protected ProcessRequestBean processRequestBean;
    
    @PathParam("id")
    private String deploymentId;

    // Helper data --------------------------------------------------------------------------------------------------------------
    
    @POST
    @Path("/process/{processId: [a-zA-Z0-9]+}/start")
    public void startNewProcess(@PathParam("processId") String processId, @PathParam("processDefId") String processDefId) { 
     
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/process/instance/{procInstId: [0-9]+}")
    public void getProcessInstanceDeatils(@PathParam("procInstId") Long procInstId) { 
    
    }
   
    @POST
    @Path("/process/instance/{procInstId: [0-9]+}/{oper: [a-zA-Z]+}")
    public void doProcessInstanceOperation(@PathParam("procInstId") Long procInstId, @PathParam("oper") String operation) { 
    
    }
    
    @POST
    @Path("/signal/{signal: [a-zA-Z0-9-]+}")
    public void signalEvent(@PathParam("signal") String signal) { 
    
    }

    @POST
    @Path("/workitem/{workItemId: [0-9-]+}/{oper: [a-zA-Z]+}")
    public void doWorkItemOperation(@PathParam("workItemId") Long workItemId, @PathParam("oper") String operation) { 
    
    }
    
    @POST
    @Path("/execute")
    public void execute(JaxbOperation operation) {
    
    }

}
