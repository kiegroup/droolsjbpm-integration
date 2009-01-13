package org.drools.runtime.pipeline.impl;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.drools.io.Resource;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.impl.BaseEmitter;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringSource;

public class SmooksFromSourceTransformer extends BaseEmitter
    implements
    Transformer {
    private Smooks                    smooks;
    private DroolsSmooksConfiguration configuration;

    public SmooksFromSourceTransformer(Smooks smooks,
                             DroolsSmooksConfiguration configuration) {
        this.smooks = smooks;
        this.configuration = configuration;

    }

    public void receive(Object object,
                       PipelineContext context) {
        this.smooks.setClassLoader( context.getClassLoader() );
        Object result = null;
        try {
            JavaResult javaResult = new JavaResult();
            ExecutionContext executionContext = this.smooks.createExecutionContext();

            Source source = null;
            if ( object instanceof Source ) {
                source = ( Source ) object;
            } else if ( object instanceof InputStream ) {                
                source =  new StreamSource( ( InputStream ) object );
            } else if ( object instanceof Reader ) {                
                source =  new StreamSource( ( Reader ) object );
            } else if ( object instanceof Resource ) {                
                source =  new StreamSource( ( ( Resource ) object).getReader() );
            } else if ( object instanceof String ) {                 
                source =  new StringSource( (String) object);
            } else {
                throw new IllegalArgumentException( "signal object must be instance of Source, InputStream, Reader or Resource" );
            }
            
            this.smooks.filter( source,
                                javaResult,
                                executionContext );            
            
            result = javaResult.getBean( this.configuration.getRootId() );
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        emit( result,
              context );
    }

}
