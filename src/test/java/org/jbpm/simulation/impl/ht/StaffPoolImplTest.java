package org.jbpm.simulation.impl.ht;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.jbpm.simulation.SimulationContextFactory;
import org.jbpm.simulation.SimulationDataProvider;
import org.jbpm.simulation.impl.SimulationPath;
import org.jbpm.simulation.util.SimulationConstants;
import org.jbpm.workflow.core.node.HumanTaskNode;
import org.junit.Test;
import org.kie.definition.process.Node;

public class StaffPoolImplTest {

    @Test
    public void testSingleWorkingHours() {
        HumanTaskNode node = new HumanTaskNode();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(SimulationConstants.STAFF_AVAILABILITY, 1);
        properties.put(SimulationConstants.TIMEUNIT, "min");
        properties.put(SimulationConstants.DISTRIBUTION_TYPE, "exact");
        properties.put("duration", "45");
        
        SimulationContextFactory.newContext(new TestSimulationDataProvider(properties));
        
        
        StaffPool pool = new StaffPoolImpl("test", node , 1);
        long startTime = System.currentTimeMillis();
        System.out.println("Start time is " + new Date(startTime));
        long waitTime = pool.allocate(startTime);
        System.out.println("Wait time is " + DurationFormatUtils.formatDurationHMS(waitTime));
        
        assertEquals(0, waitTime);
        
        startTime += 10*60*1000;
        System.out.println("Start time is " + new Date(startTime));
        waitTime = pool.allocate(startTime);
        System.out.println("Wait time is " + DurationFormatUtils.formatDurationHMS(waitTime));
        assertEquals(35*60*1000, waitTime);
        
        startTime += 10*60*1000;
        System.out.println("Start time is " + new Date(startTime));
        waitTime = pool.allocate(startTime);
        System.out.println("Wait time is " + DurationFormatUtils.formatDurationHMS(waitTime));
        assertEquals(70*60*1000, waitTime);
        
        startTime += 10*60*1000;
        System.out.println("Start time is " + new Date(startTime));
        waitTime = pool.allocate(startTime);
        System.out.println("Wait time is " + DurationFormatUtils.formatDurationHMS(waitTime));
        assertEquals(105*60*1000, waitTime);
    }
    
    @Test
    public void testRangeWorkingHours() {
        HumanTaskNode node = new HumanTaskNode();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(SimulationConstants.STAFF_AVAILABILITY, 1);
        properties.put(SimulationConstants.TIMEUNIT, "min");
        properties.put(SimulationConstants.DISTRIBUTION_TYPE, "exact");
        properties.put("duration", "45");
        properties.put("working.hours.range", "9-11,14-18");
        
        SimulationContextFactory.newContext(new TestSimulationDataProvider(properties));
        
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 9);
        c.set(Calendar.MINUTE, 0);
        
        StaffPool pool = new StaffPoolImpl("test", node , 1);
        long startTime = c.getTimeInMillis();
        System.out.println("Start time is " + new Date(startTime));
        long waitTime = pool.allocate(startTime);
        System.out.println("Wait time is " + DurationFormatUtils.formatDurationHMS(waitTime));
        System.out.println("Complete time is " + new Date(startTime + waitTime + 45*60*1000));
        
        assertEquals(0, waitTime);
        
        startTime += 10*60*1000;
        System.out.println("Start time is " + new Date(startTime));
        waitTime = pool.allocate(startTime);
        System.out.println("Wait time is " + DurationFormatUtils.formatDurationHMS(waitTime));
        System.out.println("Complete time is " + new Date(startTime + waitTime + 45*60*1000));
        assertEquals(35*60*1000, waitTime);
        
        startTime += 10*60*1000;
        System.out.println("Start time is " + new Date(startTime));
        waitTime = pool.allocate(startTime);
        System.out.println("Wait time is " + DurationFormatUtils.formatDurationHMS(waitTime));
        System.out.println("Complete time is " + new Date(startTime + waitTime + 45*60*1000));
        assertEquals(250*60*1000, waitTime);
        
        startTime += 10*60*1000;
        System.out.println("Start time is " + new Date(startTime));
        waitTime = pool.allocate(startTime);
        System.out.println("Wait time is " + DurationFormatUtils.formatDurationHMS(waitTime));
        System.out.println("Complete time is " + new Date(startTime + waitTime + 45*60*1000));
        assertEquals(285*60*1000, waitTime);
    }
    
    private class TestSimulationDataProvider implements SimulationDataProvider {
        
        private Map<String, Object> properties = null;
        
        TestSimulationDataProvider(Map<String, Object> props) {
            this.properties = props;
        }

        public Map<String, Object> getSimulationDataForNode(Node node) {
            
            return this.properties;
        }

        public double calculatePathProbability(SimulationPath path) {
            
            return 0;
        }

        public Map<String, Object> getProcessDataForNode(Node node) {
            return null;
        }
        
    }
}
