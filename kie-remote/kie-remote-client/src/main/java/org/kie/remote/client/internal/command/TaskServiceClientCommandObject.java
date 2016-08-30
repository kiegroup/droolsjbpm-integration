/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.client.internal.command;

import static org.kie.remote.client.jaxb.ConversionUtil.convertDateToXmlGregorianCalendar;
import static org.kie.remote.client.jaxb.ConversionUtil.convertMapToJaxbStringObjectPairArray;
import static org.kie.remote.client.jaxb.ConversionUtil.convertMapToStringKeyObjectValueMap;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

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
import org.kie.internal.jaxb.StringKeyObjectValueMap;
import org.kie.internal.query.QueryParameterIdentifiers;
import org.kie.remote.client.api.exception.MissingRequiredInfoException;
import org.kie.remote.jaxb.gen.ActivateTaskCommand;
import org.kie.remote.jaxb.gen.AddCommentCommand;
import org.kie.remote.jaxb.gen.AddContentCommand;
import org.kie.remote.jaxb.gen.AddTaskCommand;
import org.kie.remote.jaxb.gen.ClaimNextAvailableTaskCommand;
import org.kie.remote.jaxb.gen.ClaimTaskCommand;
import org.kie.remote.jaxb.gen.CompleteTaskCommand;
import org.kie.remote.jaxb.gen.DelegateTaskCommand;
import org.kie.remote.jaxb.gen.DeleteCommentCommand;
import org.kie.remote.jaxb.gen.DeleteContentCommand;
import org.kie.remote.jaxb.gen.ExitTaskCommand;
import org.kie.remote.jaxb.gen.FailTaskCommand;
import org.kie.remote.jaxb.gen.ForwardTaskCommand;
import org.kie.remote.jaxb.gen.GetAllCommentsCommand;
import org.kie.remote.jaxb.gen.GetAllContentCommand;
import org.kie.remote.jaxb.gen.GetAttachmentCommand;
import org.kie.remote.jaxb.gen.GetCommentCommand;
import org.kie.remote.jaxb.gen.GetContentByIdCommand;
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
import org.kie.remote.jaxb.gen.QueryCriteria;
import org.kie.remote.jaxb.gen.QueryFilter;
import org.kie.remote.jaxb.gen.QueryWhere;
import org.kie.remote.jaxb.gen.ReleaseTaskCommand;
import org.kie.remote.jaxb.gen.ResumeTaskCommand;
import org.kie.remote.jaxb.gen.SetTaskPropertyCommand;
import org.kie.remote.jaxb.gen.SkipTaskCommand;
import org.kie.remote.jaxb.gen.StartTaskCommand;
import org.kie.remote.jaxb.gen.StopTaskCommand;
import org.kie.remote.jaxb.gen.SuspendTaskCommand;
import org.kie.remote.jaxb.gen.Type;

@SuppressWarnings("unchecked")
public class TaskServiceClientCommandObject extends AbstractRemoteCommandObject implements TaskService {

    public TaskServiceClientCommandObject(RemoteConfiguration config) {
        super(config);
        if( config.isJms() && config.getTaskQueue() == null ) {
            throw new MissingRequiredInfoException("A Task queue is necessary in order to create a Remote JMS Client TaskService instance.");
        }
    }

    // helper methods -------------------------------------------------------------------------------------------------------------

    private QueryFilter addLanguageFilter( String language ) {
        QueryFilter filter = new QueryFilter();
        if( language != null ) {
            filter.setLanguage(language);
        }
        // since we do not have hint bout number of results to return return all
        filter.setCount(-1);
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
        claimNextAvailable(userId);
    }

    public void claimNextAvailable( String userId ) {
        ClaimNextAvailableTaskCommand cmd = new ClaimNextAvailableTaskCommand();
        cmd.setUserId(userId);
        executeCommand(cmd);
    }

    @Override
    public void complete( long taskId, String userId, Map<String, Object> data ) {
        CompleteTaskCommand cmd = new CompleteTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        cmd.setData(convertMapToJaxbStringObjectPairArray(data));
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
        ForwardTaskCommand cmd = new ForwardTaskCommand();
        cmd.setTaskId(taskId);
        cmd.setUserId(userId);
        cmd.setTargetEntityId(targetEntityId);
        executeCommand(cmd);
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
    public long addTask( Task task, Map<String, Object> params ) {
        AddTaskCommand cmd = new AddTaskCommand();
        org.kie.remote.jaxb.gen.Task genTask = convertKieTaskToGenTask(task);
        cmd.setJaxbTask(genTask);
        JaxbStringObjectPairArray values = convertMapToJaxbStringObjectPairArray(params);
        cmd.setParameter(values);
        return (Long) executeCommand(cmd);
    }

    @Override
    public Content getContentById( long contentId ) {
        GetContentByIdCommand cmd = new GetContentByIdCommand();
        cmd.setContentId(contentId);
        return executeCommand(cmd);
    }

    public Long addContent(long taskId, Content content) {
        AddContentCommand cmd = new AddContentCommand();
        cmd.setTaskId(taskId);
        org.kie.remote.jaxb.gen.Content jaxbContent = new org.kie.remote.jaxb.gen.Content();
        jaxbContent.setContent(content.getContent());
        cmd.setTaskId(taskId);

        return executeCommand(cmd);
    }

    public Long addContent(long taskId, Map<String, Object> params) {
        AddContentCommand cmd = new AddContentCommand();
        cmd.setTaskId(taskId);
        org.kie.remote.jaxb.gen.Content jaxbContent = new org.kie.remote.jaxb.gen.Content();
        StringKeyObjectValueMap jaxbMap = convertMapToStringKeyObjectValueMap(params);
        jaxbContent.setContentMap(jaxbMap);
        cmd.setTaskId(taskId);

        return executeCommand(cmd);
    }

    public void deleteContent(long taskId, long contentId) {
       DeleteContentCommand cmd = new DeleteContentCommand();
       cmd.setContentId(contentId);
       cmd.setTaskId(taskId);

       executeCommand(cmd);
    }

    public List<Content> getAllContentByTaskId(long taskId) {
       GetAllContentCommand cmd = new GetAllContentCommand();
       cmd.setTaskId(taskId);

       return executeCommand(cmd);
    }

    @Override
    public Map<String, Object> getTaskContent( long taskId ) {
        GetTaskContentCommand cmd = new GetTaskContentCommand();
        cmd.setTaskId(taskId);

        return executeCommand(cmd);
    }

    @Override
    public Attachment getAttachmentById( long attachId ) {
        GetAttachmentCommand cmd = new GetAttachmentCommand();
        cmd.setAttachmentId(attachId);
        return executeCommand(cmd);
    }

    @Override
    public Long addComment(long taskId, Comment comment) {
        // fill jaxbComment
        org.kie.remote.jaxb.gen.Comment jaxbComment = new org.kie.remote.jaxb.gen.Comment();
        Date addedAt = comment.getAddedAt();
        if( addedAt != null ) {
            XMLGregorianCalendar jaxbAddedAt = convertDateToXmlGregorianCalendar(addedAt);
            jaxbComment.setAddedAt(jaxbAddedAt);
        }
        User addedBy = comment.getAddedBy();
        if( addedBy != null ) {
            jaxbComment.setAddedBy(addedBy.getId());
        }
        jaxbComment.setText(comment.getText());
        jaxbComment.setId(comment.getId());

        //  create command
        AddCommentCommand cmd = new AddCommentCommand();
        cmd.setTaskId(taskId);
        cmd.setJaxbComment(jaxbComment);

        return executeCommand(cmd);
    }

    @Override
    public Long addComment( long taskId, String addedByUserId, String commentText ) {
        AddCommentCommand cmd = new AddCommentCommand();
        cmd.setTaskId(taskId);

        org.kie.remote.jaxb.gen.Comment jaxbComment = new org.kie.remote.jaxb.gen.Comment();
        jaxbComment.setAddedBy(addedByUserId);
        jaxbComment.setAddedAt(convertDateToXmlGregorianCalendar(new Date()));
        jaxbComment.setText(commentText);

        cmd.setJaxbComment(jaxbComment);

        return executeCommand(cmd);
    }

    @Override
    public void deleteComment(long taskId, long commentId) {
        DeleteCommentCommand cmd = new DeleteCommentCommand();
        cmd.setTaskId(taskId);
        cmd.setCommentId(commentId);
        executeCommand(cmd);
    }

    @Override
    public List<Comment> getAllCommentsByTaskId(long taskId) {
        GetAllCommentsCommand cmd = new GetAllCommentsCommand();
        cmd.setTaskId(taskId);

        return executeCommand(cmd);
    }

    @Override
    public Comment getCommentById(long commentId) {
        GetCommentCommand cmd = new GetCommentCommand();
        cmd.setCommentId(commentId);

        return executeCommand(cmd);
    }

    @Override
    public void setExpirationDate(long taskId, Date date) {
        SetTaskPropertyCommand cmd = new SetTaskPropertyCommand();
        cmd.setExpirationDate(convertDateToXmlGregorianCalendar(date));
        cmd.setProperty(BigInteger.valueOf(5l));

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
        return executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsBusinessAdministrator( String userId, String language ) {
        GetTaskAssignedAsBusinessAdminCommand cmd = new GetTaskAssignedAsBusinessAdminCommand();
        cmd.setUserId(userId);
        // no query filter for language
        return executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwner( String userId, String language ) {
        GetTaskAssignedAsPotentialOwnerCommand cmd = new GetTaskAssignedAsPotentialOwnerCommand();
        cmd.setUserId(userId);
        cmd.setFilter(addLanguageFilter(language));
        return executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwner( String userId, List<String> groupIds, String language, int firstResult, int maxResults ) {
        GetTaskAssignedAsPotentialOwnerCommand cmd
            = new GetTaskAssignedAsPotentialOwnerCommand();
        cmd.setUserId(userId);
        cmd.getGroupIds().addAll(groupIds);
        QueryFilter filter = new QueryFilter();
        filter.setOffset(firstResult);
        filter.setCount(maxResults);
        filter.setLanguage(language);
        cmd.setFilter(filter);
        return executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwnerByProcessId( String userId, String processId ) {
        org.kie.remote.jaxb.gen.TaskSummaryQueryCommand cmd = new org.kie.remote.jaxb.gen.TaskSummaryQueryCommand();
        cmd.setUserId(userId);

        QueryWhere queryWhere = new QueryWhere();
        cmd.setQueryWhere(queryWhere);

        QueryCriteria criteria = new QueryCriteria();
        criteria.setUnion(false);
        criteria.setFirst(true);
        criteria.setListId(QueryParameterIdentifiers.POTENTIAL_OWNER_ID_LIST);
        criteria.getParameters().add(userId);
        queryWhere.getQueryCriterias().add(criteria);

        criteria = new QueryCriteria();
        criteria.setUnion(false);
        criteria.setListId(QueryParameterIdentifiers.PROCESS_ID_LIST);
        criteria.getParameters().add(processId);
        queryWhere.getQueryCriterias().add(criteria);

        return executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksAssignedAsPotentialOwnerByStatus( String userId, List<Status> status, String language ) {
        GetTaskAssignedAsPotentialOwnerCommand cmd = new GetTaskAssignedAsPotentialOwnerCommand();
        cmd.setUserId(userId);
        if( status != null ) {
            cmd.getStatuses().addAll(status);
        }
        cmd.setFilter(addLanguageFilter(language));
        return executeCommand(cmd);
    }

    @Override
    public List<Long> getTasksByProcessInstanceId( long processInstanceId ) {
        GetTasksByProcessInstanceIdCommand cmd = new GetTasksByProcessInstanceIdCommand();
        cmd.setProcessInstanceId(processInstanceId);
        return executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksByStatusByProcessInstanceId( long processInstanceId, List<Status> status, String language ) {
        GetTasksByStatusByProcessInstanceIdCommand cmd = new GetTasksByStatusByProcessInstanceIdCommand();
        cmd.setProcessInstanceId(processInstanceId);
        if( status != null ) {
            cmd.getStatuses().addAll(status);
        }
        // no query filter for language
        return executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksOwned( String userId, String language ) {
        GetTasksOwnedCommand cmd = new GetTasksOwnedCommand();
        cmd.setUserId(userId);
        cmd.setFilter(addLanguageFilter(language));
        return executeCommand(cmd);
    }

    @Override
    public List<TaskSummary> getTasksOwnedByStatus( String userId, List<Status> status, String language ) {
        GetTasksOwnedCommand cmd = new GetTasksOwnedCommand();
        cmd.setUserId(userId);
        if( status != null ) {
            cmd.getStatuses().addAll(status);
        }
        cmd.setFilter(addLanguageFilter(language));
        return executeCommand(cmd);
    }

}
