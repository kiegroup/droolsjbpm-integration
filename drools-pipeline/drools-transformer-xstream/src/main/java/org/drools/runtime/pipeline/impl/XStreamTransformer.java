package org.drools.runtime.pipeline.impl;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.Source;

import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.SmooksPipelineProvider;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.XStreamPipelineProvider;
import org.drools.runtime.pipeline.impl.BaseEmitter;
import com.thoughtworks.xstream.XStream;;

public class XStreamTransformer extends BaseEmitter
    implements
    Transformer {
    private XStream                    xstream;   

    public XStreamTransformer(XStream xstream) {
        this.xstream = xstream;

    }

    public void signal(Object object,
                       PipelineContext context) {
        this.xstream.setClassLoader( context.getClassLoader() );
        Object result = null;
        try {
        	if ( object instanceof Reader ) {
        		result = this.xstream.fromXML( ( Reader ) object );
        	} else if ( object instanceof InputStream ) {
        		result = this.xstream.fromXML( ( InputStream ) object );
        	}
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        emit( result,
              context );
    }
    
    public static class XStreamPipelineProviderImpl implements XStreamPipelineProvider {
        public Transformer newXStreamTransformer(XStream xstream) {
            return new XStreamTransformer( xstream );
        }
    }

}
