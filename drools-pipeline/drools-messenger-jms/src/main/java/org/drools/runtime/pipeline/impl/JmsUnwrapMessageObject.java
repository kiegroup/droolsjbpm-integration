package org.drools.runtime.pipeline.impl;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Receiver;

public class JmsUnwrapMessageObject extends BaseEmitter implements Action, Receiver {

    public void receive(Object object,
                       PipelineContext context) {
        try {
            Object result = ((ObjectMessage)object).getObject();
            emit( result, context );
        } catch ( JMSException e ) {
            handleException( this, object, e );
        }
    }

}
