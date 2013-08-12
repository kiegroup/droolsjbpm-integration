package org.kie.services.remote.basic.services;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.jbpm.console.ng.bd.service.DataServiceEntryPoint;
import org.jbpm.console.ng.pr.model.ProcessInstanceSummary;


@Path("/data")
@RequestScoped
public class DataServiceResource {

    @Inject
    private DataServiceEntryPoint dataServices;
    
    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/process/instance/{procInstId: [0-9]+}")
    public JaxbProcessInstanceSummary getProcessInstanceSummary(@PathParam("procInstId") Long procInstId) {
        ProcessInstanceSummary summary = dataServices.getProcessInstanceById(procInstId);
        return new JaxbProcessInstanceSummary(summary);
    }
    
}
