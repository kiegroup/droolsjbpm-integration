package org.jbpm.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.jbpm.simulation.converter.JSONPathFormatConverter;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class PathFinderTest {
    
    @Test
    public void testSinglePath() throws IOException {
        List<String> expectedIds = new ArrayList<String>();
        expectedIds.add("_1");
        expectedIds.add("_1-_2");
        expectedIds.add("_2");
        expectedIds.add("_2-_3");
        expectedIds.add("_3");
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-UserTask.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertTrue(matchExpected(paths, expectedIds));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
            assertEquals(1, ((JSONObject)jsonPaths.get("paths")).length());
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        
        printOutPaths(paths, jsonPaths, "testSinglePath");
       
    }

    @Test
    public void testExclusiveSplit() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_1");
        expectedIds1.add("_1-_2");
        expectedIds1.add("_2");
        expectedIds1.add("_2-_3");
        expectedIds1.add("_3");
        expectedIds1.add("_3-_5");
        expectedIds1.add("_5");
        expectedIds1.add("_5-_6");
        expectedIds1.add("_6");
        expectedIds1.add("_6-_7");
        expectedIds1.add("_7");
        
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("_1");
        expectedIds2.add("_1-_2");
        expectedIds2.add("_2");
        expectedIds2.add("_2-_4");
        expectedIds2.add("_4");
        expectedIds2.add("_4-_5");
        expectedIds2.add("_5");
        expectedIds2.add("_5-_6");
        expectedIds2.add("_6");
        expectedIds2.add("_6-_7");
        expectedIds2.add("_7");
        
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
        
        assertNotNull(paths);
        assertEquals(2, paths.size());
        assertTrue(matchExpected(paths, expectedIds1, expectedIds2));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
            assertEquals(2, ((JSONObject)jsonPaths.get("paths")).length());
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        
        printOutPaths(paths, jsonPaths, "testExclusiveSplit");
       
    }
    
    @Test
    public void testInclusiveSplit() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_1");
        expectedIds1.add("_1-_2");
        expectedIds1.add("_2");
        expectedIds1.add("_2-_3");
        expectedIds1.add("_3");
        expectedIds1.add("_3-_6");
        expectedIds1.add("_6");    
        
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("_1");
        expectedIds2.add("_1-_2");
        expectedIds2.add("_2");
        expectedIds2.add("_2-_4");
        expectedIds2.add("_4");
        expectedIds2.add("_4-_7");
        expectedIds2.add("_7");
        
        
        List<String> expectedIds3 = new ArrayList<String>();
        expectedIds3.add("_1");
        expectedIds3.add("_1-_2");
        expectedIds3.add("_2");
        expectedIds3.add("_2-_5");
        expectedIds3.add("_5");
        expectedIds3.add("_5-_8");
        expectedIds3.add("_8");
        
        
        List<String> expectedIds4 = new ArrayList<String>();
        expectedIds4.add("_1");
        expectedIds4.add("_1-_2");
        expectedIds4.add("_2");
        expectedIds4.add("_2-_3");
        expectedIds4.add("_3");
        expectedIds4.add("_3-_6");
        expectedIds4.add("_6"); 
        expectedIds4.add("_2-_4");
        expectedIds4.add("_4");
        expectedIds4.add("_4-_7");
        expectedIds4.add("_7");
        expectedIds4.add("_2-_5");
        expectedIds4.add("_5");
        expectedIds4.add("_5-_8");
        expectedIds4.add("_8");
        
        List<String> expectedIds5 = new ArrayList<String>();
        expectedIds5.add("_1");
        expectedIds5.add("_1-_2");
        expectedIds5.add("_2");
        expectedIds5.add("_2-_3");
        expectedIds5.add("_3");
        expectedIds5.add("_3-_6");
        expectedIds5.add("_6"); 
        expectedIds5.add("_2-_4");
        expectedIds5.add("_4");
        expectedIds5.add("_4-_7");
        expectedIds5.add("_7");
        
        List<String> expectedIds6 = new ArrayList<String>();
        expectedIds6.add("_1");
        expectedIds6.add("_1-_2");
        expectedIds6.add("_2");
        expectedIds6.add("_2-_3");
        expectedIds6.add("_3");
        expectedIds6.add("_3-_6");
        expectedIds6.add("_6");
        expectedIds6.add("_2-_5");
        expectedIds6.add("_5");
        expectedIds6.add("_5-_8");
        expectedIds6.add("_8");
        
        List<String> expectedIds7 = new ArrayList<String>();
        expectedIds7.add("_1");
        expectedIds7.add("_1-_2");
        expectedIds7.add("_2");
        expectedIds7.add("_2-_4");
        expectedIds7.add("_4");
        expectedIds7.add("_4-_7");
        expectedIds7.add("_7");
        expectedIds7.add("_2-_5");
        expectedIds7.add("_5");
        expectedIds7.add("_5-_8");
        expectedIds7.add("_8");
        
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-InclusiveSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(7, paths.size());
        assertTrue(matchExpected(paths, expectedIds1, expectedIds2, expectedIds3, expectedIds4, expectedIds5, expectedIds6, expectedIds7));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
			assertEquals(7, ((JSONObject)jsonPaths.get("paths")).length());
		} catch (JSONException e) {
			fail(e.getMessage());
		}
        
        printOutPaths(paths, jsonPaths, "testInclusiveSplit");
    }
    
    @Test
    public void testParallelGateway() throws IOException {
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_1");
        expectedIds1.add("_1-_2");
        expectedIds1.add("_2");
        expectedIds1.add("_2-_3");
        expectedIds1.add("_3");
        expectedIds1.add("_2-_4");
        expectedIds1.add("_4");
        expectedIds1.add("_3-_5");
        expectedIds1.add("_5");
        expectedIds1.add("_4-_6");
        expectedIds1.add("_6");
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ParallelSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
 
        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertTrue(matchExpected(paths, expectedIds1));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
			assertEquals(1, ((JSONObject)jsonPaths.get("paths")).length());
		} catch (JSONException e) {
			fail(e.getMessage());
		}
        
        printOutPaths(paths, jsonPaths, "testParallelGateway");
        
    }
    
    @Test
    public void testParallelAndExclusiveGateway() throws IOException {
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_1");
        expectedIds1.add("_1-_2");
        expectedIds1.add("_2");
        expectedIds1.add("_2-_3");
        expectedIds1.add("_3");
        expectedIds1.add("SequenceFlow_1");
        expectedIds1.add("ExclusiveGateway_1");
        expectedIds1.add("_2-_4");
        expectedIds1.add("_4");
        expectedIds1.add("SequenceFlow_6");
        expectedIds1.add("_6");
        expectedIds1.add("SequenceFlow_2");
        expectedIds1.add("ScriptTask_2");
        expectedIds1.add("SequenceFlow_5");
        expectedIds1.add("_5");
        
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("_1");
        expectedIds2.add("_1-_2");
        expectedIds2.add("_2");
        expectedIds2.add("_2-_3");
        expectedIds2.add("_3");
        expectedIds2.add("SequenceFlow_1");
        expectedIds2.add("ExclusiveGateway_1");
        expectedIds2.add("_2-_4");
        expectedIds2.add("_4");
        expectedIds2.add("SequenceFlow_6");
        expectedIds2.add("_6");
        expectedIds2.add("SequenceFlow_3");
        expectedIds2.add("ScriptTask_1");
        expectedIds2.add("SequenceFlow_4");
        expectedIds2.add("_5");
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ParallelAndExclusiveSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        assertTrue(matchExpected(paths, expectedIds1, expectedIds2));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
			assertEquals(2, ((JSONObject)jsonPaths.get("paths")).length());
		} catch (JSONException e) {
			fail(e.getMessage());
		}
        
        printOutPaths(paths, jsonPaths, "testParallelAndExclusiveGateway");
    }
    
    @Test
    public void testMultipleStartEvents() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_1");
        expectedIds1.add("SequenceFlow_2");
        expectedIds1.add("ExclusiveGateway_1");
        expectedIds1.add("SequenceFlow_4");
        expectedIds1.add("UserTask_1");
        expectedIds1.add("SequenceFlow_1");
        expectedIds1.add("EndEvent_1");     
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("StartEvent_1");
        expectedIds2.add("SequenceFlow_3");
        expectedIds2.add("ExclusiveGateway_1");
        expectedIds2.add("SequenceFlow_4");
        expectedIds2.add("UserTask_1");
        expectedIds2.add("SequenceFlow_1");
        expectedIds2.add("EndEvent_1");
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-MultipleStartEventProcess.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        assertTrue(matchExpected(paths, expectedIds1, expectedIds2));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
			assertEquals(2, ((JSONObject)jsonPaths.get("paths")).length());
		} catch (JSONException e) {
			fail(e.getMessage());
		}
        
        printOutPaths(paths, jsonPaths, "testMultipleStartEvents");
    }
    
    @Test
    public void testBoundaryEventOnTask() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_1");
        expectedIds1.add("SequenceFlow_1");
        expectedIds1.add("UserTask_1");
        expectedIds1.add("BoundaryEvent_2");
        expectedIds1.add("SequenceFlow_2");
        expectedIds1.add("UserTask_2");
        expectedIds1.add("SequenceFlow_3");    
        expectedIds1.add("EndEvent_1");
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("_1");
        expectedIds2.add("SequenceFlow_1");
        expectedIds2.add("UserTask_1");
        expectedIds2.add("BoundaryEvent_2");
        expectedIds2.add("SequenceFlow_5");
        expectedIds2.add("ScriptTask_1");
        expectedIds2.add("SequenceFlow_4");
        expectedIds2.add("EndEvent_2");
        
        List<String> expectedIds3 = new ArrayList<String>();
        expectedIds3.add("_1");
        expectedIds3.add("SequenceFlow_1");
        expectedIds3.add("UserTask_1");
        expectedIds3.add("BoundaryEvent_2");
        expectedIds3.add("SequenceFlow_2");
        expectedIds3.add("UserTask_2");
        expectedIds3.add("SequenceFlow_3");
        expectedIds3.add("EndEvent_1");
        expectedIds3.add("SequenceFlow_5");
        expectedIds3.add("ScriptTask_1");
        expectedIds3.add("SequenceFlow_4");
        expectedIds3.add("EndEvent_2");
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-BoundaryMessageEventOnTask.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(3, paths.size());
        assertTrue(matchExpected(paths, expectedIds1, expectedIds2, expectedIds3));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
			assertEquals(3, ((JSONObject)jsonPaths.get("paths")).length());
		} catch (JSONException e) {
			fail(e.getMessage());
		}
        
        printOutPaths(paths, jsonPaths, "testBoundaryEventOnTask");
    }
    
    @Test
    public void testSignalThrowEndEventWithCatch() throws IOException {
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_1");
        expectedIds1.add("_1-_2");
        expectedIds1.add("_2");
        expectedIds1.add("_2-_3");
        expectedIds1.add("_3");
        expectedIds1.add("_4");
        expectedIds1.add("_4-_5");
        expectedIds1.add("_5");
        expectedIds1.add("_5-_6");
        expectedIds1.add("_6");
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-IntermediateCatchEventSignal.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertTrue(matchExpected(paths, expectedIds1));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
			assertEquals(1, ((JSONObject)jsonPaths.get("paths")).length());
		} catch (JSONException e) {
			fail(e.getMessage());
		}
        
        printOutPaths(paths, jsonPaths, "testSignalThrowEndEventWithCatch");
    }
    
    
    @Test
    public void testEmbeddedSubProcessWithExclusiveSplit() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("StartEvent_1");
        expectedIds1.add("SequenceFlow_2");
        expectedIds1.add("StartEvent_2");
        expectedIds1.add("SequenceFlow_3");
        expectedIds1.add("ExclusiveGateway_1");
        expectedIds1.add("SequenceFlow_5");
        expectedIds1.add("ScriptTask_1");
        expectedIds1.add("SequenceFlow_7");
        expectedIds1.add("ExclusiveGateway_2");
        expectedIds1.add("SequenceFlow_8");
        expectedIds1.add("EndEvent_2");
        expectedIds1.add("SequenceFlow_1");
        expectedIds1.add("EndEvent_1");
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("StartEvent_1");
        expectedIds2.add("SequenceFlow_2");
        expectedIds2.add("StartEvent_2");
        expectedIds2.add("SequenceFlow_3");
        expectedIds2.add("ExclusiveGateway_1");
        expectedIds2.add("SequenceFlow_10");
        expectedIds2.add("ScriptTask_2");
        expectedIds2.add("SequenceFlow_11");
        expectedIds2.add("ExclusiveGateway_2");
        expectedIds2.add("SequenceFlow_8");
        expectedIds2.add("EndEvent_2");
        expectedIds2.add("SequenceFlow_1");
        expectedIds2.add("EndEvent_1");
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-EmbeddedSubProcessWithExclusiveSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        assertTrue(matchExpected(paths, expectedIds1, expectedIds2));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        
        try {
			assertEquals(2, ((JSONObject)jsonPaths.get("paths")).length());
		} catch (JSONException e) {
			fail(e.getMessage());
		}
        
        printOutPaths(paths, jsonPaths, "testEmbeddedSubProcessWithExclusiveSplit");
    }
    
    @Test
    public void testAdHocProcess() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_062B2E1F-5B9A-4736-830F-CE1701A794F4"); 
        
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("_48322753-5663-47E0-AC6C-6EDA2E65E691");
        
        
        List<String> expectedIds3 = new ArrayList<String>();
        expectedIds3.add("_58BB442E-1052-4AFB-8429-918A34319C80");
        
        
        List<String> expectedIds4 = new ArrayList<String>();
        expectedIds4.add("_4CB6EAA2-C289-4783-9B47-CF1646E5E627");
        expectedIds4.add("_4D90EA4D-A7BA-430B-9B70-475D55F49620");
        expectedIds4.add("_D71F67D1-B368-4AC3-8701-0553B9E5C75A");
        expectedIds4.add("_F8016A84-8E0E-4C52-8E5A-BDD36C7CC12E");
        expectedIds4.add("_B0BC552A-0A60-41F3-8B29-EEFD93352108");
        
        List<String> expectedIds5 = new ArrayList<String>();
        expectedIds5.add("_4CB6EAA2-C289-4783-9B47-CF1646E5E627");
        expectedIds5.add("_4D90EA4D-A7BA-430B-9B70-475D55F49620");
        expectedIds5.add("_D71F67D1-B368-4AC3-8701-0553B9E5C75A");
        expectedIds5.add("_E70AC4B9-CB04-48BF-9475-F9719BD016B3");
        expectedIds5.add("_DAFCB73F-D66C-49CA-9EE4-5AA0B822F49E");

        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-AdHocProcess.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
        
        assertNotNull(paths);
        assertEquals(5, paths.size());
        assertTrue(matchExpected(paths, expectedIds1, expectedIds2, expectedIds3, expectedIds4, expectedIds5));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
			assertEquals(5, ((JSONObject)jsonPaths.get("paths")).length());
		} catch (JSONException e) {
			fail(e.getMessage());
		}
        
        printOutPaths(paths, jsonPaths, "testAdHocProcess");
    }
    
    @Test
    public void testBoundaryEventOnTaskCancelActivity() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_1");
        expectedIds1.add("SequenceFlow_1");
        expectedIds1.add("UserTask_1");
        expectedIds1.add("BoundaryEvent_2");
        expectedIds1.add("SequenceFlow_2");
        expectedIds1.add("UserTask_2");
        expectedIds1.add("SequenceFlow_3");    
        expectedIds1.add("EndEvent_1");
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("_1");
        expectedIds2.add("SequenceFlow_1");
        expectedIds2.add("UserTask_1");
        expectedIds2.add("BoundaryEvent_2");
        expectedIds2.add("SequenceFlow_5");
        expectedIds2.add("ScriptTask_1");
        expectedIds2.add("SequenceFlow_4");
        expectedIds2.add("EndEvent_2");
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-BoundaryMessageEventOnTaskCancel.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        assertTrue(matchExpected(paths, expectedIds1, expectedIds2));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
            assertEquals(2, ((JSONObject)jsonPaths.get("paths")).length());
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        
        printOutPaths(paths, jsonPaths, "testBoundaryEventOnTask");
    }
    
    @Test
    public void testParallelAndExclusiveWithTimer() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_48218CD9-67F4-4B55-AF0F-72F0A44D2FBF");
        expectedIds1.add("_3AC5BE86-60E9-4909-9A09-4C9F4FE29F13");
        expectedIds1.add("_A9FA2923-CF06-49DC-AE4F-D9F9E3EFFC77");
        expectedIds1.add("_0528DF86-8063-4C4E-9963-CA8DC8535264");
        expectedIds1.add("_1DF2159B-5D4D-447E-9B60-62D4CF2DE37A");
        expectedIds1.add("_C3CF4D64-F5B2-4381-921C-C5A5700EA797");
        expectedIds1.add("_2DD42DF2-256B-4F18-B35B-431E59B37281");    
        expectedIds1.add("_E19D4E54-A0DA-4706-B47D-637F5D6D87CD");
        expectedIds1.add("_C104B721-B9DB-498A-9653-35C6BDD7BB27");
        expectedIds1.add("_06430F17-E208-45B0-9923-BABBDBBA4FF8");
        expectedIds1.add("_AA1AE3BC-9DD1-497D-B178-0B2C41984A59");
        expectedIds1.add("_6614FE05-0CF5-4713-BA33-7925526615C4");    
        expectedIds1.add("_A952DDA0-43D7-4731-ADDE-1DFD889CA0F9");
        expectedIds1.add("_0F4D25BC-BFCE-4F45-B8BC-D0DF7BE0EF3D");
        expectedIds1.add("_9FAC6652-C112-411A-B7C4-D3CCB9C201EC");
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("_48218CD9-67F4-4B55-AF0F-72F0A44D2FBF");
        expectedIds2.add("_3AC5BE86-60E9-4909-9A09-4C9F4FE29F13");
        expectedIds2.add("_A9FA2923-CF06-49DC-AE4F-D9F9E3EFFC77");
        expectedIds2.add("_0528DF86-8063-4C4E-9963-CA8DC8535264");
        expectedIds2.add("_1DF2159B-5D4D-447E-9B60-62D4CF2DE37A");
        expectedIds2.add("_C3CF4D64-F5B2-4381-921C-C5A5700EA797");
        expectedIds2.add("_2DD42DF2-256B-4F18-B35B-431E59B37281");
        expectedIds2.add("_E19D4E54-A0DA-4706-B47D-637F5D6D87CD");
        expectedIds2.add("_C104B721-B9DB-498A-9653-35C6BDD7BB27");
        expectedIds2.add("_06430F17-E208-45B0-9923-BABBDBBA4FF8");
        expectedIds2.add("_AA1AE3BC-9DD1-497D-B178-0B2C41984A59");
        expectedIds2.add("_6614FE05-0CF5-4713-BA33-7925526615C4");
        expectedIds2.add("_A952DDA0-43D7-4731-ADDE-1DFD889CA0F9");
        expectedIds2.add("_87E9D397-D130-4557-A79E-6F8C1F67D2DD");
        expectedIds2.add("_3599EC9A-C31A-4833-A0F6-15444C8DC2D9");
        expectedIds2.add("_299A4375-3178-41F6-8890-DF2DC03197C0");
        expectedIds2.add("_E24574AA-58CE-4431-A5FA-1BE4B2C4255B");
        expectedIds2.add("_0BBA6FAE-0E24-4706-A507-2175FDC4EC05");
        expectedIds2.add("_14A3D974-9822-4C5B-A1A0-0BD0FFD6597E");
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ParallelAndExclusiveWithTimer.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        assertTrue(matchExpected(paths, expectedIds1, expectedIds2));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
            assertEquals(2, ((JSONObject)jsonPaths.get("paths")).length());
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        
        printOutPaths(paths, jsonPaths, "testBoundaryEventOnTask");
    }
    
    @Test
    public void testAdHocSubprocess() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_1");
        expectedIds1.add("_1-_2");
        expectedIds1.add("_2-1");
        expectedIds1.add("_2-_3");
        expectedIds1.add("_3");
        expectedIds1.add("_3-_4");    
        expectedIds1.add("_4");
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("_1");
        expectedIds2.add("_1-_2");
        expectedIds2.add("_2-2");
        expectedIds2.add("_2-2-_2-3");
        expectedIds2.add("_2-_3");
        expectedIds2.add("_3");
        expectedIds2.add("_3-_4");
        expectedIds2.add("_4");
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-AdHocSubProcess.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
//        assertTrue(matchExpected(paths, expectedIds1, expectedIds2));
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
            assertEquals(2, ((JSONObject)jsonPaths.get("paths")).length());
        } catch (JSONException e) {
            fail(e.getMessage());
        }
        
        printOutPaths(paths, jsonPaths, "testBoundaryEventOnTask");
    }
    
    private void printOutPaths(List<PathContext> paths, JSONObject jsonPaths, String name) {
    	System.out.println("###################" + name + "###################");
        for (PathContext context : paths) {
            System.out.println("$$$$$$$$ PATH: " + context.getId());
            System.out.println("$$$ AS TEXT:");
            for (FlowElement fe : context.getPathElements()) {
                System.out.println(fe.getName() + "  - " + fe.eClass().getName());
            }
        }
        System.out.println("$$$ AS JSON:");
        System.out.println(jsonPaths.toString());
        System.out.println("$$$$$$$$");
        System.out.println("#####################################################");
    }
    
    private boolean matchExpected(List<PathContext> paths, List<String>... expectedIds) {
        
        for (PathContext context : paths) {
            List<FlowElement> elements = context.getPathElements();
            boolean match = false;
            for (int i = 0; i < expectedIds.length; i++) {
                List<String> expected = expectedIds[i];
                
                if (expected != null && elements.size() == expected.size()) {
                    
                    for (FlowElement fe : elements) {
                        if (!expected.contains(fe.getId())) {
                            System.err.println("Following element not matched: " + fe.getId() + " " + fe.getName());
                            match = false;
                            break;
                        } 
                        match = true;
                    }
                    if (match) {
                        expectedIds[i] = null;
                        break;
                    }
                }
            }
            
            if (!match) {
                return false;
            }
        }
        
        return true;
    }
}
