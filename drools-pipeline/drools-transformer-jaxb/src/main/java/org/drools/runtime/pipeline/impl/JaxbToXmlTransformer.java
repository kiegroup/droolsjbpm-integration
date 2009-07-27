package org.drools.runtime.pipeline.impl;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.drools.result.ExecutionResults;
import org.drools.process.result.ExecutionResultsType;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.impl.ResultTranslator;


public class JaxbToXmlTransformer extends JaxbTransformer implements Transformer {

	ResultTranslator resTrans;

	public JaxbToXmlTransformer( JAXBContext jaxbCtx ) {
		super( jaxbCtx );
	}

	public void receive(Object object, PipelineContext context) {

		Object result = null;
		JAXBContext jaxbCtxt = getPrimaryContext();
		StringWriter stringWriter = new StringWriter();
		try {
			Marshaller marshaller = jaxbCtxt.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, true );

			if( object instanceof ExecutionResults ){
				if( resTrans == null ){
					resTrans = new ResultTranslator( this );
				}
				ExecutionResultsType execRes = resTrans.transform( (ExecutionResults)object );
				marshaller.marshal( execRes, stringWriter );
			} else {
				marshaller.marshal( object, stringWriter );
			}
		} catch ( Exception e ) {
			handleException( this,
					object,
					e );
		}
    	result = stringWriter.getBuffer().toString();

		emit( result,
				context );
	}

}
