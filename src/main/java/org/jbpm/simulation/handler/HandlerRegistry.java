package org.jbpm.simulation.handler;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.SubProcess;

public class HandlerRegistry {

    public static ElementHandler getHandler(Object flowElement) {
        
        if (flowElement instanceof Gateway) {
            
           return new GatewayElementHandler();
        } else if (flowElement instanceof SubProcess) {
            
            return new EmbeddedSubprocessHandler();
        } else if (flowElement instanceof Activity) {
            
            return new ActivityElementHandler();
        }
        return null;
    }
    
    public static ElementHandler getHandler() {
        return new DefaultElementHandler();
    }
    
    public static ElementHandler getMainHandler() {
        return new MainElementHandler();
    }
}
