package org.drools.runtime.pipeline.impl;

import java.io.InputStream;
import java.io.Reader;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.drools.io.Resource;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.SmooksTransformerProvider;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.impl.BaseEmitter;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.JavaResult;
import org.milyn.payload.JavaSource;
import org.milyn.payload.StringResult;

public class SmooksToSourceTransformer extends BaseEmitter
    implements
    Transformer {
    private Smooks smooks;

    public SmooksToSourceTransformer(Smooks smooks) {
        this.smooks = smooks;
    }

    public void receive(Object object,
                        PipelineContext context) {
        this.smooks.setClassLoader( context.getClassLoader() );
        Object result = null;
        try {
            StringResult stringResult = new StringResult();
            ExecutionContext executionContext = this.smooks.createExecutionContext();

            this.smooks.filter( new JavaSource( object ),
                                stringResult,
                                executionContext );

            result = stringResult.getResult();
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        emit( result,
              context );
    }

//    public static class SmooksTransformerProviderImpl
//        implements
//        SmooksTransformerProvider {
//        public Transformer newSmooksTransformer(Smooks smooks,
//                                                String rootId) {
//            DroolsSmooksConfiguration conf = new DroolsSmooksConfiguration( rootId );
//            return new SmooksToSourceTransformer( smooks,
//                                                  conf );
//        }
//    }

}
