package org.drools.runtime.pipeline.impl;

import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;

import com.thoughtworks.xstream.XStream;



public class XStreamToXmlTransformer extends BaseEmitter
    implements
    Transformer {
    private XStream xstream;

    public XStreamToXmlTransformer(XStream xstream) {
        this.xstream = xstream;

    }

    public void receive(Object object,
                        PipelineContext context) {
        this.xstream.setClassLoader( context.getClassLoader() );
        Object result = null;
        try {
            result = this.xstream.toXML( object );
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        emit( result,
              context );
    }

}
