/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.bpmn2.AdHocSubProcess;
import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.FlowElement;
import org.eclipse.bpmn2.FlowElementsContainer;
import org.eclipse.bpmn2.Process;
import org.eclipse.bpmn2.RootElement;
import org.eclipse.bpmn2.SubProcess;
import org.jbpm.simulation.helper.TestUtils;
import org.jbpm.simulation.util.BPMN2Utils;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class ContainerPathFinderTest {

    @Test
    public void testFindPathFromProcess() {

        List<String> expectedIds = new ArrayList<String>();
        expectedIds.add("_1");
        expectedIds.add("_1-_2");
        expectedIds.add("_2");
        expectedIds.add("_2-_3");
        expectedIds.add("_3");

        Definitions defs = BPMN2Utils.getDefinitions(this.getClass()
                .getResourceAsStream("/BPMN2-UserTask.bpmn2"));
        Process process = null;
        List<RootElement> rootElements = defs.getRootElements();
        for (RootElement root : rootElements) {
            if (root instanceof Process) {
                process = (Process) root;
                break;
            }
        }

        PathFinder finder = PathFinderFactory.getInstance(process);

        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(1, paths.size());
        assertTrue(TestUtils.matchExpected(paths, expectedIds));
        
        TestUtils.printOutPaths(paths, "testFindPathFromProcess");
    }

    @Test
    public void testFindPathFromAdHocSubprocess() {

        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("_2-1");

        List<String> expectedIds2 = new ArrayList<String>();

        expectedIds2.add("_2-2");
        expectedIds2.add("_2-2-_2-3");
        expectedIds2.add("_2-3");
        
        
        Definitions defs = BPMN2Utils.getDefinitions(this.getClass()
                .getResourceAsStream("/BPMN2-AdHocSubProcess.bpmn2"));
        Process process = null;
        List<RootElement> rootElements = defs.getRootElements();
        for (RootElement root : rootElements) {
            if (root instanceof Process) {
                process = (Process) root;
                break;
            }
        }
        assertNotNull(process);
        
        FlowElementsContainer container = null;
        for (FlowElement element : process.getFlowElements()) {
            if (element instanceof AdHocSubProcess) {
                container = (FlowElementsContainer) element;
                break;
            }
        }
        assertNotNull(container);
        
        PathFinder finder = PathFinderFactory.getInstance(container);

        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        
        assertTrue(TestUtils.matchExpected(paths, expectedIds1, expectedIds2));
        
        TestUtils.printOutPaths(paths, "testFindPathFromAdHocSubprocess");
    }
    
    @Test
    public void testFindPathFromEmbeddedSubprocess() throws IOException {
        
        List<String> expectedIds1 = new ArrayList<String>();
        expectedIds1.add("StartEvent_2");
        expectedIds1.add("SequenceFlow_3");
        expectedIds1.add("ExclusiveGateway_1");
        expectedIds1.add("SequenceFlow_5");
        expectedIds1.add("ScriptTask_1");
        expectedIds1.add("SequenceFlow_7");
        expectedIds1.add("ExclusiveGateway_2");
        expectedIds1.add("SequenceFlow_8");
        expectedIds1.add("EndEvent_2");
        
        List<String> expectedIds2 = new ArrayList<String>();
        expectedIds2.add("StartEvent_2");
        expectedIds2.add("SequenceFlow_3");
        expectedIds2.add("ExclusiveGateway_1");
        expectedIds2.add("SequenceFlow_10");
        expectedIds2.add("ScriptTask_2");
        expectedIds2.add("SequenceFlow_11");
        expectedIds2.add("ExclusiveGateway_2");
        expectedIds2.add("SequenceFlow_8");
        expectedIds2.add("EndEvent_2");
        
        Definitions defs = BPMN2Utils.getDefinitions(this.getClass()
                .getResourceAsStream("/BPMN2-EmbeddedSubProcessWithExclusiveSplit.bpmn2"));
        Process process = null;
        List<RootElement> rootElements = defs.getRootElements();
        for (RootElement root : rootElements) {
            if (root instanceof Process) {
                process = (Process) root;
                break;
            }
        }
        assertNotNull(process);
        
        FlowElementsContainer container = null;
        for (FlowElement element : process.getFlowElements()) {
            if (element instanceof SubProcess) {
                container = (FlowElementsContainer) element;
                break;
            }
        }
        assertNotNull(container);
        
        PathFinder finder = PathFinderFactory.getInstance(container);
        
        List<PathContext> paths = finder.findPaths();

        assertNotNull(paths);
        assertEquals(2, paths.size());
        assertTrue(TestUtils.matchExpected(paths, expectedIds1, expectedIds2));
       
        
        TestUtils.printOutPaths(paths, "testFindPathFromEmbeddedSubprocess");
    }
    
    
}
