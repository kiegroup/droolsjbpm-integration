/*
 *  Copyright 2009 salaboy.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */

package org.drools.camel.component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.grid.ExecutionNode;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.impl.ExecutionNodePipelineContextImpl;
import org.drools.runtime.pipeline.impl.XStreamResolverStrategy;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * @author salaboy
 */
public class DroolsXStreamDataFormat
    implements
    DataFormat {
    protected XStreamResolverStrategy xstreamStrategy = null;

    protected String                  charset;

    protected static Pattern          p               = Pattern.compile( "[\"']?lookup[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']" );

    public DroolsXStreamDataFormat() {
        xstreamStrategy = new XStreamResolverStrategy() {
            public XStream lookup(String name) {
                return BatchExecutionHelper.newXStreamMarshaller();
            }
        };
    }

    public void marshal(Exchange exchange,
                        Object graph,
                        OutputStream stream) throws Exception {

        PipelineContext context = (PipelineContext) exchange.getProperty( "drools-context" );
        XStream xstream = (XStream) context.getProperties().get( "xstream-instance" );
        xstream.setClassLoader( context.getClassLoader() );
        String result = null;

        result = xstream.toXML( exchange.getIn().getBody() );

        byte[] bytes;
        if ( charset != null ) {
            bytes = result.getBytes( charset );
        } else {
            bytes = result.getBytes();
        }

        stream.write( bytes );

    }

    public Object unmarshal(Exchange exchange,
                            InputStream stream) throws Exception {
        PipelineContext context = (PipelineContext) exchange.getProperty( "drools-context" );
        String str = exchange.getIn().getBody( String.class );

        Matcher m = p.matcher( str );
        String name = null;
        if ( m.find() ) {
            name = m.group( 1 );
        }

        XStream xstream = this.xstreamStrategy.lookup( name );
        if ( xstream == null ) {
            throw new IllegalArgumentException( "Unable to lookup XStream parser using name '" + name + "'" );
        }

        ExecutionNodePipelineContextImpl executionNodeContext = (ExecutionNodePipelineContextImpl) exchange.getProperty( "drools-context" );
        ExecutionNode node = executionNodeContext.getExecutionNode();
        CommandExecutor executor = node.get( DirectoryLookupFactoryService.class ).lookup( name );
        if ( executor == null ) {
            throw new IllegalArgumentException( "Unable to lookup CommandExecutor using name '" + name + "'" );
        }
        executionNodeContext.setCommandExecutor( executor );
        ClassLoader cl = null;
        if ( executor instanceof StatefulKnowledgeSessionImpl ) {
            cl = ((ReteooRuleBase) ((StatefulKnowledgeSessionImpl) executor).getRuleBase()).getRootClassLoader();
            xstream.setClassLoader( cl );
        } else if ( executor instanceof StatelessKnowledgeSessionImpl ) {
            cl = ((ReteooRuleBase) ((StatelessKnowledgeSessionImpl) executor).getRuleBase()).getRootClassLoader();
        } else if ( executor instanceof CommandBasedStatefulKnowledgeSession ) {
            cl = ((ReteooRuleBase) ((KnowledgeBaseImpl) ((CommandBasedStatefulKnowledgeSession) executor).getKnowledgeBase()).getRuleBase()).getRootClassLoader();
        } else {
            throw new IllegalArgumentException( "Unable to set ClassLoader on " + executor );
        }
        xstream.setClassLoader( cl );
        executionNodeContext.setClassLoader( cl );
        Object payload = xstream.fromXML( str );

        executionNodeContext.getProperties().put( "xstream-instance",
                                                  xstream );
        exchange.setProperty( "drools-context",
                              executionNodeContext );
        return payload;
    }

}