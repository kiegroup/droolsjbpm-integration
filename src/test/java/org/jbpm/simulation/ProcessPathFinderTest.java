package org.jbpm.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.json.JSONObject;
import org.junit.Test;

public class ProcessPathFinderTest {
    
    @Test
    public void testSinglePath() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.findPath("/BPMN2-UserTask.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        
        JSONObject jsonPaths = finder.getCompletePathsAsJSONObject();
        assertNotNull(jsonPaths);
        assertEquals(1, jsonPaths.length());
        
        printOutPaths(paths, jsonPaths);
        
    }

    @Test
    public void testExclusiveSplit() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.findPath("/BPMN2-ExclusiveSplit.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(2, paths.size());
        
        JSONObject jsonPaths = finder.getCompletePathsAsJSONObject();
        assertNotNull(jsonPaths);
        assertEquals(2, jsonPaths.length());
        
        printOutPaths(paths, jsonPaths);
    }
    
    @Test
    public void testInclusiveSplit() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.findPath("/BPMN2-InclusiveSplit.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(7, paths.size());
        
        JSONObject jsonPaths = finder.getCompletePathsAsJSONObject();
        assertNotNull(jsonPaths);
        assertEquals(7, jsonPaths.length());
        
        printOutPaths(paths, jsonPaths);
    }
    
    @Test
    public void testParallelGateway() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.findPath("/BPMN2-ParallelSplit.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        
        JSONObject jsonPaths = finder.getCompletePathsAsJSONObject();
        assertNotNull(jsonPaths);
        assertEquals(1, jsonPaths.length());
        
        printOutPaths(paths, jsonPaths);
    }
    
    @Test
    public void testParallelAndExclusiveGateway() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.findPath("/BPMN2-ParallelAndExclusiveSplit.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(2, paths.size());
        
        JSONObject jsonPaths = finder.getCompletePathsAsJSONObject();
        assertNotNull(jsonPaths);
        assertEquals(2, jsonPaths.length());
        
        printOutPaths(paths, jsonPaths);
    }
    
    @Test
    public void testMultipleStartEvents() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.findPath("/BPMN2-MultipleStartEventProcess.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(2, paths.size());
        
        JSONObject jsonPaths = finder.getCompletePathsAsJSONObject();
        assertNotNull(jsonPaths);
        assertEquals(2, jsonPaths.length());
        
        printOutPaths(paths, jsonPaths);
    }
    
    @Test
    public void testBoundaryEventOnTask() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.findPath("/BPMN2-BoundaryMessageEventOnTask.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(3, paths.size());
        
        JSONObject jsonPaths = finder.getCompletePathsAsJSONObject();
        assertNotNull(jsonPaths);
        assertEquals(3, jsonPaths.length());
        
        printOutPaths(paths, jsonPaths);
    }
    
    @Test
    public void testSignalThrowEndEventWithCatch() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.findPath("/BPMN2-IntermediateCatchEventSignal.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        
        JSONObject jsonPaths = finder.getCompletePathsAsJSONObject();
        assertNotNull(jsonPaths);
        assertEquals(1, jsonPaths.length());
        
        printOutPaths(paths, jsonPaths);
    }
    
    
    @Test
    public void testEmbeddedSubProcessWithExclusiveSplit() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.findPath("/BPMN2-EmbeddedSubProcessWithExclusiveSplit.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(2, paths.size());
        
        JSONObject jsonPaths = finder.getCompletePathsAsJSONObject();
        assertNotNull(jsonPaths);
        assertEquals(2, jsonPaths.length());
        
        printOutPaths(paths, jsonPaths);
    }
    
    private void printOutPaths(List<PathContext> paths, JSONObject jsonPaths) {
        for (PathContext context : paths) {
            System.out.println("#####################################################");
            System.out.println("## AS TEXT:");
            for (FlowElement fe : context.getPathElements()) {
                System.out.println(fe.getName() + "  - " + fe.eClass().getName());
            }
            System.out.println("## AS JSON:");
            System.out.println(jsonPaths.toString());
            System.out.println("#####################################################");
        }
    }
}
