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
