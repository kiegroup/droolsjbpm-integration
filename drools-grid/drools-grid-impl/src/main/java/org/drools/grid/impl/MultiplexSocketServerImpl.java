package org.drools.grid.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.grid.Grid;
import org.drools.grid.GridConnection;
import org.drools.grid.GridNode;
import org.drools.grid.GridServiceDescription;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.SocketService;
import org.drools.grid.io.Acceptor;
import org.drools.grid.io.AcceptorFactoryService;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.local.LocalGridNodeConnection;
import org.kie.SystemEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiplexSocketServerImpl
    implements
    SocketService {
    private AcceptorFactoryService factory;

    private String                 ip;

    private SystemEventListener    l;

    private Map<Integer, Acceptor> acceptors;
    
    private Grid                   grid;
    
    private static Logger logger = LoggerFactory.getLogger(MultiplexSocketServerImpl.class);

    public MultiplexSocketServerImpl( String ip,
                                      AcceptorFactoryService factory,
                                      SystemEventListener l,
                                      Grid grid ) {
        this.factory = factory;
        this.ip = ip;
        this.l = l;
        this.acceptors = new HashMap<Integer, Acceptor>();
        this.grid = grid;
    }

    /* (non-Javadoc)
     * @see org.drools.grid.impl.SocketServer#addService(int, java.lang.String, org.drools.grid.io.MessageReceiverHandler)
     */
    public synchronized void addService( String id,
                                         int port,
                                         Object object ) {
        if ( logger.isDebugEnabled() ) {
            logger.debug( " ### Trying to add a service -> " + id + " @ " + this.ip + ":" + port + " >> " + object + " [[ " + (object instanceof MessageReceiverHandlerFactoryService ));
        }
        MessageReceiverHandlerFactoryService handlerFactory = ( MessageReceiverHandlerFactoryService ) object;
        Acceptor acc = this.acceptors.get( port );

        MessageReceiverHandler h = handlerFactory.getMessageReceiverHandler();

        if ( acc == null ) {
            acc = factory.newAcceptor();

            MultiplexSocket ms = new MultiplexSocket();
            if ( logger.isDebugEnabled() ) {
                logger.debug( " ### Opening new Acceptor for -> " + this.ip + ":" + port + " >> " + object );
            }
            acc.open( new InetSocketAddress( this.ip,
                                             port ),
                                             ms,
                                             this.l );


            this.acceptors.put( port,
                                acc );
        } else {
            if ( logger.isDebugEnabled() ) {
                logger.debug( " ### Found existing Acceptor: " + this.ip + ":" + port + " >> " + object );
            }
        }

        MultiplexSocket ms = (MultiplexSocket) acc.getMessageReceiverHandler();
        ms.getHandlers().put( id, h );
        handlerFactory.registerSocketService( this.grid, id, this.ip, port );
    }

    /* (non-Javadoc)
     * @see org.drools.grid.impl.SocketServer#removeService(int, java.lang.String)
     */
    public synchronized void removeService(int socket,
                                           String id) {
        Acceptor acc = this.acceptors.get( socket );
        if ( acc != null ) {
            MultiplexSocket ms = (MultiplexSocket) acc.getMessageReceiverHandler();
            ms.getHandlers().remove( id  );
            if ( ms.getHandlers().isEmpty() ) {
                // If there are no more services on this socket, then close it
                acc.close();
            }
        }
    }

    public void close() {
        for ( Acceptor acc : this.acceptors.values() ) {
            if ( acc.isOpen() ) {
                if( logger.isTraceEnabled() ) {
                    logger.trace( " ### Closing Acceptor: " + acc.isOpen() );
                }
                acc.close();
                if( logger.isTraceEnabled() ) {
                    logger.trace( " ### Acceptor Closed: " + acc.isOpen() );
                }
            }

        }
    }

    public String getIp() {
        return this.ip;
    }

    public Set<Integer> getPorts() {
        return acceptors.keySet();
    }

    public <T> GridConnection<T> getConnection(GridServiceDescription<T> gsd) {
        GridNode gnode = grid.getGridNode( gsd.getId() );
        if ( gnode != null ) {
            LocalGridNodeConnection conn = new LocalGridNodeConnection( gnode );
        } else {
            
        }
        
        // this is a hack for now, will add in factories later
        if ( gsd.getServiceInterface().isAssignableFrom( GridNode.class )) {
           // new GridNodeConnectio(gsd);
        }
        return null;
    }
}
