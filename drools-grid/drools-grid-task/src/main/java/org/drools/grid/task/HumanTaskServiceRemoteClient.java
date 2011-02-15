/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.grid.task;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.eventmessaging.EventKey;
import org.drools.grid.ConnectorException;
import org.drools.grid.GenericNodeConnector;
import org.drools.grid.internal.Message;
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
import org.drools.task.service.Command;
import org.drools.task.service.CommandName;
import org.drools.task.service.ContentData;
import org.drools.task.service.FaultData;
import org.drools.task.service.Operation;

public class HumanTaskServiceRemoteClient
    implements
    HumanTaskService {

    private final GenericNodeConnector connector;
    private final AtomicInteger        counter;
    private int                        sessionId;
    private String                     clientName;
    private int                        DEFAULT_WAIT_TIME = 3000;

    public HumanTaskServiceRemoteClient(GenericNodeConnector connector,
                                        int sessionId) {
        this.connector = connector;
        this.counter = new AtomicInteger();
        this.clientName = String.valueOf( sessionId );
        this.sessionId = sessionId;

    }

    public void disconnect() throws ConnectorException {
        try {
            this.connector.disconnect();
        } catch ( RemoteException ex ) {
            Logger.getLogger( HumanTaskServiceRemoteClient.class.getName() ).log( Level.SEVERE,
                                                                                  null,
                                                                                  ex );
        }
    }

    public void addTask(Task task,
                        ContentData content,
                        AddTaskMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( task );
        args.add( content );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.AddTaskRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public Task getTask(long taskId,
                        GetTaskMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 1 );
        args.add( taskId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.GetTaskRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

        return null;
    }

    public void addComment(long taskId,
                           Comment comment,
                           AddCommentMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( taskId );
        args.add( comment );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.AddCommentRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void deleteComment(long taskId,
                              long commentId,
                              DeleteCommentMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( taskId );
        args.add( commentId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.DeleteCommentRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void addAttachment(long taskId,
                              Attachment attachment,
                              Content content,
                              AddAttachmentMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( taskId );
        args.add( attachment );
        args.add( content );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.AddAttachmentRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void deleteAttachment(long taskId,
                                 long attachmentId,
                                 long contentId,
                                 DeleteAttachmentMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( taskId );
        args.add( attachmentId );
        args.add( contentId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.DeleteAttachmentRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void setDocumentContent(long taskId,
                                   Content content,
                                   SetDocumentMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( taskId );
        args.add( content );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.SetDocumentContentRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getContent(long contentId,
                           GetContentMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 1 );
        args.add( contentId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.GetContentRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void claim(long taskId,
                      String userId,
                      TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Claim );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void start(long taskId,
                      String userId,
                      TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Start );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void stop(long taskId,
                     String userId,
                     TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Stop );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void release(long taskId,
                        String userId,
                        TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Release );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void suspend(long taskId,
                        String userId,
                        TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Suspend );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void resume(long taskId,
                       String userId,
                       TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Resume );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void skip(long taskId,
                     String userId,
                     TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( Operation.Skip );
        args.add( taskId );
        args.add( userId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void delegate(long taskId,
                         String userId,
                         String targetUserId,
                         TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 4 );
        args.add( Operation.Delegate );
        args.add( taskId );
        args.add( userId );
        args.add( targetUserId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void forward(long taskId,
                        String userId,
                        String targetEntityId,
                        TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 4 );
        args.add( Operation.Forward );
        args.add( taskId );
        args.add( userId );
        args.add( targetEntityId );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void complete(long taskId,
                         String userId,
                         ContentData outputData,
                         TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 5 );
        args.add( Operation.Complete );
        args.add( taskId );
        args.add( userId );
        args.add( null );
        args.add( outputData );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void fail(long taskId,
                     String userId,
                     FaultData faultData,
                     TaskOperationMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 5 );
        args.add( Operation.Fail );
        args.add( taskId );
        args.add( userId );
        args.add( null );
        args.add( faultData );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.OperationRequest,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getTasksOwned(String userId,
                              String language,
                              TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QueryTasksOwned,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getTasksAssignedAsBusinessAdministrator(String userId,
                                                        String language,
                                                        TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsBusinessAdministrator,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getTasksAssignedAsExcludedOwner(String userId,
                                                String language,
                                                TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsExcludedOwner,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getTasksAssignedAsPotentialOwner(String userId,
                                                 String language,
                                                 TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsPotentialOwner,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getTasksAssignedAsPotentialOwner(String userId,
                                                 List<String> groupIds,
                                                 String language,
                                                 TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( groupIds );
        args.add( language );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsPotentialOwnerWithGroup,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getSubTasksAssignedAsPotentialOwner(long parentId,
                                                    String userId,
                                                    String language,
                                                    TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( parentId );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QuerySubTasksAssignedAsPotentialOwner,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getSubTasksByParent(long parentId,
                                    TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( parentId );
        //@TODO: un hard code this
        args.add( "en-UK" );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QueryGetSubTasksByParentTaskId,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getTasksAssignedAsRecipient(String userId,
                                            String language,
                                            TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsRecipient,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getTasksAssignedAsTaskInitiator(String userId,
                                                String language,
                                                TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsTaskInitiator,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void getTasksAssignedAsTaskStakeholder(String userId,
                                                  String language,
                                                  TaskSummaryMessageResponseHandler responseHandler) {

        List<Object> args = new ArrayList<Object>( 2 );
        args.add( userId );
        args.add( language );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.QueryTasksAssignedAsTaskStakeholder,
                                   args );
        Message msg = new Message( this.sessionId,
                                   this.counter.incrementAndGet(),
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public void registerForEvent(EventKey key,
                                 boolean remove,
                                 EventMessageResponseHandler responseHandler) { //@TODO: look for the event stuff

        List<Object> args = new ArrayList<Object>( 3 );
        args.add( key );
        args.add( remove );
        args.add( this.clientName );
        Command cmd = new Command( this.counter.getAndIncrement(),
                                   CommandName.RegisterForEventRequest,
                                   args );
        int responseId = this.counter.incrementAndGet();
        Message msg = new Message( this.sessionId,
                                   responseId,
                                   false,
                                   cmd );
        this.connector.write( msg,
                              responseHandler );

    }

    public String getId() throws ConnectorException,
                         RemoteException {
        return "Remote:Task:";
    }

    public ServiceType getServiceType() throws ConnectorException,
                                       RemoteException {
        return ServiceType.REMOTE;
    }

}
