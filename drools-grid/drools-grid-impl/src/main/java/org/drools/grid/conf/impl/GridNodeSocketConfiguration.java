/**
 * 
 */
package org.drools.grid.conf.impl;

import org.drools.grid.conf.GridPeerServiceConfiguration;
import org.drools.grid.service.directory.impl.*;
import org.drools.grid.CoreServicesLookup;

import org.drools.grid.Grid;
import org.drools.grid.GridNode;
import org.drools.grid.MessageReceiverHandlerFactoryService;
import org.drools.grid.SocketService;

public class GridNodeSocketConfiguration
    implements
    GridPeerServiceConfiguration {
    private int port = -1;

    public GridNodeSocketConfiguration(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void configureService(Grid grid) {
        GridNode gnode = grid.get( GridNode.class );

        if ( port != -1 ) {
            CoreServicesWhitePagesImpl coreServicesWP = (CoreServicesWhitePagesImpl) grid.get( CoreServicesLookup.class );

            GridServiceDescriptionImpl gsd = (GridServiceDescriptionImpl) coreServicesWP.lookup( GridNode.class );
            if ( gsd == null ) {
                gsd = new GridServiceDescriptionImpl( GridNode.class );
            }

            SocketService mss = grid.get( SocketService.class );

            //            GridServiceDescription service = coreServicesWP.getServices().get( SchedulerService.class.getName() );
            //            if( service == null){
            //                coreServicesWP.getServices().put(SchedulerService.class.getName(), gsd);
            //                service = gsd;
            //            }
            //            Address address = null;
            //            if(service.getAddresses().get("socket") != null){
            //                address = service.getAddresses().get("socket");
            //            } else{
            //                address = service.addAddress( "socket" );
            //            }
            //            InetSocketAddress[] addresses = (InetSocketAddress[])address.getObject();
            //            if(addresses != null && addresses.length >= 1){
            //                 InetSocketAddress[] newAddresses = new InetSocketAddress[addresses.length+1];
            //                if(addresses !=null){
            //                    System.arraycopy(addresses, 0, newAddresses, 0, addresses.length);
            //                }
            //                newAddresses[addresses.length]= new InetSocketAddress( mss.getIp(),
            //                                                             this.port);
            //                 ServiceConfiguration conf = new SchedulerServiceConfiguration(newAddresses);
            //                 service.setData(conf);
            //            }else{
            //                 InetSocketAddress[] newAddress = new InetSocketAddress[1];
            //                 newAddress[0]= new InetSocketAddress( mss.getIp(),
            //                                                         this.port);
            //                 address.setObject(  newAddress );
            //                 ServiceConfiguration conf = new SchedulerServiceConfiguration(newAddress);
            //                 service.setData(conf);
            //            }

//            mss.addService( this.port,
//                            GridNode.class.getName(),
//                            ((MessageReceiverHandlerFactoryService) gnode).getMessageReceiverHandler() );
        }
    }
}