package org.kie.services.remote.newrest;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.kie.services.remote.ejb.ProcessRequestBean;
import org.kie.services.remote.newrest.move.me.to.client.JaxbDeploymentMessage;
import org.kie.services.remote.newrest.move.me.to.client.JaxbResponseMessage;
import org.kie.services.remote.newrest.move.me.to.client.JaxbTaskMessage;
import org.kie.services.remote.newrest.move.me.to.client.JaxbVariableOrSignalMessage;
import org.kie.services.remote.newrest.move.me.to.client.JaxbWorkItemMessage;
import org.kie.services.remote.rest.RuntimeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Maciej's API
 * 
 *
 */
public class NewDeploymentResource {

    private Logger logger = LoggerFactory.getLogger(RuntimeResource.class);

    @EJB
    protected ProcessRequestBean processRequestBean;
    
    @GET
    @Path("/deployments" )
    @Produces(MediaType.APPLICATION_XML)
    public JaxbResponseMessage getDeployments() { 
        return null;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}")
    public JaxbResponseMessage getDeploymentInfo(@PathParam("id") String id) { 
        return null;
    }
    
    @POST
    @Path("/deployment")
    @Consumes(MediaType.APPLICATION_XML)
    public void deployDeployment(JaxbDeploymentMessage xmlDeployment) { 
    }
    
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/processes")
    public JaxbResponseMessage getProcessInfo(@PathParam("id") String id) { 
        return null;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/{processDefId: [a-zA-Z0-9-_]+}")
    public JaxbResponseMessage startNewProcess(@PathParam("id") String id, @PathParam("processDefId") String processDefId) { 
        return null;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instances")
    public JaxbResponseMessage getProcessInstancesInfo(@PathParam("id") String id) {
        return null;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}")
    public JaxbResponseMessage getProcessInstanceInfo(@PathParam("id") String id, @PathParam("procInstId") Long procInstId) { 
        return null;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}/variables")
    public JaxbResponseMessage getProcessInstanceVariablesInfo(@PathParam("id") String id, @PathParam("procInstId") Long procInstId) { 
        return null;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}/variable/{name: [a-zA-Z0-9]+}")
    public JaxbResponseMessage getProcessInstanceVariableInfo(@PathParam("id") String id, @PathParam("procInstId") Long procInstId, 
            @PathParam("name") String varName) { 
        return null;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}/variable/{name: [a-zA-Z0-9]+}")
    public JaxbResponseMessage setProcessInstanceVariable(@PathParam("id") String id, @PathParam("procInstId") Long procInstId, 
            @PathParam("name") String varName, JaxbVariableOrSignalMessage xmlVariable) { 
        return null;
    }
    
    @DELETE
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}/variable/{name: [a-zA-Z0-9]+}")
    public void deleteProcessInstanceVariable(@PathParam("id") String id, @PathParam("procInstId") Long procInstId, 
            @PathParam("name") String varName) {
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}/signal/{name: [a-zA-Z0-9]+}")
    public void signalProcessInstance(@PathParam("id") String id, @PathParam("procInstId") Long procInstId, 
            @PathParam("name") String signalName, JaxbVariableOrSignalMessage xmlVariable) { 
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/signal/{name: [a-zA-Z0-9]+}")
    public void signal(@PathParam("id") String id, @PathParam("procInstId") Long procInstId, 
            @PathParam("name") String signalName, JaxbVariableOrSignalMessage xmlVariable) { 
    }
    
    @DELETE
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}")
    public void abortProcessInstance(@PathParam("id") String id, @PathParam("procInstId") Long procInstId) { 

    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}/workitems")
    public JaxbResponseMessage getWorkItems(@PathParam("id") String id, @PathParam("procInstId") Long procInstId) { 
        return null;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}/workitem/{workItemId: [0-9]+}")
    public void completeWorkItem(@PathParam("id") String id, @PathParam("procInstId") Long procInstId,
            @PathParam("workItemId") Long workItemId, JaxbWorkItemMessage xmlMsg) { 
    }
    
    @DELETE
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/process/instance/{procInstId: [0-9]+}/workitem/{workItemId: [0-9]+}")
    public void abortWorkItem(@PathParam("id") String id, @PathParam("procInstId") Long procInstId,
            @PathParam("workItemId") Long workItemId) { 
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/task/instances")
    public JaxbTaskMessage getTaskInstances(@PathParam("id") String id) { 
            return null;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/task/instance/process/{procInstId: [0-9]+}")
    public JaxbTaskMessage getTaskInstances(@PathParam("id") String id, @PathParam("procInstId") Long procInstId) { 
            return null;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/task/instance/workitem/{workItemId: [0-9]+}")
    public JaxbTaskMessage getTaskFromWorkItemId(@PathParam("id") String id, @PathParam("procInstId") Long procInstId) { 
            return null;
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/task/instance")
    public JaxbTaskMessage createNewTaskInstance(@PathParam("id") String id, JaxbTaskMessage taskMsg) { 
            return null;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/task/instance/{taskId: [0-9]+}")
    public JaxbTaskMessage getTaskInstanceDetails(@PathParam("id") String id, @PathParam("taskId") Long taskId) { 
            return null;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/task/instance/{taskId: [0-9]+}/{oper: [a-zA-Z]+}")
    public JaxbTaskMessage doTaskOperation(@PathParam("id") String id, @PathParam("taskId") Long taskId, @PathParam("oper") String oper) { 
            return null;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deployment/{id: [a-zA-Z0-9-_]+}/task/query")
    public JaxbTaskMessage doTaskQuery(@PathParam("id") String id, @Context UriInfo uriInfo) { 
        MultivaluedMap<String, String> params = uriInfo.getQueryParameters();
            return null;
    }
    
    
}