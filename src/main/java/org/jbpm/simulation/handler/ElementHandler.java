package org.jbpm.simulation.handler;

import org.eclipse.bpmn2.FlowElement;
import org.jbpm.simulation.PathContextManager;

public interface ElementHandler {

    boolean handle(FlowElement element, PathContextManager manager);
}
