package org.drools.grid.io.impl;

import org.drools.SystemEventListener;
import org.drools.command.impl.ContextImpl;
import org.drools.command.impl.GenericCommand;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.runtime.impl.ExecutionResultImpl;

public class MessageHandlerImpl
    implements
    MessageReceiverHandler {

    private SystemEventListener systemEventListener;
    private NodeData            data;

    public MessageHandlerImpl(NodeData data,
                              SystemEventListener systemEventListener) {
        this.systemEventListener = systemEventListener;
        this.data = data;
    }

    /* (non-Javadoc)
     * @see org.drools.vsm.GenericMessageHandler#messageReceived(org.drools.vsm.GenericIoWriter, org.drools.vsm.Message)
     */
    public void messageReceived(Conversation conversation,
                                Message msg) {
        this.systemEventListener.debug( "Message receieved : " + msg );

        // we always need to process a List, for genericity, but don't force a List on the payload
        //        List<GenericCommand> commands;
        //        if ( msg.getPayload() instanceof List ) {
        //            commands = (List<GenericCommand>) msg.getPayload();
        //        } else {
        //            commands = new ArrayList<GenericCommand>();
        //            commands.add( (GenericCommand) msg.getPayload() );
        //        }
        GenericCommand command = (GenericCommand) msg.getBody();

        // Setup the evaluation context 
        ContextImpl localSessionContext = new ContextImpl( "session_" + msg.getConversationId(),
                                                           this.data.getContextManager(),
                                                           this.data.getTemp() );
        ExecutionResultImpl localKresults = new ExecutionResultImpl();
        localSessionContext.set( "kresults_" + msg.getConversationId(),
                                 localKresults );

        Object result = command.execute( localSessionContext );

        //        session.write( new MessageImpl( msg.getConversationId(),
        //                                    msg.getRequestId(),
        //                                    null,
        //                                    1,
        //                                    result ),
        //                       null );

        //        if ( !msg.isAsync() && localKresults.getIdentifiers().isEmpty() ) {
        //            // if it's not an async invocation and their are no results, just send a simple notification message
        //            session.write( new Message( msg.getSessionId(),
        //                                        msg.getResponseId(),
        //                                        msg.isAsync(),
        //                                        new FinishedCommand() ), null );
        //        } else {
        //            // return the payload
        //            session.write( new Message( msg.getSessionId(),
        //                                        msg.getResponseId(),
        //                                        msg.isAsync(),
        //                                        localKresults ), null );
        //        }
    }

}
