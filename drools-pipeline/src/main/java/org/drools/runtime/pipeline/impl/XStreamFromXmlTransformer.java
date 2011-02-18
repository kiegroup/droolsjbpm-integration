/*
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

import org.drools.io.Resource;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;

import com.thoughtworks.xstream.XStream;

public class XStreamFromXmlTransformer extends BaseEmitter
    implements  
    Transformer {
    private XStream                    xstream;

    public XStreamFromXmlTransformer(XStream xstream) {
        this.xstream = xstream;

    }

    public void receive(Object object,
                       PipelineContext context) {
        this.xstream.setClassLoader( context.getClassLoader() );
        Object result = null;
        try {
            if ( object instanceof String ) {
                result = this.xstream.fromXML( ( String ) object );
            } else  if ( object instanceof Reader ) {
                result = this.xstream.fromXML( ( Reader ) object );
            } else if ( object instanceof InputStream ) {
                result = this.xstream.fromXML( ( InputStream ) object );
            } else if ( object instanceof Resource ) {
                result = this.xstream.fromXML( (( Resource ) object).getReader() );
            } else {
                throw new IllegalArgumentException( "signal object must be instance of InputStream or Resource" );
            }
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }
        emit( result,
              context );
    }
    
    public void sniff(Reader reader) {
        
    }
}
