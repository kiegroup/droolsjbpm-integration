package org.drools.runtime.pipeline.impl;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.Source;

import org.drools.io.Resource;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.SmooksTransformerProvider;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.XStreamTransformerProvider;
import org.drools.runtime.pipeline.impl.BaseEmitter;
import com.thoughtworks.xstream.XStream;

public class XStreamFromXmlTransformer extends BaseEmitter
    implements
    Transformer {
    private XStream                    xstream;   

    public XStreamFromXmlTransformer(XStream xstream) {
        this.xstream = xstream;

    }

    public void receive(Object object,
                       PipelineContext context) {
        this.xstream.setClassLoader( context.getClassLoader() );
        Object result = null;
        try {
        	if ( object instanceof String ) {
        	    result = this.xstream.fromXML( ( String ) object );
        	} else  if ( object instanceof Reader ) {
        		result = this.xstream.fromXML( ( Reader ) object );
        	} else if ( object instanceof InputStream ) {
        		result = this.xstream.fromXML( ( InputStream ) object );
        	} else if ( object instanceof Resource ) {
                result = this.xstream.fromXML( (( Resource ) object).getReader() );
            } else {
                throw new IllegalArgumentException( "signal object must be instance of InputStream or Resource" );
            }
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        emit( result,
              context );
    }
}
