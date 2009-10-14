/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.XStreamTransformerProvider;

import com.thoughtworks.xstream.XStream;
 
public class XStreamTransformerProviderImpl implements XStreamTransformerProvider {
    public Transformer newXStreamFromXmlTransformer(XStream xstream) {
        return new XStreamFromXmlTransformer( xstream );
    }
    
    public Transformer newXStreamToXmlTransformer(XStream xstream) {
        return new XStreamToXmlTransformer( xstream );
    }        
}