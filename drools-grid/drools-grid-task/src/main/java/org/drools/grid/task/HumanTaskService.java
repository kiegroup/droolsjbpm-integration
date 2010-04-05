package org.drools.grid.task;

import java.util.List;

import org.drools.eventmessaging.EventKey;

import org.drools.grid.task.TaskClientMessageHandlerImpl.AddAttachmentMessageResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.AddCommentMessageResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.AddTaskMessageResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.DeleteAttachmentMessageResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.DeleteCommentMessageResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.GetContentMessageResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.GetTaskMessageResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.SetDocumentMessageResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.TaskOperationMessageResponseHandler;
import org.drools.grid.task.TaskClientMessageHandlerImpl.TaskSummaryMessageResponseHandler;
import org.drools.grid.task.eventmessaging.EventMessageResponseHandler;
import org.drools.task.Attachment;
import org.drools.task.Comment;
import org.drools.task.Content;
import org.drools.task.Task;
import org.drools.task.service.ContentData;
import org.drools.task.service.FaultData;
 

public interface HumanTaskService {
    public void addTask(Task task, ContentData content, AddTaskMessageResponseHandler responseHandler);
    public Task getTask(long taskId, GetTaskMessageResponseHandler responseHandler);
    public void addComment(long taskId, Comment comment, AddCommentMessageResponseHandler responseHandler);
    public void deleteComment(long taskId, long commentId, DeleteCommentMessageResponseHandler responseHandler);
    public void addAttachment(long taskId, Attachment attachment, Content content, AddAttachmentMessageResponseHandler responseHandler);
    public void deleteAttachment(long taskId, long attachmentId, long contentId, DeleteAttachmentMessageResponseHandler responseHandler );
    public void setDocumentContent(long taskId, Content content, SetDocumentMessageResponseHandler responseHandler);
    public void getContent(long contentId, GetContentMessageResponseHandler responseHandler);
    public void claim(long taskId, String userId, TaskOperationMessageResponseHandler responseHandler);
    public void start(long taskId, String userId, TaskOperationMessageResponseHandler responseHandler);
    public void stop(long taskId, String userId, TaskOperationMessageResponseHandler responseHandler);
    public void release(long taskId, String userId, TaskOperationMessageResponseHandler responseHandler);
    public void suspend(long taskId, String userId, TaskOperationMessageResponseHandler responseHandler);
    public void resume(long taskId, String userId, TaskOperationMessageResponseHandler responseHandler);
    public void skip(long taskId, String userId, TaskOperationMessageResponseHandler responseHandler);
    public void delegate(long taskId, String userId, String targetUserId, TaskOperationMessageResponseHandler responseHandler);
    public void forward(long taskId, String userId, String targetEntityId, TaskOperationMessageResponseHandler responseHandler) ;
    public void complete(long taskId, String userId, ContentData outputData, TaskOperationMessageResponseHandler responseHandler) ;
    public void fail(long taskId, String userId, FaultData faultData, TaskOperationMessageResponseHandler responseHandler);
    public void getTasksOwned(String userId, String language, TaskSummaryMessageResponseHandler responseHandler);
    public void getTasksAssignedAsBusinessAdministrator(String userId, String language, TaskSummaryMessageResponseHandler responseHandler);
    public void getTasksAssignedAsExcludedOwner(String userId, String language, TaskSummaryMessageResponseHandler responseHandler);
    public void getTasksAssignedAsPotentialOwner(String userId, String language, TaskSummaryMessageResponseHandler responseHandler);
    public void getTasksAssignedAsPotentialOwner(String userId, List<String> groupIds, String language, TaskSummaryMessageResponseHandler responseHandler);
    public void getSubTasksAssignedAsPotentialOwner(long parentId, String userId, String language, TaskSummaryMessageResponseHandler responseHandler);
    public void getSubTasksByParent(long parentId, TaskSummaryMessageResponseHandler responseHandler);
    public void getTasksAssignedAsRecipient(String userId, String language, TaskSummaryMessageResponseHandler responseHandler);
    public void getTasksAssignedAsTaskInitiator(String userId, String language, TaskSummaryMessageResponseHandler responseHandler);
    public void getTasksAssignedAsTaskStakeholder(String userId, String language, TaskSummaryMessageResponseHandler responseHandler);
    public void registerForEvent(EventKey key, boolean remove, EventMessageResponseHandler responseHandler);
    public boolean connect();
    public void disconnect();
}
