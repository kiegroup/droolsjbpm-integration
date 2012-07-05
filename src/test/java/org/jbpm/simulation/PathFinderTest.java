package org.jbpm.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.jbpm.simulation.converter.JSONPathFormatConverter;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class PathFinderTest {
    
    @Test
    public void testSinglePath() throws IOException {
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-UserTask.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        
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
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ExclusiveSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
        
        assertNotNull(paths);
        assertEquals(2, paths.size());
        
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
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-InclusiveSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(7, paths.size());
        
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
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ParallelSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
 
        assertNotNull(paths);
        assertEquals(1, paths.size());
        
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
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-ParallelAndExclusiveSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        
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
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-MultipleStartEventProcess.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        
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
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-BoundaryMessageEventOnTask.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(3, paths.size());
        
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
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-IntermediateCatchEventSignal.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        
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
        
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-EmbeddedSubProcessWithExclusiveSplit.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        
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
        PathFinder finder = PathFinderFactory.getInstance(this.getClass().getResourceAsStream("/BPMN2-AdHocProcess.bpmn2"));
        
        List<PathContext> paths = finder.findPaths();
        
        assertNotNull(paths);
        assertEquals(5, paths.size());
        
        JSONObject jsonPaths = new JSONPathFormatConverter().convert(paths);
        assertNotNull(jsonPaths);
        try {
			assertEquals(5, ((JSONObject)jsonPaths.get("paths")).length());
		} catch (JSONException e) {
			fail(e.getMessage());
		}
        
        printOutPaths(paths, jsonPaths, "testAdHocProcess");
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
}
