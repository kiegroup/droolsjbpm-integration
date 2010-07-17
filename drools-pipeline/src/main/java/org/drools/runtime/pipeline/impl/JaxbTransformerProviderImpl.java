/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.drools.runtime.pipeline.JaxbTransformerProvider;
import org.drools.runtime.pipeline.Transformer;

public class JaxbTransformerProviderImpl implements JaxbTransformerProvider {
    public Transformer newJaxbFromXmlTransformer(Unmarshaller unmarshaller) {
        return new JaxbFromXmlTransformer( unmarshaller );
    }
    
    public Transformer newJaxbToXmlTransformer(Marshaller marshaller) {
        return new JaxbToXmlTransformer( marshaller );
    }    
}