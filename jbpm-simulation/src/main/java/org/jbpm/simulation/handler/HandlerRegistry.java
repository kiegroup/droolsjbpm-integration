package org.jbpm.simulation.handler;

import org.eclipse.bpmn2.Activity;
import org.eclipse.bpmn2.AdHocSubProcess;
import org.eclipse.bpmn2.EndEvent;
import org.eclipse.bpmn2.Gateway;
import org.eclipse.bpmn2.GatewayDirection;
import org.eclipse.bpmn2.IntermediateThrowEvent;
import org.eclipse.bpmn2.SubProcess;

public class HandlerRegistry {

    public static ElementHandler getHandler(Object flowElement) {
        
        if (flowElement instanceof Gateway) {
            if (((Gateway) flowElement).getGatewayDirection() == GatewayDirection.CONVERGING) {
                return new ConvergingGatewayElementHandler();
            } else {
                return new GatewayElementHandler();
            }
        }  else if (flowElement instanceof AdHocSubProcess) {
            
            return new AdHocSubProcessElementHandler();
        } else if (flowElement instanceof SubProcess) {
            
            return new EmbeddedSubprocessHandler();
        } else if (flowElement instanceof Activity) {
            
            return new ActivityElementHandler();
        } else if (flowElement instanceof IntermediateThrowEvent) {
            
            return new ThrowEventElementHandler();
        } else if (flowElement instanceof EndEvent) {

            return new EventElementHandler();
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
