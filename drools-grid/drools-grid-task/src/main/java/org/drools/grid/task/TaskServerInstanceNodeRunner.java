package org.drools.grid.task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.remote.mina.MinaIoHandler;
import org.drools.task.UserInfo;
import org.drools.task.service.TaskService;
import org.drools.task.service.TaskServiceSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskServerInstanceNodeRunner {

    private static Logger logger = LoggerFactory.getLogger( TaskServerInstanceNodeRunner.class );

    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec<String> addressOpt = parser.accepts( "address",
                                                        "the address to bind the node" ).withRequiredArg().ofType( String.class );
        OptionSpec<Integer> portOpt = parser.accepts( "port",
                                                      "the port to listen in the given address" ).withRequiredArg().ofType( Integer.class );
        OptionSet options = parser.parse( args );

        String address = options.valueOf( addressOpt );
        int port = options.valueOf( portOpt );

        EntityManagerFactory emf = Persistence.createEntityManagerFactory( "org.drools.task" );

        TaskService taskService = new TaskService( emf,
                                                   SystemEventListenerFactory.getSystemEventListener() );
        TaskServiceSession taskSession = taskService.createSession();
        UserInfo userInfo = new DefaultUserInfo();
        taskService.setUserinfo( userInfo );

        SocketAddress htAddress = new InetSocketAddress( address,
                                                         port );
        SocketAcceptor htAcceptor = new NioSocketAcceptor();

        htAcceptor.setHandler( new MinaIoHandler( SystemEventListenerFactory.getSystemEventListener(),
                                                  new TaskServerMessageHandlerImpl( taskService,
                                                                                    SystemEventListenerFactory.getSystemEventListener() ) ) );
        final MinaAcceptor humanTaskServer = new MinaAcceptor( htAcceptor,
                                                               htAddress );

        Runtime.getRuntime().addShutdownHook( new Thread() {
            @Override
            public void run() {
                logger.info( "stoping server..." );
                humanTaskServer.stop();
                logger.info( "server stoped..." );
            }
        } );

        humanTaskServer.start();
        logger.info( "server started at " + htAddress.toString() + " ... (ctrl-c to stop it)" );

    }
}
