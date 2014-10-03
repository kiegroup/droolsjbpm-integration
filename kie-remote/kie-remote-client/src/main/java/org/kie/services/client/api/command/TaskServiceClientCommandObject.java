package org.kie.services.client.api.command;

import static org.kie.remote.client.jaxb.ConversionUtil.convertDateToXmlGregorianCalendar;
import static org.kie.remote.client.jaxb.ConversionUtil.convertMapToJaxbStringObjectPairArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.kie.api.command.Command;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.Attachment;
import org.kie.api.task.model.Comment;
import org.kie.api.task.model.Content;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.I18NText;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.PeopleAssignments;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.api.task.model.TaskSummary;
import org.kie.api.task.model.User;
import org.kie.remote.jaxb.gen.ActivateTaskCommand;
import org.kie.remote.jaxb.gen.AddTaskCommand;
import org.kie.remote.jaxb.gen.ClaimNextAvailableTaskCommand;
import org.kie.remote.jaxb.gen.ClaimTaskCommand;
import org.kie.remote.jaxb.gen.CompleteTaskCommand;
import org.kie.remote.jaxb.gen.DelegateTaskCommand;
import org.kie.remote.jaxb.gen.ExitTaskCommand;
import org.kie.remote.jaxb.gen.FailTaskCommand;
import org.kie.remote.jaxb.gen.GetAttachmentCommand;
import org.kie.remote.jaxb.gen.GetContentCommand;
import org.kie.remote.jaxb.gen.GetTaskAssignedAsBusinessAdminCommand;
import org.kie.remote.jaxb.gen.GetTaskAssignedAsPotentialOwnerCommand;
import org.kie.remote.jaxb.gen.GetTaskByWorkItemIdCommand;
import org.kie.remote.jaxb.gen.GetTaskCommand;
import org.kie.remote.jaxb.gen.GetTaskContentCommand;
import org.kie.remote.jaxb.gen.GetTasksByProcessInstanceIdCommand;
import org.kie.remote.jaxb.gen.GetTasksByStatusByProcessInstanceIdCommand;
import org.kie.remote.jaxb.gen.GetTasksOwnedCommand;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.NominateTaskCommand;
import org.kie.remote.jaxb.gen.QueryFilter;
import org.kie.remote.jaxb.gen.ReleaseTaskCommand;
import org.kie.remote.jaxb.gen.ResumeTaskCommand;
import org.kie.remote.jaxb.gen.SkipTaskCommand;
import org.kie.remote.jaxb.gen.StartTaskCommand;
import org.kie.remote.jaxb.gen.StopTaskCommand;
import org.kie.remote.jaxb.gen.SuspendTaskCommand;
import org.kie.remote.jaxb.gen.Type;
import org.kie.services.client.api.command.exception.MissingRequiredInfoException;

public class TaskServiceClientCommandObject extends AbstractRemoteCommandObject implements TaskService {

    public TaskServiceClientCommandObject(RemoteConfiguration config) {
        super(config);
        if( config.isJms() && config.getTaskQueue() == null ) { 
            throw new MissingRequiredInfoException("A Task queue is necessary in order to create a Remote JMS Client TaskService instance.");
        }
    }

    // helper methods -------------------------------------------------------------------------------------------------------------
    
    private QueryFilter addLanguageFilter( String language ) {
        QueryFilter filter = null;
        if( language != null ) {
            filter = new QueryFilter();
            filter.setLanguage(language);
        }
        return filter;
    }

    private static org.kie.remote.jaxb.gen.Task convertKieTaskToGenTask( Task task ) {
        org.kie.remote.jaxb.gen.Task genTask = new org.kie.remote.jaxb.gen.Task();
        genTask.setDescription(task.getDescription());
        List<org.kie.remote.jaxb.gen.I18NText> genTextList = convertKieTextListToGenTextList(task.getDescriptions());
        if( genTextList != null ) {
            genTask.getDescriptions().addAll(genTextList);
        }
        genTask.setId(task.getId());
        genTask.setName(task.getName());
        genTextList = convertKieTextListToGenTextList(task.getNames());
        if( genTextList != null ) {
            genTask.getNames().addAll(genTextList);
        }
        PeopleAssignments kiePeepAssigns = task.getPeopleAssignments();
        if( kiePeepAssigns != null ) {
            org.kie.remote.jaxb.gen.PeopleAssignments genPeepAssigns = new org.kie.remote.jaxb.gen.PeopleAssignments();
            genTask.setPeopleAssignments(genPeepAssigns);
            List<org.kie.remote.jaxb.gen.OrganizationalEntity> genOrgEntList = convertKieOrgEntListToGenOrgEntList(kiePeepAssigns
                    .getBusinessAdministrators());
            if( genOrgEntList != null ) {
                genPeepAssigns.getBusinessAdministrators().addAll(genOrgEntList);
            }
            genOrgEntList = convertKieOrgEntListToGenOrgEntList(kiePeepAssigns.getPotentialOwners());
            if( genOrgEntList != null ) {
                genPeepAssigns.getPotentialOwners().addAll(genOrgEntList);
            }
            org.kie.remote.jaxb.gen.OrganizationalEntity genOrgEnt = convertKieOrgEntToGenOrgEnt(kiePeepAssigns.getTaskInitiator());
            if( genOrgEnt != null ) {
                genPeepAssigns.setTaskInitiatorId(genOrgEnt.getId());
            }
        }
        genTask.setPriority(task.getPriority());
        genTask.setSubject(task.getSubject());
        genTextList = convertKieTextListToGenTextList(task.getSubjects());
        if( genTextList != null ) { 
            genTask.getSubjects().addAll(genTextList);
        }
        genTask.setTaskType(task.getTaskType());
        TaskData kieTaskData = task.getTaskData();
        genTask.setTaskData(convertKieTaskDataToGenTaskData(kieTaskData));
    
        return genTask;
    }

    private static List<org.kie.remote.jaxb.gen.I18NText> convertKieTextListToGenTextList( List<I18NText> kieTextList ) {
        List<org.kie.remote.jaxb.gen.I18NText> genTextList = null;
        if( kieTextList != null ) {
            genTextList = new ArrayList<org.kie.remote.jaxb.gen.I18NText>(kieTextList.size());
            for( I18NText text : kieTextList ) {
                org.kie.remote.jaxb.gen.I18NText genText = new org.kie.remote.jaxb.gen.I18NText();
                genText.setId(text.getId());
                genText.setLanguage(text.getLanguage());
                genText.setText(text.getText());
                genTextList.add(genText);
            }
        }
        return genTextList;
    }

    private static List<org.kie.remote.jaxb.gen.OrganizationalEntity> convertKieOrgEntListToGenOrgEntList(
            List<OrganizationalEntity> kieOrgEntList ) {
        List<org.kie.remote.jaxb.gen.OrganizationalEntity> genOrgEntList = null;
        if( kieOrgEntList != null ) {
            genOrgEntList = new ArrayList<org.kie.remote.jaxb.gen.OrganizationalEntity>(kieOrgEntList.size());
            for( OrganizationalEntity kieOrgEnt : kieOrgEntList ) {
                org.kie.remote.jaxb.gen.OrganizationalEntity genOrgEnt = convertKieOrgEntToGenOrgEnt(kieOrgEnt);
                genOrgEntList.add(genOrgEnt);
            }
        }
        return genOrgEntList;
    }

    private static org.kie.remote.jaxb.gen.OrganizationalEntity convertKieOrgEntToGenOrgEnt( OrganizationalEntity kieOrgEnt ) {
        if( kieOrgEnt != null ) {
            org.kie.remote.jaxb.gen.OrganizationalEntity genOrgEnt = new org.kie.remote.jaxb.gen.OrganizationalEntity();
            genOrgEnt.setId(kieOrgEnt.getId());
            if( kieOrgEnt instanceof Group ) {
                genOrgEnt.setType(Type.GROUP);
            } else if( kieOrgEnt instanceof User ) {
                genOrgEnt.setType(Type.USER);
            }
            return genOrgEnt;
        }
        return null;
    }

    private static org.kie.remote.jaxb.gen.TaskData convertKieTaskDataToGenTaskData(TaskData kieTaskData ) { 
        org.kie.remote.jaxb.gen.TaskData genTaskData = null;
        if( kieTaskData != null ) { 
            genTaskData = new org.kie.remote.jaxb.gen.TaskData();
            
            genTaskData.setStatus(kieTaskData.getStatus());
            genTaskData.setPreviousStatus(kieTaskData.getPreviousStatus());
            User user = kieTaskData.getActualOwner();
            genTaskData.setActualOwner(convertKieUserToStringId(user));
            genTaskData.setCreatedBy(convertKieUserToStringId(kieTaskData.getCreatedBy()));
            genTaskData.setCreatedOn(convertDateToXmlGregorianCalendar(kieTaskData.getCreatedOn()));
            Date date = kieTaskData.getActivationTime();
            genTaskData.setActivationTime(convertDateToXmlGregorianCalendar(date));
            date = kieTaskData.getExpirationTime();
            genTaskData.setExpirationTime(convertDateToXmlGregorianCalendar(date));
            genTaskData.setSkipable(kieTaskData.isSkipable());
            genTaskData.setWorkItemId(kieTaskData.getWorkItemId());
            genTaskData.setProcessInstanceId(kieTaskData.getProcessInstanceId());
            genTaskData.setProcessId(kieTaskData.getProcessId());
            genTaskData.setDeploymentId(kieTaskData.getDeploymentId());
            genTaskData.setProcessSessionId(kieTaskData.getProcessSessionId());
            genTaskData.setDocumentType(kieTaskData.getDocumentType());
            genTaskData.setDocumentContentId(kieTaskData.getDocumentContentId());
            genTaskData.setOutputType(kieTaskData.getOutputType());
            genTaskData.setOutputContentId(kieTaskData.getOutputContentId());
            genTaskData.setFaultName(kieTaskData.getFaultName());
            genTaskData.setFaultType(kieTaskData.getFaultType());
            genTaskData.setFaultContentId(kieTaskData.getFaultContentId());
            List<Attachment> attachs = kieTaskData.getAttachments(); 
            if( attachs != null ) { 
               for( Attachment attach : attachs ) { 
                  org.kie.remote.jaxb.gen.Attachment genAttach = new org.kie.remote.jaxb.gen.Attachment();
                  genAttach.setId(attach.getId());
                  genAttach.setName(attach.getName());
                  genAttach.setContentType(attach.getContentType());
                  genAttach.setAttachedAt(convertDateToXmlGregorianCalendar(attach.getAttachedAt()));
                  genAttach.setAttachedBy(attach.getAttachedBy().getId());
                  genAttach.setSize(attach.getSize());
                  genAttach.setAttachmentContentId(attach.getAttachmentContentId());
                  genTaskData.getAttachments().add(genAttach);
               }
            }
            List<Comment> comments = kieTaskData.getComments();
            if( comments != null ) { 
                for( Comment comment : comments ) { 
                    org.kie.remote.jaxb.gen.Comment genComment = new org.kie.remote.jaxb.gen.Comment();
                    genComment.setId(comment.getId());
                    genComment.setText(comment.getText());
                    genComment.setAddedAt(convertDateToXmlGregorianCalendar(comment.getAddedAt()));
                }
            }
            genTaskData.setParentId(kieTaskData.getParentId());
        }
        return genTaskData;
    }

    private static String convertKieUserToStringId(User user ) { 
        String userId = null;
        if( user != null ) { 
           userId = user.getId();
        }
        return userId;
    }

    // TaskService methods --------------------------------------------------------------------------------------------------------
    
    @Override
    public <T> T execute( Command<T> command ) {
        return (T) unsupported(TaskService.class, Object.class);
    }

    @Override
    public void activate( long taskId, String userId ) {
        ActivateTaskCommand cmd = new ActivateTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void claim( long taskId, String userId ) {
        ClaimTaskCommand cmd = new ClaimTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void claimNextAvailable( String userId, String language ) {
        ClaimNextAvailableTaskCommand cmd = new ClaimNextAvailableTaskCommand();
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void complete( long taskId, String userId, Map<String, Object> data ) {
        CompleteTaskCommand cmd = new CompleteTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        JaxbStringObjectPairArray values = convertMapToJaxbStringObjectPairArray(data);
        executeCommand(cmd);
    }

    @Override
    public void delegate( long taskId, String userId, String targetUserId ) {
        DelegateTaskCommand cmd = new DelegateTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        cmd.setTargetEntityId(targetUserId);
        executeCommand(cmd);
    }

    @Override
    public void exit( long taskId, String userId ) {
        ExitTaskCommand cmd = new ExitTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void fail( long taskId, String userId, Map<String, Object> faultData ) {
        FailTaskCommand cmd = new FailTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        JaxbStringObjectPairArray values = convertMapToJaxbStringObjectPairArray(faultData);
        cmd.setData(values);
        executeCommand(cmd);
    }

    @Override
    public void forward( long taskId, String userId, String targetEntityId ) {
        DelegateTaskCommand cmd = new DelegateTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        cmd.setTargetEntityId(targetEntityId);
        executeCommand(cmd);
    }

    @Override
    public Task getTaskByWorkItemId( long workItemId ) {
        GetTaskByWorkItemIdCommand cmd = new GetTaskByWorkItemIdCommand();
        cmd.setWorkItemId(workItemId);
        return (Task) executeCommand(cmd);
    }

    @Override
    public Task getTaskById( long taskId ) {
        GetTaskCommand cmd = new GetTaskCommand();
        cmd.setTaskId(taskId);
        return (Task) executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsBusinessAdministrator( String userId, String language ) {
        GetTaskAssignedAsBusinessAdminCommand cmd = new GetTaskAssignedAsBusinessAdminCommand();
        cmd.setUserId(userId);
        // no query filter for language
        return (List<TaskSummary>) executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwner( String userId, String language ) {
        GetTaskAssignedAsPotentialOwnerCommand cmd = new GetTaskAssignedAsPotentialOwnerCommand();
        cmd.setUserId(userId);
        cmd.setFilter(addLanguageFilter(language));
        return (List<TaskSummary>) executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwnerByStatus( String userId, List<Status> status, String language ) {
        GetTaskAssignedAsPotentialOwnerCommand cmd = new GetTaskAssignedAsPotentialOwnerCommand();
        cmd.setUserId(userId);
        if( status != null ) {
            cmd.getStatuses().addAll(status);
        }
        cmd.setFilter(addLanguageFilter(language));
        return (List<TaskSummary>) executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksOwned( String userId, String language ) {
        GetTasksOwnedCommand cmd = new GetTasksOwnedCommand();
        cmd.setUserId(userId);
        cmd.setFilter(addLanguageFilter(language));
        return (List<TaskSummary>) executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksOwnedByStatus( String userId, List<Status> status, String language ) {
        GetTasksOwnedCommand cmd = new GetTasksOwnedCommand();
        cmd.setUserId(userId);
        if( status != null ) {
            cmd.getStatuses().addAll(status);
        }
        cmd.setFilter(addLanguageFilter(language));
        return (List<TaskSummary>) executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksByStatusByProcessInstanceId( long processInstanceId, List<Status> status, String language ) {
        GetTasksByStatusByProcessInstanceIdCommand cmd = new GetTasksByStatusByProcessInstanceIdCommand();
        cmd.setProcessInstanceId(processInstanceId);
        if( status != null ) {
            cmd.getStatuses().addAll(status);
        }
        // no query filter for language
        return (List<TaskSummary>) executeCommand(cmd);
    }

    @Override
    public List<Long> getTasksByProcessInstanceId( long processInstanceId ) {
        GetTasksByProcessInstanceIdCommand cmd = new GetTasksByProcessInstanceIdCommand();
        cmd.setProcessInstanceId(processInstanceId);
        return (List<Long>) executeCommand(cmd);
    }

    @Override
    public long addTask( Task task, Map<String, Object> params ) {
        AddTaskCommand cmd = new AddTaskCommand();
        org.kie.remote.jaxb.gen.Task genTask = convertKieTaskToGenTask(task);
        cmd.setJaxbTask(genTask);
        JaxbStringObjectPairArray values = convertMapToJaxbStringObjectPairArray(params);
        cmd.setParameter(values);
        return (Long) executeCommand(cmd);
    }

    @Override
    public void release( long taskId, String userId ) {
        ReleaseTaskCommand cmd = new ReleaseTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void resume( long taskId, String userId ) {
        ResumeTaskCommand cmd = new ResumeTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void skip( long taskId, String userId ) {
        SkipTaskCommand cmd = new SkipTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void start( long taskId, String userId ) {
        StartTaskCommand cmd = new StartTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void stop( long taskId, String userId ) {
        StopTaskCommand cmd = new StopTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void suspend( long taskId, String userId ) {
        SuspendTaskCommand cmd = new SuspendTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void nominate( long taskId, String userId, List<OrganizationalEntity> potentialOwners ) {
        NominateTaskCommand cmd = new NominateTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        List<org.kie.remote.jaxb.gen.OrganizationalEntity> genOrgEntList = convertKieOrgEntListToGenOrgEntList(potentialOwners);
        if( genOrgEntList != null ) {
            cmd.getPotentialOwners().addAll(genOrgEntList);
        }
        executeCommand(cmd);
    }

    @Override
    public Content getContentById( long contentId ) {
        GetContentCommand cmd = new GetContentCommand();
        cmd.setContentId(contentId);
        return (Content) executeCommand(cmd);
    }

    @Override
    public Attachment getAttachmentById( long attachId ) {
        GetAttachmentCommand cmd = new GetAttachmentCommand();
        cmd.setAttachmentId(attachId);
        return (Attachment) executeCommand(cmd);
    }

    @Override
    public Map<String, Object> getTaskContent( long taskId ) {
        GetTaskContentCommand cmd = new GetTaskContentCommand();
        cmd.setTaskId(taskId);
        return (Map<String, Object>) executeCommand(cmd);
    }

}
