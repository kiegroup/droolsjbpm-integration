package org.drools.runtime.pipeline.impl;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;

import org.drools.io.Resource;
import org.drools.runtime.pipeline.JaxbTransformerProvider;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.impl.BaseEmitter;
import org.drools.runtime.pipeline.impl.BaseStage;
import org.xml.sax.InputSource;

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
