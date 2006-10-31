package org.drools;

import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.detection.multicast.MulticastDetector;
import org.jboss.remoting.network.NetworkNotification;
import org.jboss.remoting.network.NetworkRegistry;
import org.jboss.remoting.transporter.TransporterClient;

public class DroolsClient
    implements
    NotificationListener {
    
    private String locatorURI = "rmi://localhost:5400/?" + InvokerLocator.BYVALUE + "=" + Boolean.TRUE.toString();
    
    private Set       servers = new HashSet();
    MBeanServer       server;
    MulticastDetector detector;

    DroolsServer      droolsServer;
    
    /**
     * Sets up NetworkRegistry and MulticastDetector so we can listen for any additions or removals of remoting
     * servers on the network.
     *
     * @throws Exception
     */
    public void start() throws Exception {
        if ( this.detector != null ) {
            return;
        }

        // we need an MBeanServer to store our network registry and multicast detector services
        server = MBeanServerFactory.createMBeanServer();

        // the registry will house all remoting servers discovered
        NetworkRegistry registry = NetworkRegistry.getInstance();
        server.registerMBean( registry,
                              new ObjectName( "remoting:type=NetworkRegistry" ) );
        System.out.println( "NetworkRegistry has been created" );

        // register class as listener, so know when new server found
        registry.addNotificationListener( this,
                                          null,
                                          null );
        System.out.println( "NetworkRegistry has added the client as a listener" );

        // multicast detector will detect new network registries that come online
        detector = new MulticastDetector();
        server.registerMBean( detector,
                              new ObjectName( "remoting:type=MulticastDetector" ) );
        detector.start();
        System.out.println( "MulticastDetector has been created and is listening for new NetworkRegistries to come online" );
    }

    public void stop() throws Exception {
        this.detector.stop();
    }

    public void reset() throws Exception {
        this.detector.stop();
        this.servers.clear();
        this.detector.start();
    }

    //    public static void main(String[] args) {
    //        DroolsClient client = new DroolsClient();
    //        try {
    //            client.start();
    //            Thread.sleep( 1000 );
    //            
    //            client.connect();
    //            
    //            client.stop();
    //
    //        } catch ( Exception e ) {
    //            e.printStackTrace();
    //        }
    //    }   

    public void connect() throws Exception {
//        InvokerLocator locator = (InvokerLocator) this.servers.toArray()[0];
//        DroolsServer server = (DroolsServer) TransporterClient.createTransporterClient( locator.getLocatorURI(),
//                                                                                        DroolsServer.class );
        
        this.droolsServer = (DroolsServer) TransporterClient.createTransporterClient( this.locatorURI,
                                                                                        DroolsServer.class );        
    }

    public void disconnect() {
        TransporterClient.destroyTransporterClient( this.droolsServer );
    }

    /**
     * Callback method from the broadcaster MBean this listener implementation is registered to. When a new server
     * is detected, a welcome message will immediately be sent to the newly discovered server.
     *
     * @param notification the notification object
     * @param handback     the handback object given to the broadcaster upon listener registration
     */
    public void handleNotification(Notification notification,
                                   Object handback) {
        if ( notification instanceof NetworkNotification ) {
            System.out.println( "GOT A NETWORK-REGISTRY NOTIFICATION: " + notification.getType() );

            NetworkNotification networkNotification = (NetworkNotification) notification;

            if ( NetworkNotification.SERVER_ADDED.equals( networkNotification.getType() ) ) { // notification is for new servers being added
                System.out.println( "New server(s) have been detected - getting locators and sending welcome messages" );
                InvokerLocator[] locators = networkNotification.getLocator();
                for ( int i = 0; i < locators.length; i++ ) {
                    this.servers.add( locators[i] );
                }
            } else if ( NetworkNotification.SERVER_REMOVED.equals( networkNotification.getType() ) ) {
                System.out.println( "Server(s) have gone offline - getting locators and sending welcome messages" );
                InvokerLocator[] locators = networkNotification.getLocator();
                for ( int i = 0; i < locators.length; i++ ) {
                    this.servers.remove( locators[i] );
                }
            }
        }

        return;
    }
}
