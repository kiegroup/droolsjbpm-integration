package org.kie.server.remote.rest.jbpm;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.NodeInstanceDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.model.UserTaskInstanceDesc;
import org.jbpm.services.api.model.VariableDesc;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.query.QueryContext;
import org.kie.internal.query.QueryFilter;
import org.kie.internal.task.api.AuditTask;
import org.kie.internal.task.api.model.TaskEvent;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.remote.rest.common.exception.ExecutionServerRestOperationException;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;
import static org.kie.server.remote.rest.common.util.RestUtils.*;
import static org.kie.server.remote.rest.jbpm.resources.Messages.*;

@Path("/server")
public class RuntimeDataServiceResource {

    public static final Logger logger = LoggerFactory.getLogger(RuntimeDataServiceResource.class);
    private static final Boolean BYPASS_AUTH_USER = Boolean.parseBoolean(System.getProperty("org.kie.server.bypass.auth.user", "false"));

    private RuntimeDataService runtimeDataService;
    private IdentityProvider identityProvider;

    public RuntimeDataServiceResource(RuntimeDataService delegate, KieServerRegistry context) {
        this.runtimeDataService = delegate;
        this.identityProvider = context.getIdentityProvider();
    }

    protected String getUser(String queryParamUser) {
        if (BYPASS_AUTH_USER) {
            return queryParamUser;
        }

        return identityProvider.getName();
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
    public Response getProcessInstancesByDeploymentId(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("id") String containerId, @QueryParam("status")List<Integer> status,
            @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize) {

        if (status == null || status.isEmpty()) {
            status = new ArrayList<Integer>();
            status.add(ProcessInstance.STATE_ACTIVE);
        }

        Collection<ProcessInstanceDesc> instances = runtimeDataService.getProcessInstancesByDeploymentId(containerId, status , new QueryContext(page, pageSize));

        return createCorrectVariant(instances, headers, Response.Status.OK);
    }

    @GET
    @Path("process/instances/{pInstanceId}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getProcessInstanceById(@javax.ws.rs.core.Context HttpHeaders headers, @PathParam("pInstanceId") long processInstanceId) {
        Variant v = getVariant(headers);
        ProcessInstanceDesc processInstanceDesc = runtimeDataService.getProcessInstanceById(processInstanceId);
        if (processInstanceDesc == null) {

            throw ExecutionServerRestOperationException.notFound(MessageFormat.format(PROCESS_INSTANCE_NOT_FOUND, processInstanceId), v);
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

    @GET
    @Path(TASKS_ASSIGN_POT_OWNERS_GET_URI)
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getTasksAssignedAsPotentialOwner(@Context HttpHeaders headers, @PathParam("id") String containerId,
            @QueryParam("user") String userId, @QueryParam("page") @DefaultValue("0") Integer page, @QueryParam("pageSize") @DefaultValue("10") Integer pageSize ) {

        Variant v = getVariant(headers);

        try {
            userId = getUser(userId);
            logger.debug("About to search for task assigned as potential owner for user '{}'", userId);

            List<TaskSummary> tasks = runtimeDataService.getTasksAssignedAsPotentialOwner(userId, new QueryFilter(page, pageSize));
            org.kie.server.api.model.instance.TaskSummary[] instances = new org.kie.server.api.model.instance.TaskSummary[tasks.size()];

            logger.debug("Found {} tasks for user '{}' assigned as potential owner", tasks.size(), userId);
            int counter = 0;
            for (TaskSummary taskSummary : tasks) {
                org.kie.server.api.model.instance.TaskSummary task = org.kie.server.api.model.instance.TaskSummary.builder()
                        .id(taskSummary.getId())
                        .name(taskSummary.getName())
                        .description(taskSummary.getDescription())
                        .subject(taskSummary.getSubject())
                        .taskParentId(taskSummary.getParentId())
                        .activationTime(taskSummary.getActivationTime())
                        .actualOwner(taskSummary.getActualOwnerId())
                        .containerId(taskSummary.getDeploymentId())
                        .createdBy(taskSummary.getCreatedById())
                        .createdOn(taskSummary.getCreatedOn())
                        .expirationTime(taskSummary.getExpirationTime())
                        .priority(taskSummary.getPriority())
                        .processId(taskSummary.getProcessId())
                        .processInstanceId(taskSummary.getProcessInstanceId())
                        .status(taskSummary.getStatusId())
                        .skipable(taskSummary.isSkipable())
                        .build();
                instances[counter] = task;
                counter++;
            }

            return createResponse(new TaskSummaryList(instances), v, Response.Status.OK);

        } catch (Exception e) {
            logger.error("Unexpected error during processing {}", e.getMessage(), e);
            throw ExecutionServerRestOperationException.internalServerError(MessageFormat.format(UNEXPECTED_ERROR, e.getMessage()), v);
        }

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
