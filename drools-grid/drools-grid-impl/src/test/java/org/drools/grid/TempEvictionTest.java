/*
 * Copyright 2010 salaboy.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * under the License.
 */
package org.drools.grid;

import org.drools.time.JobContext;
import org.drools.time.JobHandle;
import java.io.Serializable;
import java.util.Date;
import org.drools.grid.impl.GridNodeServer;
import org.drools.grid.io.MessageReceiverHandler;
import org.drools.grid.impl.GridNodeImpl;
import java.util.Map;
import java.util.HashMap;
import org.drools.command.impl.ContextImplWithEviction;
import org.drools.grid.impl.EvictionJob;
import org.drools.time.Trigger;
import org.drools.time.impl.JDKTimerService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TempEvictionTest {

    private Map<String, GridServiceDescription> coreServicesMap;

    public TempEvictionTest() {
    }

    @Before
    public void setUp() {
        coreServicesMap = new HashMap<String, GridServiceDescription>();
    }


    @Test
    public void simpleEvictionTest() throws InterruptedException {
        MessageReceiverHandler handler = new GridNodeImpl("myNode").getMessageReceiverHandler();
        ContextImplWithEviction contextTemp = (ContextImplWithEviction) ((GridNodeServer) handler).getData().getTemp();
        ((ContextImplWithEviction) contextTemp).setEntryEvictionTime(2000); // 2 seconds 
        ((ContextImplWithEviction) contextTemp).setEvictionWakeUpTime(1000); // 1 seconds
        JDKTimerService timer = new JDKTimerService(1);
        
        contextTemp.set("myvalue", "value");


        Long evictionWakeUpTime = contextTemp.getEvictionWakeUpTime();


        timer.scheduleJob(new EvictionJob(contextTemp), new MockJobContext(), new MockTrigger(new Date(), evictionWakeUpTime));
        //Set the timestamp for the first time
        contextTemp.set("myvalue", "value");

        Thread.sleep(1000);
        
        //Update the timestamp
        String value = (String) contextTemp.get("myvalue");

        //Wait for eviction
        Thread.sleep(4000);

        assertNull((String) contextTemp.get("myvalue"));


    }

    public static class MockTrigger
            implements
            Trigger,
            Serializable {

        private Date date;
        private Date nextTime;
        private long delay;

        public MockTrigger() {
        }

        public MockTrigger(Date date, long delay) {
            this.date = date;
            this.nextTime = date;
            this.delay = delay;

        }

        public Date hasNextFireTime() {
            return this.nextTime;
        }

        public Date nextFireTime() {
            nextTime = new Date(this.date.getTime() + delay);
            this.date = nextTime;
            return this.nextTime;

        }
    }
    
    public static class MockJobContext
        implements
        JobContext,
        Serializable {
        private String text;

        public MockJobContext() {

        }

        public MockJobContext(String text) {
            this.text = text;
        }

        public JobHandle getJobHandle() {
            return null;
        }

        public void setJobHandle(JobHandle jobHandle) {

        }

        public String getText() {
            return this.text;
        }

    }
    
//    @Test
//    public void evictionTemp() throws InterruptedException {
//        Grid grid1 = new GridImpl(new HashMap<String, Object>());
//        configureGrid1(grid1,
//                8000,
//                null);
//
//        Grid grid2 = new GridImpl(new HashMap<String, Object>());
//        configureGrid1(grid2,
//                -1,
//                grid1.get(WhitePages.class));
//
//        GridNode n1 = grid1.createGridNode("n1");
//        grid1.get(SocketService.class).addService("n1", 8000, n1);
//
//        GridServiceDescription<GridNode> n1Gsd = grid2.get(WhitePages.class).lookup("n1");
//        GridConnection<GridNode> conn = grid2.get(ConnectionFactoryService.class).createConnection(n1Gsd);
//        GridNode remoteN1 = conn.connect();
//
//        KnowledgeBuilder kbuilder = remoteN1.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
//
//        assertNotNull(kbuilder);
//
//        String rule = "package test\n"
//                + "rule \"test\""
//                + "  when"
//                + "  then"
//                + "      System.out.println(\"Rule Fired!\");"
//                + " end";
//
//        kbuilder.add(new ByteArrayResource(rule.getBytes()),
//                ResourceType.DRL);
//
//        KnowledgeBuilderErrors errors = kbuilder.getErrors();
//        if (errors != null && errors.size() > 0) {
//            for (KnowledgeBuilderError error : errors) {
//                System.out.println("Error: " + error.getMessage());
//
//            }
//            fail("KnowledgeBase did not build");
//        }
//
//        KnowledgeBase kbase = remoteN1.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
//
//        assertNotNull(kbase);
//
//        kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
//
//        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
//
//        assertNotNull(session);
//
//       
//        
//        Thread.sleep(10000);
//        
//        StatefulKnowledgeSession session2 = kbase.newStatefulKnowledgeSession();
//        assertNotNull(session2);
//        
//        Thread.sleep(10000);
//        session2.insert(new NodeTests.MyObject("something"));
//        int i = session2.fireAllRules();
//        assertEquals(1, i);
//        
//        Thread.sleep(30000);
//        
//         remoteN1.dispose();
//        grid1.get(SocketService.class).close();
//
//    }
//
//    private void configureGrid1(Grid grid,
//            int port,
//            WhitePages wp) {
//
//        //Local Grid Configuration, for our client
//        GridPeerConfiguration conf = new GridPeerConfiguration();
//
//        //Configuring the Core Services White Pages
//        GridPeerServiceConfiguration coreSeviceWPConf = new CoreServicesLookupConfiguration(coreServicesMap);
//        conf.addConfiguration(coreSeviceWPConf);
//
//        //Configuring the Core Services Scheduler
//        GridPeerServiceConfiguration coreSeviceSchedulerConf = new CoreServicesSchedulerConfiguration();
//        conf.addConfiguration(coreSeviceSchedulerConf);
//
//        //Configuring the WhitePages 
//        WhitePagesLocalConfiguration wplConf = new WhitePagesLocalConfiguration();
//        wplConf.setWhitePages(wp);
//        conf.addConfiguration(wplConf);
//
////        //Create a Local Scheduler
////        SchedulerLocalConfiguration schlConf = new SchedulerLocalConfiguration( "myLocalSched" );
////        conf.addConfiguration( schlConf );
//
//        if (port >= 0) {
//            //Configuring the SocketService
//            MultiplexSocketServiceCongifuration socketConf = new MultiplexSocketServiceCongifuration(new MultiplexSocketServerImpl("127.0.0.1",
//                    new MinaAcceptorFactoryService(),
//                    SystemEventListenerFactory.getSystemEventListener(),
//                    grid));
//            socketConf.addService(WhitePages.class.getName(), wplConf.getWhitePages(), port);
////            socketConf.addService( SchedulerService.class.getName(), schlConf.getSchedulerService(), port );
//
//            conf.addConfiguration(socketConf);
//        }
//        conf.configure(grid);
//
//    }
}
