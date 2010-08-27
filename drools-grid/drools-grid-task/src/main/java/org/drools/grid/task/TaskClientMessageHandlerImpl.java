/**
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.drools.SystemEventListener;
import org.drools.eventmessaging.Payload;
import org.drools.grid.internal.GenericIoWriter;
import org.drools.grid.internal.GenericMessageHandler;
import org.drools.grid.internal.Message;
import org.drools.grid.internal.MessageResponseHandler;
import org.drools.grid.task.eventmessaging.EventMessageResponseHandler;
import org.drools.grid.task.responseHandlers.BlockingTaskSummaryMessageResponseHandler;
import org.drools.task.Content;
import org.drools.task.Task;
import org.drools.task.query.TaskSummary;
import org.drools.task.service.Command;

public class TaskClientMessageHandlerImpl
    implements
    GenericMessageHandler {

    /**
     * Listener used for logging
     */
    private SystemEventListener                    systemEventListener;
    protected Map<Integer, MessageResponseHandler> responseHandlers;

    public TaskClientMessageHandlerImpl(SystemEventListener systemEventListener) {
        this.systemEventListener = systemEventListener;
        this.responseHandlers = new ConcurrentHashMap<Integer, MessageResponseHandler>();;
    }

    public void exceptionCaught(GenericIoWriter session,
                                Throwable cause) throws Exception {
        this.systemEventListener.exception( "Uncaught exception on client",
                                            cause );
    }

    public void messageReceived(GenericIoWriter session,
                                Message msg) throws Exception {
        Command cmd = (Command) msg.getPayload();
        this.systemEventListener.debug( "Message receieved redirected to the client 1111111111: " + cmd.getName() );
        this.systemEventListener.debug( "Arguments : " + Arrays.toString( cmd.getArguments().toArray() ) );

        switch ( cmd.getName() ) {
            case OperationResponse : {
                TaskOperationMessageResponseHandler responseHandler = (TaskOperationMessageResponseHandler) this.responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        responseHandler.setIsDone( true );
                        System.out.println( "IS DONDEEEE" );
                    }
                }
                break;
            }
            case GetTaskResponse : {
                GetTaskMessageResponseHandler responseHandler = (GetTaskMessageResponseHandler) this.responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        Task task = (Task) cmd.getArguments().get( 0 );
                        responseHandler.execute( task );
                    }
                }
                break;
            }
            case AddTaskResponse : {
                AddTaskMessageResponseHandler responseHandler = (AddTaskMessageResponseHandler) this.responseHandlers.remove( msg.getResponseId() );
                System.out.println( "response id searched: " + msg.getResponseId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        long taskId = (Long) cmd.getArguments().get( 0 );
                        responseHandler.execute( taskId );
                    }
                }
                break;
            }
            case AddCommentResponse : {
                AddCommentMessageResponseHandler responseHandler = (AddCommentMessageResponseHandler) this.responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        long commentId = (Long) cmd.getArguments().get( 0 );
                        responseHandler.execute( commentId );
                    }
                }
                break;
            }
            case DeleteCommentResponse : {
                DeleteCommentMessageResponseHandler responseHandler = (DeleteCommentMessageResponseHandler) this.responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        responseHandler.setIsDone( true );
                    }
                }
                break;
            }
            case AddAttachmentResponse : {
                AddAttachmentMessageResponseHandler responseHandler = (AddAttachmentMessageResponseHandler) this.responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        long attachmentId = (Long) cmd.getArguments().get( 0 );
                        long contentId = (Long) cmd.getArguments().get( 1 );
                        responseHandler.execute( attachmentId,
                                                 contentId );
                    }
                }
                break;
            }
            case DeleteAttachmentResponse : {
                DeleteAttachmentMessageResponseHandler responseHandler = (DeleteAttachmentMessageResponseHandler) this.responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        responseHandler.setIsDone( true );
                    }
                }
                break;
            }
            case GetContentResponse : {
                GetContentMessageResponseHandler responseHandler = (GetContentMessageResponseHandler) this.responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        Content content = (Content) cmd.getArguments().get( 0 );
                        responseHandler.execute( content );
                    }
                }
                break;
            }
            case SetDocumentContentResponse : {
                SetDocumentMessageResponseHandler responseHandler = (SetDocumentMessageResponseHandler) this.responseHandlers.remove( cmd.getId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        long contentId = (Long) cmd.getArguments().get( 0 );
                        responseHandler.execute( contentId );
                    }
                }
                break;
            }
            case QueryTaskSummaryResponse : {
                BlockingTaskSummaryMessageResponseHandler responseHandler = (BlockingTaskSummaryMessageResponseHandler) this.responseHandlers.remove( msg.getResponseId() );
                System.out.println( "responseHandler: " + responseHandler + " id searched: " + msg.getResponseId() );
                if ( responseHandler != null ) {
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                    } else {
                        List<TaskSummary> results = (List<TaskSummary>) cmd.getArguments().get( 0 );
                        responseHandler.execute( results );
                    }
                }
                break;
            }
            case EventTriggerResponse : {
                EventMessageResponseHandler responseHandler = (EventMessageResponseHandler) this.responseHandlers.remove( cmd.getId() ); //@TODO view messaging stuff
                System.out.println( "EVENT TRIGGER RESPONSE " + responseHandler + " size " + this.responseHandlers.size() + " id " + cmd.getId() );
                if ( responseHandler != null ) {
                    System.out.println( "responseHandler---: " + responseHandler );
                    if ( !cmd.getArguments().isEmpty() && cmd.getArguments().get( 0 ) instanceof RuntimeException ) {
                        responseHandler.setError( (RuntimeException) cmd.getArguments().get( 0 ) );
                        System.out.println( "EEerror" );
                    } else {
                        Payload payload = (Payload) cmd.getArguments().get( 0 );
                        System.out.println( "EExecute " );
                        responseHandler.execute( payload );
                    }
                }
                break;
            }
        }
    }

    public static interface GetTaskMessageResponseHandler
        extends
        MessageResponseHandler {
        public void execute(Task task);
    }

    public static interface AddTaskMessageResponseHandler
        extends
        MessageResponseHandler {
        public void execute(long taskId);
    }

    public static interface TaskOperationMessageResponseHandler
        extends
        MessageResponseHandler {
        public void setIsDone(boolean done);
    }

    public static interface AddCommentMessageResponseHandler
        extends
        MessageResponseHandler {
        public void execute(long commentId);
    }

    public static interface DeleteCommentMessageResponseHandler
        extends
        MessageResponseHandler {
        public void setIsDone(boolean done);
    }

    public static interface AddAttachmentMessageResponseHandler
        extends
        MessageResponseHandler {
        public void execute(long attachmentId,
                            long contentId);
    }

    public static interface DeleteAttachmentMessageResponseHandler
        extends
        MessageResponseHandler {
        public void setIsDone(boolean done);
    }

    public static interface SetDocumentMessageResponseHandler
        extends
        MessageResponseHandler {
        public void execute(long contentId);
    }

    public static interface GetContentMessageResponseHandler
        extends
        MessageResponseHandler {
        public void execute(Content content);
    }

    public static interface TaskSummaryMessageResponseHandler
        extends
        MessageResponseHandler {
        public void execute(List<TaskSummary> results);
    }

}