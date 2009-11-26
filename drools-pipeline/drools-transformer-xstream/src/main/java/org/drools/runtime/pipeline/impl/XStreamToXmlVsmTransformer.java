/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.drools.vsm.ServiceManager;
import org.w3c.dom.Document;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomReader;

public class XStreamToXmlVsmTransformer extends BaseEmitter
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