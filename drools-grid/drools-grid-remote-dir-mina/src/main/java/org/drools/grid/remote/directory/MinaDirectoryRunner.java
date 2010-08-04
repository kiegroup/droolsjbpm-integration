package org.drools.grid.remote.directory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.drools.SystemEventListenerFactory;
import org.drools.grid.internal.NodeData;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.drools.grid.remote.mina.MinaIoHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaDirectoryRunner {

	private static Logger logger = LoggerFactory.getLogger(MinaDirectoryRunner.class);
	
	public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec<String> addressOpt = parser.accepts("address", "the address to bind the node").withRequiredArg().ofType(String.class);
        OptionSpec<Integer> portOpt = parser.accepts("port", "the port to listen in the given address").withRequiredArg().ofType(Integer.class);
        OptionSet options = parser.parse(args);

		String address = options.valueOf(addressOpt);
		int port = options.valueOf(portOpt);
		logger.info("starting server");
		SocketAddress socket = new InetSocketAddress(address, port);
		NodeData nodeData = new NodeData();
		// setup Server
		SocketAcceptor acceptor = new NioSocketAcceptor();
		acceptor.setHandler(new MinaIoHandler(SystemEventListenerFactory.getSystemEventListener(),
							new DirectoryServerMessageHandlerImpl(
									SystemEventListenerFactory.getSystemEventListener())));
		
       final MinaAcceptor minaAcceptor = new MinaAcceptor(acceptor, socket);
       Runtime.getRuntime().addShutdownHook(new Thread() {
           public void run() {
        	   logger.info("stoping directory server...");
               minaAcceptor.stop();
               logger.info("server directory stoped...");
           }
        });
       minaAcceptor.start();
       logger.info("directory server started at "+ socket.toString() +" ... (ctrl-c to stop it)");
       new Thread(
                new Runnable() {

                    public void run() {
                        while(true){
                            try {
                                Thread.sleep(10 * 1000);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("Accessor clients = " + minaAcceptor.getCurrentSessions());
                        }
                    }
                }).start();
	}
}
