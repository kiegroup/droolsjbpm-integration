package org.drools;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.detection.multicast.MulticastDetector;
import org.jboss.remoting.transporter.TransporterServer;

public class RuleBaseService implements Runnable {
    // Default locator values
    private String locatorURI = "rmi://localhost:5401/?" + InvokerLocator.BYVALUE + "=" + Boolean.TRUE.toString();
    private TransporterServer server = null;
    private MulticastDetector detector;       

   public void start() throws Exception
   {       
      Thread thread = new Thread( this );
      thread.run();       
      server = TransporterServer.createTransporterServer(locatorURI, new DroolsServerImpl() );
      
      // we need an MBeanServer to store our network registry and multicast detector services
      MBeanServer mbeanServer = MBeanServerFactory.createMBeanServer();

      // multicast detector will detect new network registries that come online
      detector = new MulticastDetector();
      mbeanServer.registerMBean(detector, new ObjectName("remoting:type=MulticastDetector"));
      detector.start();
      System.out.println("MulticastDetector has been created and is listening for new NetworkRegistries to come online");      
   }

   public void stop() throws Exception
   {
      if(server != null)
      {
          detector.stop();
          server.stop();
      }
   }
   
   public void exit() throws Exception {
       stop();
       this.server = null;
       this.detector = null;
   }

    public void run() {
        try
        {
            while ( this.server != null ) {
                Thread.sleep( 200 );
            }
        }
        catch(Exception e)
        {
           e.printStackTrace();
        }
        finally
        {
           try {
               stop();
           } catch ( Exception e ) {
               e.printStackTrace();
           }
        }
    }
}
