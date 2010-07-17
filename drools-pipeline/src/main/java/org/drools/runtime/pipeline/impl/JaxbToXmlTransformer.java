package org.drools.runtime.pipeline.impl;

import java.io.StringWriter;

import javax.xml.bind.Marshaller;

import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;

public class JaxbToXmlTransformer extends BaseEmitter
    implements
    Transformer {
    private Marshaller            marshaller;

    public JaxbToXmlTransformer(Marshaller marshaller) {
        this.marshaller = marshaller;
    }

    public void receive(Object object,
                       PipelineContext context) {
        Object result = null;
        try {
            StringWriter stringWriter = new StringWriter();
            
            this.marshaller.marshal( object, stringWriter );

            result = stringWriter.getBuffer().toString();
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        
        emit( result,
              context );
    }

}
