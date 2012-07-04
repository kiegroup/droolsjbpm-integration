package org.jbpm.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.junit.Test;

public class ProcessPathFinderTest {
    
    @Test
    public void testSinglePath() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.finPath("/BPMN2-UserTask.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        
        printOutPaths(paths);
    }

    @Test
    public void testExclusiveSplit() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.finPath("/BPMN2-ExclusiveSplit.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(2, paths.size());
        
        printOutPaths(paths);
    }
    
    @Test
    public void testInclusiveSplit() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.finPath("/BPMN2-InclusiveSplit.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(7, paths.size());
        
        printOutPaths(paths);
    }
    
    @Test
    public void testParallelGateway() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.finPath("/BPMN2-ParallelSplit.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        
        printOutPaths(paths);
    }
    
    @Test
    public void testParallelAndExclusiveGateway() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.finPath("/BPMN2-ParallelAndExclusiveSplit.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(2, paths.size());
        
        printOutPaths(paths);
    }
    
    @Test
    public void testMultipleStartEvents() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.finPath("/BPMN2-MultipleStartEventProcess.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(2, paths.size());
        
        printOutPaths(paths);
    }
    
    @Test
    public void testBoundaryEventOnTask() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.finPath("/BPMN2-BoundaryMessageEventOnTask.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(3, paths.size());
        
        printOutPaths(paths);
    }
    
    @Test
    public void testSignalThrowEndEventWithCatch() throws IOException {
        
        ProcessPathFinder finder = new ProcessPathFinder();
        
        finder.finPath("/BPMN2-IntermediateCatchEventSignal.bpmn2");
        
        List<PathContext> paths = finder.getCompletePaths();
        
        assertNotNull(paths);
        assertEquals(1, paths.size());
        
        printOutPaths(paths);
    }
    
    private void printOutPaths(List<PathContext> paths) {
        for (PathContext context : paths) {
            System.out.println("#####################################################");
            for (FlowElement fe : context.getPathElements()) {
                System.out.println(fe.getName() + " " + fe.eClass().getName());
            }
            System.out.println("#####################################################");
        }
    }
}
