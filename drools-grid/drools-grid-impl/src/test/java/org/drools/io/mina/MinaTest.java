package org.drools.io.mina;

import static org.junit.Assert.assertEquals;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import org.drools.grid.impl.GridImpl;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Acceptor;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.ConversationManagerImpl;
import org.drools.grid.io.impl.ExceptionMessage;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.junit.Test;
import org.kie.SystemEventListener;
import org.kie.SystemEventListenerFactory;

public class MinaTest {

    @Test
    public void test1() throws Exception {
        SystemEventListener l = SystemEventListenerFactory.getSystemEventListener();

        MessageReceiverHandler accHandler = new MessageReceiverHandler() {

            private String     id;
            private AtomicLong counter = new AtomicLong();

            public void messageReceived(Conversation conversation,
                                        Message msgIn) {
                conversation.respond( "echo: " + msgIn.getBody() );
            }

            public void exceptionReceived(Conversation conversation, ExceptionMessage msg) {
            }

        };

        Acceptor acc = new MinaAcceptor();
        acc.open( new InetSocketAddress( "127.0.0.1",
                                         8000 ),
                  accHandler,
                  l );


        ConversationManager cm = new ConversationManagerImpl( new GridImpl("peer"),
                                                              l );

        Conversation cv = cm.startConversation( "s1",
                                                new InetSocketAddress( "127.0.0.1",
                                                                       8000 ),
                                                                       "r1" );

        BlockingMessageResponseHandler blockHandler = new BlockingMessageResponseHandler();

        cv.sendMessage( "hello",
                        blockHandler );

        Message msg = blockHandler.getMessage(100, 5000 );
        System.out.println( msg.getBody() );

        cv.endConversation();
        if ( acc.isOpen() ) {
            acc.close();
        }
        assertEquals( false,
                      acc.isOpen() );
    }

}
