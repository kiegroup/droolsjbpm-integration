package org.kie.services.remote.rest;

import static org.kie.services.client.message.ServiceMessage.*;
import static org.kie.services.client.message.ServiceMessageMapper.convertQueryParamsToServiceMsg;

import java.lang.reflect.Method;
import java.util.Map;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.ServiceMessage;
import org.kie.services.client.message.serialization.impl.JaxbSerializationProvider;
import org.kie.services.client.message.serialization.impl.jaxb.JaxbServiceMessage;
import org.kie.services.remote.UnfinishedError;
import org.kie.services.remote.ejb.ProcessRequestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/rest/task/{id: [a-zA-Z0-9-]+}")
@RequestScoped
public class TaskResource {

    private Logger logger = LoggerFactory.getLogger(TaskResource.class);

    @EJB
    protected ProcessRequestBean processRequestBean;
    
    @PathParam("id")
    private String taskId;

    @POST
    @Path("/{oper: [a-zA-Z]+}")
    public void doTaskOperation(@PathParam("oper") String operation) { 
    
    }
}
