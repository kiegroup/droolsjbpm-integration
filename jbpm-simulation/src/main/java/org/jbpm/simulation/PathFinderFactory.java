package org.jbpm.simulation;

import java.io.File;
import java.io.InputStream;

import org.eclipse.bpmn2.Definitions;
import org.eclipse.bpmn2.FlowElementsContainer;
import org.jbpm.simulation.impl.BPMN2PathFinderImpl;

public class PathFinderFactory {

    public static PathFinder getInstance(String bpmn2Xml) {
        return new BPMN2PathFinderImpl(bpmn2Xml);
    }
    
    public static PathFinder getInstance(File bpmn2Xml) {
        return new BPMN2PathFinderImpl(bpmn2Xml);
    }
    
    public static PathFinder getInstance(Definitions bpmn2Defs) {
        return new BPMN2PathFinderImpl(bpmn2Defs);
    }
    
    public static PathFinder getInstance(InputStream bpmn2Stream) {
        return new BPMN2PathFinderImpl(bpmn2Stream);
    }
    
    public static PathFinder getInstance(FlowElementsContainer bpmn2Container) {
        return new BPMN2PathFinderImpl(bpmn2Container);
    }
}
