package org.kie.server.services.jbpm.rest;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbpm.services.api.ProcessService;
import org.kie.api.command.Command;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.server.api.model.type.JaxbLong;

import static org.kie.server.services.rest.RestUtils.*;

@Path("/server")
public class ProcessServiceResource  {

    private ProcessService delegate;

    public ProcessServiceResource(ProcessService delegate) {
        this.delegate = delegate;
    }

    @PUT
    @Path("containers/{id}/process/{pId}")
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response startProcess(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @PathParam("pId") String processId, @javax.ws.rs.core.Context HttpServletRequest request) {
        try {
            Long processInstanceId = delegate.startProcess(containerId, processId, extractMapFromParams(request.getParameterMap()));

            return createCorrectVariant(new JaxbLong(processInstanceId), headers, Response.Status.CREATED);
        } catch (Exception e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    public Long startProcess(String containerId, String processId, Map<String, Object> parameters) {
        return delegate.startProcess(containerId, processId, parameters);
    }

    @DELETE
    @Path("containers/{id}/process/instance/{pInstanceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response abortProcessInstance(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("pInstanceId") Long processInstanceId) {
        try {
            delegate.abortProcessInstance(processInstanceId);
            // return null to produce 204 NO_CONTENT response code
            return null;
        } catch (Exception e) {
            return createCorrectVariant(e.getMessage(), headers, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }


    public void abortProcessInstances(List<Long> list) {

    }


    public void signalProcessInstance(Long aLong, String s, Object o) {

    }


    public void signalProcessInstances(List<Long> list, String s, Object o) {

    }


    public ProcessInstance getProcessInstance(Long aLong) {
        return null;
    }


    public void setProcessVariable(Long aLong, String s, Object o) {

    }


    public void setProcessVariables(Long aLong, Map<String, Object> map) {

    }


    public Object getProcessInstanceVariable(Long aLong, String s) {
        return null;
    }


    public Map<String, Object> getProcessInstanceVariables(Long aLong) {
        return null;
    }


    public Collection<String> getAvailableSignals(Long aLong) {
        return null;
    }


    public void completeWorkItem(Long aLong, Map<String, Object> map) {

    }


    public void abortWorkItem(Long aLong) {

    }


    public WorkItem getWorkItem(Long aLong) {
        return null;
    }


    public List<WorkItem> getWorkItemByProcessInstance(Long aLong) {
        return null;
    }


    public <T> T execute(String s, Command<T> command) {
        return null;
    }


    public <T> T execute(String s, Context<?> context, Command<T> command) {
        return null;
    }

    protected static Map<String, Object> extractMapFromParams(Map<String, String[]> params) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (Map.Entry<String, String[]> entry : params.entrySet()) {

            String key = entry.getKey();
            String[] paramValues = entry.getValue();

            String mapKey = key;
            String mapVal = paramValues[0].trim();

            map.put(mapKey, mapVal);

        }
        return map;
    }

}
