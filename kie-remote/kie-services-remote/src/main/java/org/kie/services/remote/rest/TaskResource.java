package org.kie.services.remote.rest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.jbpm.kie.services.api.IdentityProvider;
import org.jbpm.services.task.commands.*;
import org.jbpm.services.task.impl.model.TaskImpl;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.kie.api.command.Command;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.TaskSummary;
import org.kie.services.client.api.command.serialization.jaxb.impl.JaxbCommandMessage;
import org.kie.services.remote.cdi.ProcessRequestBean;
import org.kie.services.remote.rest.exception.IncorrectRequestException;
import org.kie.services.remote.rest.jaxb.JaxbGenericResponse;
import org.kie.services.remote.rest.jaxb.JaxbTaskSummaryList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/task")
@RequestScoped
public class TaskResource extends ResourceBase {

    private Logger logger = LoggerFactory.getLogger(TaskResource.class);

    @Inject
    private ProcessRequestBean processRequestBean;

    @Context
    private HttpServletRequest request;
    
    @Inject
    private IdentityProvider identityProvider;
    
    private static String[] allowedOperations = { "activate", "claim", "claimnextavailable", "complete", "delegate", "exit",
            "fail", "forward", "release", "resume", "skip", "start", "stop", "suspend", "nominate" };

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/")
    public JaxbGenericResponse getTaskInstanceInfo(@PathParam("oper") String operation) {
        return null;
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    @Path("/execute")
    public JaxbGenericResponse execute(JaxbCommandMessage<?> cmdMsg) {
        List<Object> results = new ArrayList<Object>();
        for (Object cmd : cmdMsg.getCommands()) {
            Object result = processRequestBean.doTaskOperation((Command<?>) cmd);
            results.add(result);
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/query")
    public JaxbTaskSummaryList query(@Context UriInfo uriInfo) {
        MultivaluedMap<String, String> multiParams = uriInfo.getQueryParameters();
        Map<String, List<String>> params = getRequestParams(request);
        List<Long> workItemIdList = getLongListParam("workItemId", false, params, "query", true);
        List<Long> taskIdList = getLongListParam("taskId", false, params, "query", true);
        List<String> busAdminList = getStringListParam("businessAdministrator", false, params, "query");
        List<String> potOwnList = getStringListParam("potentialOwner", false, params, "query");
        List<String> statusStrList = getStringListParam("status", false, params, "query");
        List<String> taskOwnList = getStringListParam("taskOwner", false, params, "query");
        List<Long> procInstIdList = getLongListParam("processInstanceId", false, params, "query", true);
        String language = getStringParam("languauge", false, params, "query");

        // clean up params
        if (language == null) {
            language = "en-UK";
        }
        List<Status> statusList = convertStringListToStatusList(statusStrList);

        // process params/cmds
        Queue<Command<?>> cmds = new LinkedList<Command<?>>();
        if (!workItemIdList.isEmpty()) {
            for (Long workItemId : workItemIdList) {
                cmds.add(new GetTaskByWorkItemIdCommand(workItemId));
            }
        }
        if (!taskIdList.isEmpty()) {
            for (Long taskId : taskIdList) {
                cmds.add(new GetTaskCommand(taskId));
            }
        }

        Set<TaskSummaryImpl> results = new HashSet<TaskSummaryImpl>();
        Command<?> cmd = null;
        while (!cmds.isEmpty()) {
            cmd = cmds.poll();
            TaskImpl task = (TaskImpl) processRequestBean.doTaskOperation(cmd);
            if (task != null) {
                results.add(convertTaskToTaskSummary(task));
            }
        }

        int assignments = 0;
        assignments += potOwnList.isEmpty() ? 0 : 1;
        assignments += busAdminList.isEmpty() ? 0 : 1;
        assignments += taskOwnList.isEmpty() ? 0 : 1;

        if (assignments == 0) {
            if (!procInstIdList.isEmpty()) {
                if (!statusList.isEmpty()) {
                    for (Long procInstId : procInstIdList) {
                        cmds.add(new GetTasksByStatusByProcessInstanceIdCommand(procInstId.longValue(), language, statusList));
                    }
                } else {
                    for (Long procInstId : procInstIdList) {
                        cmd = new GetTasksByProcessInstanceIdCommand(procInstId);
                        List<Long> procInstTaskIdList = (List<Long>) processRequestBean.doTaskOperation(cmd);
                        for (Long taskId : procInstTaskIdList) {
                            cmd = new GetTaskCommand(taskId);
                            TaskImpl task = (TaskImpl) processRequestBean.doTaskOperation(cmd);
                            if (task != null) {
                                results.add(convertTaskToTaskSummary(task));
                            }
                        }
                    }
                }
            }
        } else {
            if (!busAdminList.isEmpty()) {
                for (String userId : busAdminList) {
                    cmds.add(new GetTaskAssignedAsBusinessAdminCommand(userId, language));
                }
            }
            if (!potOwnList.isEmpty()) {
                if (statusList.isEmpty()) {
                    for (String userId : potOwnList) {
                        cmds.add(new GetTasksOwnedCommand(userId, language));
                    }
                } else {
                    for (String userId : potOwnList) {
                        cmds.add(new GetTasksOwnedCommand(userId, language, statusList));
                    }

                }
            }
            if (!taskOwnList.isEmpty()) {
                if (statusList.isEmpty()) {
                    for (String userId : taskOwnList) {
                        cmds.add(new GetTaskAssignedAsPotentialOwnerCommand(userId, language));
                    }
                } else {
                    for (String userId : taskOwnList) {
                        cmds.add(new GetTaskAssignedAsPotentialOwnerCommand(userId, language, statusList));
                    }
                }
            }
        }

        while (!cmds.isEmpty()) {
            cmd = cmds.poll();
            List<TaskSummary> taskSummaryList = (List<TaskSummary>) processRequestBean.doTaskOperation(cmd);
            if (taskSummaryList != null && !taskSummaryList.isEmpty()) {
                for (TaskSummary taskSummary : taskSummaryList) {
                    results.add((TaskSummaryImpl) taskSummary);
                }
            }
        }

        System.out.println("TEST SEND");
        return new JaxbTaskSummaryList(results);
    }

    @POST
    @Path("/{id: [0-9-]+}/{oper: [a-zA-Z]+}")
    public void doTaskOperation(@PathParam("id") long taskId, @PathParam("oper") String operation) { 
        Map<String, List<String>> params = getRequestParams(request);
        operation = checkThatOperationExists(operation, allowedOperations);        
        String userId = identityProvider.getName();
        Command<?> cmd = null;
        if ("activate".equals(operation)) {
            cmd = new ActivateTaskCommand(taskId, userId);
        } else if ("claim".equals(operation)) {
            cmd = new ClaimTaskCommand(taskId, userId);
        } else if ("claimnextavailable".equals(operation)) {
            String language = getStringParam("language", false, params, operation);
            if (language == null) {
                language = "en-UK";
            }
            cmd = new ClaimNextAvailableTaskCommand(userId, language);
        } else if ("complete".equals(operation)) {
            Map<String, Object> data = extractMapFromParams(params, operation);
            cmd = new CompleteTaskCommand(taskId, userId, data);
        } else if ("delegate".equals(operation)) {
            String targetEntityId = getStringParam("targetEntityId", true, params, operation);
            cmd = new DelegateTaskCommand(taskId, userId, targetEntityId);
        } else if ("exit".equals(operation)) {
            cmd = new ExitTaskCommand(taskId, userId);
        } else if ("fail".equals(operation)) {
            Map<String, Object> data = extractMapFromParams(params, operation);
            cmd = new FailTaskCommand(taskId, userId, data);
        } else if ("forward".equals(operation)) {
            String targetEntityId = getStringParam("targetEntityId", true, params, operation);
            cmd = new ForwardTaskCommand(taskId, userId, targetEntityId);
        } else if ("release".equals(operation)) {
            cmd = new ReleaseTaskCommand(taskId, userId);
        } else if ("resume".equals(operation)) {
            cmd = new ResumeTaskCommand(taskId, userId);
        } else if ("skip".equals(operation)) {
            cmd = new SkipTaskCommand(taskId, userId);
        } else if ("start".equals(operation)) {
            cmd = new StartTaskCommand(taskId, userId);
        } else if ("stop".equals(operation)) {
            cmd = new StopTaskCommand(taskId, userId);
        } else if ("nominate".equals(operation)) {
            List<OrganizationalEntity> potentialOwners = getOrganizationalEntityListFromParams(params);
            cmd = new NominateTaskCommand(taskId, userId, potentialOwners);
        } else {
            throw new IncorrectRequestException("Unsupported operation: /task/" + taskId + "/" + operation);
        }
        processRequestBean.doTaskOperation(cmd);
    }

}
