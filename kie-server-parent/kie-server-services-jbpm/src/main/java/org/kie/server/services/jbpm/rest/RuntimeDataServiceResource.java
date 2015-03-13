package org.kie.server.services.jbpm.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.query.QueryContext;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.task.api.AuditTask;
import org.kie.internal.task.api.model.TaskEvent;

import static org.kie.server.services.rest.RestUtils.*;

@Path("/server")
public class RuntimeDataServiceResource {

    private RuntimeDataService delegate;

    public RuntimeDataServiceResource(RuntimeDataService delegate) {
        this.delegate = delegate;
    }

    public Collection<ProcessInstanceDesc> getProcessInstances(QueryContext queryContext) {
        return null;
    }

    public Collection<ProcessInstanceDesc> getProcessInstances(List<Integer> list, String s, QueryContext queryContext) {
        return null;
    }

    public Collection<ProcessInstanceDesc> getProcessInstancesByProcessId(List<Integer> list, String s, String s1, QueryContext queryContext) {
        return null;
    }

    public Collection<ProcessInstanceDesc> getProcessInstancesByProcessName(List<Integer> list, String s, String s1, QueryContext queryContext) {
        return null;
    }

    @GET
    @Path("containers/{id}/process/instances")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstancesByDeploymentId(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @QueryParam("status")List<Integer> status) {

        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }

        Collection<ProcessInstanceDesc> instances = delegate.getProcessInstancesByDeploymentId(containerId, status , new QueryContext(0, 20));

        return createCorrectVariant(instances, headers, Response.Status.OK);
    }

    @GET
    @Path("containers/{id}/process/instances/{pInstanceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceById(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId) {

        ProcessInstanceDesc processInstanceDesc = delegate.getProcessInstanceById(processInstanceId);
        if (processInstanceDesc == null) {

            return createCorrectVariant("Not found", headers, Response.Status.NOT_FOUND);
        }
        return createCorrectVariant(processInstanceDesc, headers, Response.Status.OK);
    }

    public Collection<ProcessInstanceDesc> getProcessInstancesByProcessDefinition(String s, QueryContext queryContext) {
        return null;
    }

    public Collection<ProcessInstanceDesc> getProcessInstancesByProcessDefinition(String s, List<Integer> list, QueryContext queryContext) {
        return null;
    }

    public NodeInstanceDesc getNodeInstanceForWorkItem(Long aLong) {
        return null;
    }

    public Collection<NodeInstanceDesc> getProcessInstanceHistoryActive(long l, QueryContext queryContext) {
        return null;
    }

    public Collection<NodeInstanceDesc> getProcessInstanceHistoryCompleted(long l, QueryContext queryContext) {
        return null;
    }

    public Collection<NodeInstanceDesc> getProcessInstanceFullHistory(long l, QueryContext queryContext) {
        return null;
    }

    public Collection<NodeInstanceDesc> getProcessInstanceFullHistoryByType(long l, RuntimeDataService.EntryType entryType, QueryContext queryContext) {
        return null;
    }

    public Collection<VariableDesc> getVariablesCurrentState(long l) {
        return null;
    }

    public Collection<VariableDesc> getVariableHistory(long l, String s, QueryContext queryContext) {
        return null;
    }

    public Collection<ProcessDefinition> getProcessesByDeploymentId(String s, QueryContext queryContext) {
        return null;
    }

    public Collection<ProcessDefinition> getProcessesByFilter(String s, QueryContext queryContext) {
        return null;
    }

    public Collection<ProcessDefinition> getProcesses(QueryContext queryContext) {
        return null;
    }

    public Collection<String> getProcessIds(String s, QueryContext queryContext) {
        return null;
    }

    public ProcessDefinition getProcessById(String s) {
        return null;
    }

    public ProcessDefinition getProcessesByDeploymentIdProcessId(String s, String s1) {
        return null;
    }

    public UserTaskInstanceDesc getTaskByWorkItemId(Long aLong) {
        return null;
    }

    public UserTaskInstanceDesc getTaskById(Long aLong) {
        return null;
    }

    public List<TaskSummary> getTasksAssignedAsBusinessAdministrator(String s, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskSummary> getTasksAssignedAsBusinessAdministratorByStatus(String s, List<Status> list, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String s, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String s, List<String> list, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwnerByStatus(String s, List<Status> list, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwner(String s, List<String> list, List<Status> list1, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskSummary> getTasksAssignedAsPotentialOwnerByExpirationDateOptional(String s, List<Status> list, Date date, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskSummary> getTasksOwnedByExpirationDateOptional(String s, List<Status> list, Date date, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskSummary> getTasksOwned(String s, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskSummary> getTasksOwnedByStatus(String s, List<Status> list, QueryFilter queryFilter) {
        return null;
    }

    public List<Long> getTasksByProcessInstanceId(Long aLong) {
        return null;
    }

    public List<TaskSummary> getTasksByStatusByProcessInstanceId(Long aLong, List<Status> list, QueryFilter queryFilter) {
        return null;
    }

    public List<AuditTask> getAllAuditTask(String s, QueryFilter queryFilter) {
        return null;
    }

    public List<TaskEvent> getTaskEvents(long l, QueryFilter queryFilter) {
        return null;
    }
}
