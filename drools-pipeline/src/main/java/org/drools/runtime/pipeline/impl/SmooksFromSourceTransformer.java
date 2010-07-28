/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                throw new IllegalArgumentException( "signal object must be instance of Source, InputStream, Reader, Resource or String" );
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
