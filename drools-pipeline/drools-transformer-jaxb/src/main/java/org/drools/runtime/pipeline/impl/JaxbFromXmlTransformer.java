package org.drools.runtime.pipeline.impl;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;

import org.drools.io.Resource;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.impl.BaseEmitter;
import org.drools.runtime.pipeline.impl.BaseStage;
import org.xml.sax.InputSource;

public class JaxbFromXmlTransformer extends BaseEmitter
    implements
    Transformer {
    private Unmarshaller            unmarshaller;

    public JaxbFromXmlTransformer(Unmarshaller unmarshaller) {
        this.unmarshaller = unmarshaller;
    }

    public void receive(Object object,
                       PipelineContext context) {
        Object result = null;
        try {
            if ( object instanceof File ) {
                result = this.unmarshaller.unmarshal( (File) object );
            } else if ( object instanceof InputStream ) {
                result = this.unmarshaller.unmarshal( (InputStream) object );
            } else if ( object instanceof Reader ) {
                result = this.unmarshaller.unmarshal( (Reader) object );
            } else if ( object instanceof Source ) {
                result = this.unmarshaller.unmarshal( (Source) object );
            } else if ( object instanceof InputSource ) {
                result = this.unmarshaller.unmarshal( (InputSource) object );
            }  else if ( object instanceof Resource ) {
                result = this.unmarshaller.unmarshal( (( Resource ) object).getReader() );
            }  else if ( object instanceof String ) {
                result = this.unmarshaller.unmarshal( new StringReader( ( String ) object ) );
            } else {
                throw new IllegalArgumentException( "signal object must be instance of File, InputStream, Reader, Source, InputSource, Resource, String" );
            }
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        
        if ( result instanceof JAXBElement ) {
            result = ((JAXBElement) object).getValue().getClass().getName();
        }
        
        emit( result,
              context );
    }

}
