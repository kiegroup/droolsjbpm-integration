/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import java.util.Properties;

import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.JmsMessengerProvider;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.ResultHandlerFactory;
import org.drools.runtime.pipeline.Service;

public class JmsMessengerProviderImpl
    implements
    JmsMessengerProvider {
    public Service newJmsMessenger(Pipeline pipeline,
                                   Properties properties,
                                   String destinationName,
                                   ResultHandlerFactory resultHandlerFactory) {
        return new JmsMessenger( pipeline,
                                 properties,
                                 destinationName,
                                 resultHandlerFactory );
    }
    
    public Action newJmsUnwrapMessageObject() {
        return new JmsUnwrapMessageObject();
    }

}