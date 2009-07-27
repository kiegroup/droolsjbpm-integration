package org.drools.runtime.pipeline.impl;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.drools.process.result.ExecutionResultsType;
import org.drools.runtime.pipeline.impl.BaseEmitter;

/**
 * @author: Wolfgang Laun
 */
public class JaxbTransformer extends BaseEmitter {

    public final static String COMMAND_PACKAGES =
        "org.drools.command.runtime"         + ":" +
        "org.drools.command.runtime.process" + ":" +
        "org.drools.command.runtime.rule";

    public final static String RESULT_PACKAGE =
        "org.drools.process.result";

    private   Map<Object,JAXBContext> class2ctxt;

    protected JAXBContext jaxbContext;
    protected JAXBContext jaxbPrimaryContext;
    
    protected JaxbTransformer( JAXBContext jaxbCtx ){
    	this.jaxbContext        = jaxbCtx;
    	this.jaxbPrimaryContext = jaxbCtx;
        class2ctxt = new HashMap<Object,JAXBContext>();
    }

    public void addContextForCommands() {
    	try {
    		jaxbPrimaryContext = getContext( JaxbTransformer.COMMAND_PACKAGES );
    	} catch ( Exception e ) {
    		handleException( this,
    				null,
    				e );
    		return;
    	}
    }

    public void addContextForResults() {
    	try {
    		jaxbPrimaryContext = getContext( ExecutionResultsType.class );
    	} catch ( Exception e ) {
    		handleException( this,
    				null,
    				e );
    		return;
    	}
    }

    
    protected JAXBContext getContext(){
    	return this.jaxbContext;
    }
    
    protected JAXBContext getPrimaryContext(){
    	return this.jaxbPrimaryContext;
    }
    
    protected JAXBContext getContext( Class<?> clazz ) throws JAXBException{
    	JAXBContext ctxt = class2ctxt.get( clazz );
    	if( ctxt == null ){
            ctxt = JAXBContext.newInstance( clazz );
            class2ctxt.put( clazz, ctxt );
        }
        return ctxt;
    }

    protected JAXBContext getContext( String packagePath ) throws JAXBException{
    	JAXBContext ctxt = class2ctxt.get( packagePath );
    	if( ctxt == null ){
            ctxt = JAXBContext.newInstance( packagePath );
            class2ctxt.put( packagePath, ctxt );
        }
        return ctxt;
    }

}
