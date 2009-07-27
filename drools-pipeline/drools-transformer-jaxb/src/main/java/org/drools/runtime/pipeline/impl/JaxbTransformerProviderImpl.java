package org.drools.runtime.pipeline.impl;

import javax.xml.bind.JAXBContext;

import org.drools.runtime.pipeline.JaxbTransformerProvider;
import org.drools.runtime.pipeline.Transformer;

public class JaxbTransformerProviderImpl implements JaxbTransformerProvider {
    public Transformer newJaxbFromXmlTransformer( JAXBContext jaxbCtx ) {
        return new JaxbFromXmlTransformer( jaxbCtx );
    }
    
    public Transformer newJaxbFromXmlCommandTransformer( JAXBContext jaxbCtx ) {
    	JaxbFromXmlTransformer trans = new JaxbFromXmlTransformer( jaxbCtx );
    	trans.addContextForCommands();
    	return trans;
    }

    public Transformer newJaxbToXmlTransformer( JAXBContext jaxbCtx ) {
        return new JaxbToXmlTransformer( jaxbCtx );
    }    

    public Transformer newJaxbToXmlResultTransformer( JAXBContext jaxbCtx ) {
    	JaxbToXmlTransformer trans = new JaxbToXmlTransformer( jaxbCtx );
    	trans.addContextForResults();
    	return trans;
    }

}