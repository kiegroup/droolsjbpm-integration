/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;

import com.thoughtworks.xstream.XStream;

public class XStreamToXmlGridTransformer extends BaseEmitter
    implements
    Transformer {
    Object result = null;
    public void receive(Object object,
                        PipelineContext context) { 
        this.result = transform(context, object);
        
        emit( result,
              context );
    }

    public Object transform(PipelineContext context, Object object) {
        XStream xstream = (XStream) context.getProperties().get( "xstream-instance" );
        xstream.setClassLoader(context.getClassLoader());
        Object result = null;
        try {
            result = xstream.toXML(object);
        } catch (Exception e) {
            handleException(this, object, e);
        }
        return result;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
    
}