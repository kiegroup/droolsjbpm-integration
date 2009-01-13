package org.drools.runtime.pipeline.impl;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.drools.runtime.pipeline.Emitter;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Receiver;
import org.drools.runtime.pipeline.impl.BaseEmitter;

public class JmsUnwrapMessageObject extends BaseEmitter implements Receiver, Emitter {

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
