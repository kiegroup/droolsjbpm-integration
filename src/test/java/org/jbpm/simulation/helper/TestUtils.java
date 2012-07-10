package org.jbpm.simulation.helper;

import java.util.List;

import org.eclipse.bpmn2.FlowElement;
import org.jbpm.simulation.PathContext;
import org.json.JSONObject;

public class TestUtils {

    public static boolean matchExpected(List<PathContext> paths, List<String>... expectedIds) {
        
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
    
    public static void printOutPaths(List<PathContext> paths, String name) {
        if (!"true".equalsIgnoreCase(System.getProperty("test.debug.off"))) {
            System.out.println("###################" + name + "###################");
            for (PathContext context : paths) {
                System.out.println("PATH: " + context.getId());
                System.out.println("AS TEXT:");
                for (FlowElement fe : context.getPathElements()) {
                    System.out.println(fe.getName() + "  - " + fe.eClass().getName());
                }
            }
            System.out.println("#####################################################");
        }
    }
    
    public static void printOutPaths(List<PathContext> paths, JSONObject jsonPaths, String name) {
        if (!"true".equalsIgnoreCase(System.getProperty("test.debug.off"))) {
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
}
