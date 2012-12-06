package org.drools.grid.time.impl;

import java.util.Map;

import org.drools.grid.GridServiceDescription;
import org.drools.grid.io.Acceptor;
import org.drools.grid.remote.mina.MinaAcceptor;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.SystemEventListener;
import org.kie.SystemEventListenerFactory;

public class DistributedSchedulerTest {

    private Acceptor                            acc = new MinaAcceptor();
    private SystemEventListener                 l   = SystemEventListenerFactory.getSystemEventListener();
    private Map<String, GridServiceDescription> coreServicesMap;

    @After
    public void tearDown() {
        MockJob.counter = 0;
    }


    @Test @Ignore
    public void testDummy() {
        
    }

//    public void test1() throws Exception {
//
//        MessageReceiverHandler accHandler = new MessageReceiverHandler() {
//
//            private String     id;
//            private AtomicLong counter = new AtomicLong();
//
//            public void messageReceived(Conversation conversation,
//                                        Message msgIn) {
//                conversation.respond( "echo: " + msgIn.getBody() );
//            }
//        };
//
//        acc.open( new InetSocketAddress( "127.0.0.1",
//                                         5012 ),
//                  accHandler,
//                  l );
//
//        Connector conn = new MinaConnector();
//
//        ConversationManager cm = new ConversationManagerImpl( "s1",
//                                                              conn,
//                                                              l );
//
//        Conversation cv = cm.startConversation( new InetSocketAddress( "127.0.0.1",
//                                                                       5012 ),
//                                                "r1" );
//
//        BlockingMessageResponseHandler blockHandler = new BlockingMessageResponseHandler();
//
//        cv.sendMessage( "hello",
//                        blockHandler );
//
//        Message msg = blockHandler.getMessage( 5000 );
//        System.out.println( msg.getBody() );
//        conn.close();
//        acc.close();
//    }
//
//    public void testDistributedJobSchedullingLocal() {
//
//        GridImpl grid = new GridImpl( new ConcurrentHashMap<String, Object>() );
//        grid.addService( SchedulerService.class,
//                         new SchedulerImpl( "myLocalSched",
//                                            grid ) );
//
//        SchedulerService scheduler = grid.get( SchedulerService.class );
//
//        UuidJobHandle handle = new UuidJobHandle();
//        ScheduledJob sj1 = new ScheduledJob( handle,
//                                             new MockJob(),
//                                             new MockJobContext( "xxx" ),
//                                             new MockTrigger( new Date( 1000 ) ),
//                                             new ScheduledJobConfiguration( 1 ) );
//        ScheduledJob sj2 = new ScheduledJob( handle,
//                                             new MockJob(),
//                                             new MockJobContext( "xxx" ),
//                                             new MockTrigger( new Date( 1000 ) ),
//                                             new ScheduledJobConfiguration( 1 ) );
//
//        scheduler.scheduleJob( new MockJob(),
//                               new MockJobContext( "xxx" ),
//                               new MockTrigger( new Date( 1000 ) ) );
//        //The Job Will be executed in 1 second
//        try {
//            Thread.sleep( 1000 );
//        } catch ( InterruptedException ex ) {
//            Logger.getLogger( DistributedSchedulerTest.class.getName() ).log( Level.SEVERE,
//                                                                              null,
//                                                                              ex );
//        }
//        assertEquals( 1,
//                      MockJob.counter );
//
//    }
//
//    /*
//     * Test Including:
//     *    - 1 Core Service White Pages 
//     *    - 1 Core Service Scheduler
//     *    - 1 MultiplexService 
//     *    - 1 White Pages (Local)
//     *    - 1 Scheduler (Local)
//     *
//     */
//    public void testDistributedJobSchedulingRemote() {
//        //Core services Map Definition
//        coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesLookup.class.getName() );
//
//        //Grid View 
//        GridImpl grid1 = new GridImpl( new ConcurrentHashMap<String, Object>() );
//        //Configure grid with: 
//        //  core whitepages
//        //  core scheduler
//        //  local whitepages
//        //  local scheduler
//        //  expose multiplex socket
//        configureGrid1( grid1,
//                        5012 );
//
//        GridImpl grid2 = new GridImpl( new ConcurrentHashMap<String, Object>() );
//        Connector conn = new MinaConnector();
//        configureGrid2( grid2,
//                        conn );
//
//        //Create a Job
//        UuidJobHandle handle = new UuidJobHandle();
//        ScheduledJob sj1 = new ScheduledJob( handle,
//                                             new MockJob(),
//                                             new MockJobContext( "xxx" ),
//                                             new MockTrigger( new Date( 1000 ) ),
//                                             new ScheduledJobConfiguration( 1 ) );
//
//        //From grid2 I get the Scheduler (that it's a client)
//        SchedulerService scheduler = grid2.get( SchedulerService.class );
//
//        //Schedule remotely the Job
//        scheduler.scheduleJob( new MockJob(),
//                               new MockJobContext( "xxx" ),
//                               new MockTrigger( new Date( 1000 ) ) );
//        try {
//            Thread.sleep( 1000 );
//        } catch ( InterruptedException ex ) {
//            Logger.getLogger( DistributedSchedulerTest.class.getName() ).log( Level.SEVERE,
//                                                                              null,
//                                                                              ex );
//        }
//
//        assertEquals( 1,
//                      MockJob.counter );
//
//        //Close the peer connection
//        conn.close();
//
//        //Shutdown the SocketService
//        grid1.get( SocketService.class ).close();
//
//    }
//
//    public void testMultipleSchedulersTest() {
//        //Core services Map Definition
//        coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesLookup.class.getName() );
//
//        //Grid View 
//        GridImpl grid1 = new GridImpl( new ConcurrentHashMap<String, Object>() );
//        configureGrid1( grid1,
//                        5012 );
//
//        GridImpl grid2 = new GridImpl( new ConcurrentHashMap<String, Object>() );
//        configureGrid1( grid2,
//                        5013 );
//
//        GridImpl grid3 = new GridImpl( new ConcurrentHashMap<String, Object>() );
//        Connector conn = new MinaConnector();
//        configureGrid3( grid3,
//                        conn );
//
//        CoreServicesLookup corewp = grid3.get( CoreServicesLookup.class );
//
//        GridServiceDescription gsd = corewp.lookup( SchedulerService.class );
//
//        assertEquals( 1,
//                             ((InetSocketAddress[]) gsd.getAddresses().values().iterator().next().getObject()).length );
//
//        assertEquals( 2,
//                             ((InetSocketAddress[]) ((SchedulerServiceConfiguration) gsd.getData()).getServices( grid3 )).length );
//
//        assertEquals( 0,
//                             MockJob.counter );
//
//        conn.close();
//        grid1.get( SocketService.class ).close();
//        grid2.get( SocketService.class ).close();
//
//    }
//
//    public void testGetDataFromCoreServices() {
//
//        coreServicesMap = new HashMap<String, GridServiceDescription>();//Hazelcast.newHazelcastInstance( null ).getMap( CoreServicesLookup.class.getName() );
//
//        //Grid View 
//        GridImpl grid1 = new GridImpl( new ConcurrentHashMap<String, Object>() );
//        configureGrid1( grid1,
//                        5012 );
//
//        GridImpl grid2 = new GridImpl( new ConcurrentHashMap<String, Object>() );
//        configureGrid1( grid2,
//                        5013 );
//
//        CoreServicesLookup corewp = grid1.get( CoreServicesLookup.class );
//
//        //Get Scheduler Service
//        GridServiceDescription gsd = corewp.lookup( SchedulerService.class );
//
//        assertEquals( 1,
//                             ((InetSocketAddress[]) gsd.getAddresses().values().iterator().next().getObject()).length );
//
//        assertEquals( 2,
//                             ((InetSocketAddress[]) ((SchedulerServiceConfiguration) gsd.getData()).getServices( grid1 )).length );
//
//        Connector conn = new MinaConnector();
//
//        ConversationManager cm = new ConversationManagerImpl( "s1",
//                                                              conn,
//                                                              l );
//
//        SchedulerClient schedulerClient = new SchedulerClient( grid1,
//                                                               gsd,
//                                                               cm );
//        ((SchedulerServiceConfiguration) gsd.getData()).setRedundancy( 3 );
//
//        JobHandle handle = schedulerClient.scheduleJob( new MockJob(),
//                                                        new MockJobContext( "xxx" ),
//                                                        new MockTrigger( new Date( 1000 ) ) );
//        try {
//            Thread.sleep( 1000 );
//        } catch ( InterruptedException ex ) {
//            Logger.getLogger( DistributedSchedulerTest.class.getName() ).log( Level.SEVERE,
//                                                                              null,
//                                                                              ex );
//        }
//
//        assertEquals( 3,
//                      MockJob.counter );
//
//        conn.close();
//        grid1.get( SocketService.class ).close();
//        grid2.get( SocketService.class ).close();
//
//    }
//
//    public static class MockJobContext
//        implements
//        JobContext,
//        Serializable {
//
//        private String text;
//
//        public MockJobContext() {
//        }
//
//        public MockJobContext(String text) {
//            this.text = text;
//        }
//
//        public JobHandle getJobHandle() {
//            return null;
//        }
//
//        public void setJobHandle(JobHandle jobHandle) {
//        }
//
//        public String getText() {
//            return this.text;
//        }
//    }
//
//    public static class MockTrigger
//        implements
//        Trigger,
//        Serializable {
//
//        private Date date;
//
//        public MockTrigger() {
//        }
//
//        public MockTrigger(Date date) {
//            this.date = date;
//
//        }
//
//        public Date hasNextFireTime() {
//            return this.date;
//        }
//
//        public Date nextFireTime() {
//            Date tmp = new Date();
//            tmp.setTime( this.date.getTime() );
//            this.date = null;
//            return tmp;
//        }
//    }
//
//    public static class DisTimerService
//            implements
//            TimerService {
//
//        public long getCurrentTime() {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        public long getTimeToNextJob() {
//            // TODO Auto-generated method stub
//            return 0;
//        }
//
//        public boolean removeJob(JobHandle jobHandle) {
//            // TODO Auto-generated method stub
//            return false;
//        }
//
//        public JobHandle scheduleJob(Job job,
//                                     JobContext ctx,
//                                     Trigger trigger) {
//            // TODO Auto-generated method stub
//            return null;
//        }
//
//        public void shutdown() {
//            // TODO Auto-generated method stub
//        }
//    }
//
//    public static class TimerServiceClusterManager {
//
//        private String[] ids;
//        private int      redundancyCount;
//
//        public void configure(String[] ids,
//                              int redundancyCount) {
//            this.ids = ids;
//            this.redundancyCount = redundancyCount;
//            if ( redundancyCount >= ids.length ) {
//                throw new IllegalArgumentException( "Redundancy must be less than or equal to to total-1" );
//            }
//        }
//
//        private int indexOf(final int hashCode,
//                            final int dataSize) {
//            return hashCode & (dataSize - 1);
//        }
//    }
//
//    public static class RemoteTimerService
//            implements
//            TimerService {
//
//        public long getCurrentTime() {
//            throw new UnsupportedOperationException( "not supported" );
//        }
//
//        public long getTimeToNextJob() {
//            throw new UnsupportedOperationException( "not supported" );
//        }
//
//        public boolean removeJob(JobHandle jobHandle) {
//            return false;
//        }
//
//        public JobHandle scheduleJob(Job job,
//                                     JobContext ctx,
//                                     Trigger trigger) {
//            UuidJobHandle jhandle = new UuidJobHandle();
//
//            ScheduledJob sj = new ScheduledJob( jhandle,
//                                                job,
//                                                ctx,
//                                                trigger );
//            return jhandle;
//        }
//
//        public void shutdown() {
//            throw new UnsupportedOperationException( "not supported" );
//        }
//    }
//
//    private void configureGrid1(Grid grid,
//                                int port) {
//
//        //Local Grid Configuration, for our client
//        GridPeerConfiguration conf = new GridPeerConfiguration();
//
//        //Configuring the Core Services White Pages
//        GridPeerServiceConfiguration coreSeviceWPConf = new CoreServicesLookupConfiguration( coreServicesMap );
//        conf.addConfiguration( coreSeviceWPConf );
//
//        //Configuring the Core Services Scheduler
//        GridPeerServiceConfiguration coreSeviceSchedulerConf = new CoreServicesSchedulerConfiguration();
//        conf.addConfiguration( coreSeviceSchedulerConf );
//
//        //Configuring the SocketService
//        GridPeerServiceConfiguration socketConf = new MultiplexSocketServiceCongifuration( new MultiplexSocketServerImpl( "127.0.0.1",
//                                                                                                                          new MinaAcceptorFactoryService(),
//                                                                                                                          l ) );
//        conf.addConfiguration( socketConf );
//
//        //Configuring the WhitePages 
//        GridPeerServiceConfiguration wplConf = new WhitePagesLocalConfiguration();
//        conf.addConfiguration( wplConf );
//
//        //Exposing Local WhitePages
//        GridPeerServiceConfiguration wpsc = new WhitePagesSocketConfiguration( port );
//        conf.addConfiguration( wpsc );
//        GridPeerServiceConfiguration registerwpincore = new RegisterWhitePagesConfiguration();
//        conf.addConfiguration( registerwpincore );
//
//        //Create a Local Scheduler
//        GridPeerServiceConfiguration schlConf = new SchedulerLocalConfiguration( "myLocalSched" );
//        conf.addConfiguration( schlConf );
//
//        //Expose it to the Grid so it can be accesed by different nodes
//        // I need to use the same port to reuse the service multiplexer
//        GridPeerServiceConfiguration schlsc = new SchedulerSocketConfiguration( port );
//        conf.addConfiguration( schlsc );
//
//        GridPeerServiceConfiguration registerschedincore = new RegisterSchedulerConfiguration();
//        conf.addConfiguration( registerschedincore );
//
//        conf.configure( grid );
//
//    }
//
//    private void configureGrid2(Grid grid2,
//                                Connector conn) {
//        GridPeerConfiguration conf = new GridPeerConfiguration();
//
//        GridPeerServiceConfiguration coreSeviceWPConf = new CoreServicesLookupConfiguration( coreServicesMap );
//        conf.addConfiguration( coreSeviceWPConf );
//
//        ConversationManager cm = new ConversationManagerImpl( "s1",
//                                                              conn,
//                                                              l );
//
//        GridPeerServiceConfiguration wprConf = new WhitePagesRemoteConfiguration( cm );
//        conf.addConfiguration( wprConf );
//
//        GridPeerServiceConfiguration schedRemoteClientConf = new SchedulerRemoteConfiguration( cm );
//        conf.addConfiguration( schedRemoteClientConf );
//
//        conf.configure( grid2 );
//    }
//
//    private void configureGrid3(Grid grid3,
//                                Connector conn) {
//        GridPeerConfiguration conf = new GridPeerConfiguration();
//
//        GridPeerServiceConfiguration coreSeviceWPConf = new CoreServicesLookupConfiguration( coreServicesMap );
//        conf.addConfiguration( coreSeviceWPConf );
//
//        ConversationManager cm = new ConversationManagerImpl( "s1",
//                                                              conn,
//                                                              l );
//
//        GridPeerServiceConfiguration wprConf = new WhitePagesRemoteConfiguration( cm );
//        conf.addConfiguration( wprConf );
//
//        GridPeerServiceConfiguration schedRemoteClientConf = new SchedulerRemoteConfiguration( cm );
//        conf.addConfiguration( schedRemoteClientConf );
//
//        conf.configure( grid3 );
//    }
}
