package org.drools.runtime.pipeline.impl;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.drools.runtime.pipeline.Emitter;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Receiver;
import org.drools.runtime.pipeline.impl.BaseEmitter;

public class JmsUnwrapText  extends BaseEmitter implements Receiver, Emitter {

    public void receive(Object object,
                       PipelineContext context) {
        try {
            String string = ((TextMessage)object).getText();
            emit( string, context );
        } catch ( JMSException e ) {
            handleException( this, object, e );
        }
    }

}