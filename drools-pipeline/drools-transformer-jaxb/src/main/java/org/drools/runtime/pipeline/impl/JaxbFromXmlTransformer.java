package org.drools.runtime.pipeline.impl;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;

import org.drools.io.Resource;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;

import org.xml.sax.InputSource;

public class JaxbFromXmlTransformer extends JaxbTransformer
    implements
    Transformer {

    private CommandTranslator cmdTrans;

    public JaxbFromXmlTransformer( JAXBContext jaxbCtx ) {
        super( jaxbCtx );
    }

    public void receive(Object object, PipelineContext context) {
    	Unmarshaller unmarshaller;
    	try {
    		JAXBContext jaxbContext = getPrimaryContext();
    		unmarshaller = jaxbContext.createUnmarshaller();
    	} catch ( Exception e ) {
    		handleException( this,
    				object,
    				e );
    		return;
    	}

    	Object result = null;
    	try {
    		if ( object instanceof File ) {
    			result = unmarshaller.unmarshal( (File) object );
    		} else if ( object instanceof InputStream ) {
    			result = unmarshaller.unmarshal( (InputStream) object );
    		} else if ( object instanceof Reader ) {
    			result = unmarshaller.unmarshal( (Reader) object );
    		} else if ( object instanceof Source ) {
    			result = unmarshaller.unmarshal( (Source) object );
    		} else if ( object instanceof InputSource ) {
    			result = unmarshaller.unmarshal( (InputSource) object );
    		}  else if ( object instanceof Resource ) {
    			result = unmarshaller.unmarshal( (( Resource ) object).getReader() );
    		}  else if ( object instanceof String ) {
    			result = unmarshaller.unmarshal( new StringReader( ( String ) object ) );
    		} else {
    			throw new IllegalArgumentException( "signal object must be instance of File, InputStream, Reader, Source, InputSource, Resource, String" );
    		}
    	} catch ( Exception e ) {
    		handleException( this,
    				object,
    				e );
    	}

    	if ( result instanceof JAXBElement ) {
    		result = ((JAXBElement<?>) result).getValue();
    	}

    	if( result instanceof BatchExecutionCommand ){
    		if( cmdTrans == null ){
    			cmdTrans = new CommandTranslator( this );
    		}
    		cmdTrans.transform( (BatchExecutionCommand)result );
    	}

    	emit( result,
    			context );
    }

}
