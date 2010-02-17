package org.drools.camel.component;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.io.Resource;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.impl.ExecutionResultImpl;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.impl.CommandTranslator;
import org.drools.runtime.pipeline.impl.ResultTranslator;
import org.drools.runtime.pipeline.impl.ServiceManagerPipelineContextImpl;
import org.drools.vsm.ServiceManager;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * 
 * @author Lucas Amador
 * 
 */
public class DroolsJaxbDataFormat implements DataFormat {

	private CommandTranslator cmdTranslator;
	private ResultTranslator resTranslator;
	private String charset;
	
	public DroolsJaxbDataFormat() {
		this.cmdTranslator = new CommandTranslator();
		this.resTranslator = new ResultTranslator();
	}

	public void marshal(Exchange exchange, Object graph, OutputStream stream)
	throws Exception {

		Object body = exchange.getIn().getBody();
		
		JAXBContext jaxbContext = (JAXBContext) exchange.getProperty("jaxb-context");
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		
		String result = null;
		
		if (body instanceof ExecutionResults) {
			result = resTranslator.transform((ExecutionResultImpl) body, marshaller);
		}

		byte[] bytes;
        if (charset != null) {
            bytes = result.getBytes(charset);
        } else {
            bytes = result.getBytes();
        }
		
		stream.write(bytes);

	}

	public Object unmarshal(Exchange exchange, InputStream stream) throws Exception {
		
		JAXBContext jaxbContext = (JAXBContext) exchange.getIn().getHeader("jaxb-context");

		PipelineContext context = (PipelineContext) exchange.getProperty("drools-context");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		Document d = exchange.getIn().getBody(Document.class);
		Object body = exchange.getIn().getBody();
		String name = d.getDocumentElement().getAttribute("lookup");
		ServiceManagerPipelineContextImpl vsmContext = (ServiceManagerPipelineContextImpl) exchange.getProperty("drools-context");
		ServiceManager sm = vsmContext.getServiceManager();
		CommandExecutor executor = sm.lookup(name);
		if (executor == null) {
			throw new IllegalArgumentException("Unable to lookup CommandExecutor using name '" + name + "'");
		}
		vsmContext.setCommandExecutor(executor);
		ClassLoader cl = null;
		if (executor instanceof StatefulKnowledgeSessionImpl) {
			cl = ((ReteooRuleBase) ((StatefulKnowledgeSessionImpl) executor).getRuleBase()).getRootClassLoader();
		} else if (executor instanceof StatelessKnowledgeSessionImpl) {
			cl = ((ReteooRuleBase) ((StatelessKnowledgeSessionImpl) executor).getRuleBase()).getRootClassLoader();
		} else {
			throw new IllegalArgumentException("Unable to set ClassLoader on " + executor);
		}
		vsmContext.setClassLoader(cl);
		Object payload = null;
		if ( body instanceof File ) {
			payload = unmarshaller.unmarshal( (File) body );
		} else if ( body instanceof InputStream ) {
			payload = unmarshaller.unmarshal( (InputStream) body );
		} else if ( body instanceof Reader ) {
			payload = unmarshaller.unmarshal( (Reader) body );
		} else if ( body instanceof Source ) {
			payload = unmarshaller.unmarshal( (Source) body );
		} else if ( body instanceof InputSource ) {
			payload = unmarshaller.unmarshal( (InputSource) body );
		}  else if ( body instanceof Resource ) {
			payload = unmarshaller.unmarshal( (( Resource ) body).getReader() );
		}  else if ( body instanceof String ) {
			payload = unmarshaller.unmarshal( new StringReader( ( String ) body ) );
		} else {
			throw new IllegalArgumentException( "exchange input body object must be instance of File, InputStream, Reader, Source, InputSource, Resource, String" );
		}

		if ( payload instanceof JAXBElement ) {
			payload = ((JAXBElement<?>) payload).getValue();
		}

		if (payload instanceof BatchExecutionCommand) {
			payload = cmdTranslator.transform((BatchExecutionCommand) payload, unmarshaller);
		}

		exchange.setProperty("drools-context", context);
		exchange.setProperty("jaxb-context", jaxbContext);

		return payload;
	}
	
}
