package org.drools.grid.distributed.rio;


import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.SystemEventListener;
import org.drools.grid.generic.Message;
import org.drools.grid.generic.MessageResponseHandler;
import org.drools.grid.ExecutionNodeService;
import org.drools.grid.generic.GenericNodeConnector;


public class InternalRioNodeConnector
     implements GenericNodeConnector{

    protected final String                 name;
    protected AtomicInteger                counter;
    protected ExecutionNodeService         nodeService;
    protected SocketAddress                address;
    protected SystemEventListener          eventListener;
  
    public InternalRioNodeConnector(String name,
                        SystemEventListener eventListener
                       ) {
        if ( name == null ) {
            throw new IllegalArgumentException( "Name can not be null" );
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.eventListener = eventListener;
   
    }

    public InternalRioNodeConnector(String name,
                        SystemEventListener eventListener,
                        ExecutionNodeService nodeService
                        ) {
        if ( name == null ) {
            throw new IllegalArgumentException( "Name can not be null" );
        }
        this.name = name;
        this.counter = new AtomicInteger();
        this.eventListener = eventListener;
        this.nodeService = nodeService;
       
       
    }

    /* (non-Javadoc)
     * @see org.drools.vsm.mina.Messenger#connect()
     */
    public boolean connect() {
        
        return true;
    }

    /* (non-Javadoc)
     * @see org.drools.vsm.mina.Messenger#disconnect()
     */
    public void disconnect() {
        //I don't need to be disconected
    }

    public Message write(Message msg) {
        if ( nodeService != null ) {
            try {

                Message returnMessage = this.nodeService.write( msg );
                return returnMessage;
              
            } catch ( RemoteException ex ) {
                Logger.getLogger( InternalRioNodeConnector.class.getName() ).log( Level.SEVERE,
                                                                      null,
                                                                      ex );
            } catch ( Exception ex ) {
                Logger.getLogger( InternalRioNodeConnector.class.getName() ).log( Level.SEVERE,
                                                                      null,
                                                                      ex );
            }
        }
        throw new IllegalStateException( "sessionService should not be null" );
    }
    
    public void write(Message msg,
                      MessageResponseHandler responseHandler) {
        throw new UnsupportedOperationException();
    }

 
    public String getId() {
        try {
            return nodeService.getId();
        } catch (RemoteException ex) {
            Logger.getLogger(InternalRioNodeConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }



    

    
    public void setNodeService(Object object) {
        this.nodeService = (ExecutionNodeService) object;
        try {
            System.out.println("Setting Node Service = " + nodeService.getId());
        } catch (RemoteException ex) {
            Logger.getLogger(InternalRioNodeConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



}
