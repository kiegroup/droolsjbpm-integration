package org.drools.io.mina;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.drools.SystemEventListener;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.internal.responsehandlers.BlockingMessageResponseHandler;
import org.drools.grid.io.Acceptor;
import org.drools.grid.io.Connector;
import org.drools.grid.io.Conversation;
import org.drools.grid.io.ConversationManager;
import org.drools.grid.io.IoWriter;
import org.drools.grid.io.Message;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.io.impl.ConversationManagerImpl;
import org.drools.grid.io.impl.MessageHandlerImpl;
import org.drools.grid.io.impl.MessageImpl;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.remote.mina.MinaConnector;
import org.drools.grid.service.directory.WhitePages;

import junit.framework.TestCase;

public class MinaTest extends TestCase {

    public void test1() throws Exception {
        SystemEventListener l = SystemEventListenerFactory.getSystemEventListener();

        MessageReceiverHandler accHandler = new MessageReceiverHandler() {

            private String     id;
            private AtomicLong counter = new AtomicLong();

            public void messageReceived(Conversation conversation,
                                        Message msgIn) {
                conversation.respond(  "echo: " + msgIn.getBody() );
            }

        };

        Acceptor acc = new MinaAcceptor();
        acc.open( new InetSocketAddress( "127.0.0.1",
                                         5012 ),
                  accHandler,
                  l );

        Connector conn = new MinaConnector();

        ConversationManager cm = new ConversationManagerImpl( "s1",
                                                              conn,
                                                              l);

        Conversation cv = cm.startConversation( new InetSocketAddress( "127.0.0.1",
                                                                       5012 ),
                                                                       "r1" );
        
        BlockingMessageResponseHandler blockHandler = new BlockingMessageResponseHandler();

        cv.sendMessage( "hello",
                        blockHandler );
        
        Message msg = blockHandler.getMessage( 5000 );
        System.out.println( msg.getBody() );
        
        conn.close();
        if(acc.isOpen()){
            acc.close();
        }
        assertEquals(false, acc.isOpen());
    }

}
