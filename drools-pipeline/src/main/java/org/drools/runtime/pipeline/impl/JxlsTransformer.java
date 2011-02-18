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
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import net.sf.jxls.reader.XLSReadMessage;
import net.sf.jxls.reader.XLSReadStatus;
import net.sf.jxls.reader.XLSReader;

import org.drools.io.Resource;
import org.drools.runtime.pipeline.JxlsTransformerProvider;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;
import org.mvel2.compiler.ExpressionCompiler;

public class JxlsTransformer extends BaseEmitter
    implements
    Transformer {

    private XLSReader    xlsReader;
    private Serializable expr;
    private String       text;

    public JxlsTransformer(XLSReader xlsReader,
                           String text) {
        super();
        this.xlsReader = xlsReader;
        this.text = text;
    }

    public void receive(Object object,
                       PipelineContext context) {
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader( context.getClassLoader() );

        if ( expr == null ) {
            // create MVEL compilation
            final ParserContext parserContext = new ParserContext();
            parserContext.setStrictTypeEnforcement( false );

            ExpressionCompiler compiler = new ExpressionCompiler( this.text );
            this.expr = compiler.compile( parserContext );
        }

        XLSReadStatus readStatus = null;
        Map<String, Object> beans = null;
        try {
            beans = (Map<String, Object>) MVEL.executeExpression( this.expr,
                                                                  object );
            
            // error check beans
            if ( beans == null || !(beans instanceof Map) ) {
                throw new RuntimeException( "Bean map expression must evaluate to a populated Map interface" );
            }
            
            if ( object instanceof InputStream ) {
                readStatus = xlsReader.read( (InputStream) object,
                                             beans );
            } else if ( object instanceof Resource ) {
                readStatus = xlsReader.read( ((Resource) object).getInputStream(),
                                             beans );
            } else {
                throw new IllegalArgumentException( "signal object must be instance of InputStream or Resource" );
            }

            // error check xls reader
            if ( readStatus == null || !readStatus.isStatusOK() ) {
                StringBuilder builder = new StringBuilder();
                builder.append( "Unable to parse resource with XLS:\n" );
                for ( XLSReadMessage message : ((List<XLSReadMessage>) readStatus.getReadMessages()) ) {
                    builder.append( message.getMessage() + "\n" );
                }
                throw new RuntimeException( "Unable to parse resource with XLS:\n" + builder.toString() );
            }
            
        } catch ( Exception e ) {
            handleException( this,
                             object,
                             e );
        }

        Thread.currentThread().setContextClassLoader( originalClassLoader );
        emit( beans,
              context );
    }

    //    
        public static class JxlsTransformerProviderImpl implements JxlsTransformerProvider {
            public Transformer newJxlsTransformer(XLSReader xlsReader,
                                                  String text) {
                return new JxlsTransformer( xlsReader, text );
            }
        }

}
