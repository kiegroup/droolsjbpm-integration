package org.drools.grid.impl;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.drools.SystemEventListener;
import org.drools.grid.MultiplexSocketService;
import org.drools.grid.io.Acceptor;
import org.drools.grid.io.AcceptorFactoryService;
import org.drools.grid.io.MessageReceiverHandler;

public class MultiplexSocketServerImpl implements MultiplexSocketService {
    private AcceptorFactoryService factory;
    
    private String ip;
    
    private SystemEventListener l;
    
    private Map<Integer, Acceptor> acceptors;
    
    public MultiplexSocketServerImpl(String ip, AcceptorFactoryService factory, SystemEventListener l ) {
        this.factory = factory;
        this.ip = ip;
        this.l = l;
        this.acceptors = new HashMap<Integer, Acceptor>();
    }
    
    
    /* (non-Javadoc)
     * @see org.drools.grid.impl.SocketServer#addService(int, java.lang.String, org.drools.grid.io.MessageReceiverHandler)
     */
    public synchronized void addService(int socket, String id, MessageReceiverHandler receiver ) {
        Acceptor acc = this.acceptors.get( socket );
        
        if ( acc == null ) {
            acc = factory.newAcceptor();
        
            MultiplexSocket ms = new MultiplexSocket();

            acc.open( new InetSocketAddress( this.ip,
                                             socket ),
                                             ms,
                                             this.l );
            this.acceptors.put(socket, acc);
        }
        
        MultiplexSocket ms = ( MultiplexSocket ) acc.getMessageReceiverHandler();
        ms.getHandlers().put( id, receiver );
    }
    
    /* (non-Javadoc)
     * @see org.drools.grid.impl.SocketServer#removeService(int, java.lang.String)
     */
    public synchronized void removeService(int socket, String id) {
        Acceptor acc = this.acceptors.get( socket );
        if ( acc != null ) {
            MultiplexSocket ms = ( MultiplexSocket ) acc.getMessageReceiverHandler();
            ms.getHandlers().remove( id );   
            if ( ms.getHandlers().isEmpty() ) {
                // If there are no more services on this socket, then close it
                acc.close();
            }
        }
    }

    public void close(){
        for(Acceptor acc : this.acceptors.values()){
            if(acc.isOpen()){
                acc.close();
            }
            
            
        }
    }

    public String getIp() {
        return this.ip;
    }
}
