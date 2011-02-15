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

/**
 * 
 */
package org.drools.runtime.pipeline.impl;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.core.util.StringUtils;
import org.drools.grid.GridNode;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.io.Resource;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.pipeline.PipelineContext;
import org.drools.runtime.pipeline.Transformer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomReader;

public class XStreamFromXmlGridTransformer extends BaseEmitter
    implements
    Transformer {

    private XStreamResolverStrategy strategy;
    private Object payload;
    private PipelineContext context;
    
    private static Pattern p = Pattern.compile("[\"']?lookup[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']");
    
    public XStreamFromXmlGridTransformer(XStreamResolverStrategy strategy) {
        this.strategy = strategy;
    }

    public void receive(Object object,
                        PipelineContext context) {            
        this.payload = processPayload(object, context);
        emit( this.payload,
              context );
    }

    public Object processPayload(Object object, PipelineContext context) {
        
        try {
            String str ;
            if ( object instanceof String ) {
                str = (String) object; 
            } else if ( object instanceof Reader ) {
                str = StringUtils.toString( ((Reader)object) );
            } else if ( object instanceof InputStream ) {
                str = StringUtils.toString( ((InputStream)object) );
            } else if ( object instanceof Resource ) {
                str = StringUtils.toString( ((Resource) object).getReader());
            } else {
                throw new IllegalArgumentException( "signal object must be instance of String, Reader, InputStream or Resource, was: " + object );
            }                  
            
            Matcher m = p.matcher( str );
            String name = null;
            if ( m.find() ) {
                name = m.group( 1 );
            }
            
            XStream xstream = this.strategy.lookup(name);
            if (xstream == null) {
                throw new IllegalArgumentException("Unable to lookup XStream parser using name '" + name + "'");
            }
            ExecutionNodePipelineContextImpl executionNodeContext = (ExecutionNodePipelineContextImpl) context;
            GridNode node = executionNodeContext.getGridNode();
            
            CommandExecutor executor = node.get(name, CommandExecutor.class);
            if (executor == null) {
                throw new IllegalArgumentException("Unable to lookup CommandExecutor using name '" + name + "'");
            }
            executionNodeContext.setCommandExecutor(executor);
            ClassLoader cl = null;
            if (executor instanceof StatefulKnowledgeSessionImpl) {
                cl = ((ReteooRuleBase) (( StatefulKnowledgeSessionImpl ) executor).getRuleBase()).getRootClassLoader();
                xstream.setClassLoader(cl);
            } else if (executor instanceof StatelessKnowledgeSessionImpl) {
                cl = ((ReteooRuleBase) (( StatelessKnowledgeSessionImpl ) executor).getRuleBase()).getRootClassLoader();
            } else {
                throw new IllegalArgumentException("Unable to set ClassLoader on " + executor);
            }
            xstream.setClassLoader(cl);
            executionNodeContext.setClassLoader(cl);
            payload = xstream.fromXML( ((String) object) );
            context.getProperties().put("xstream-instance", xstream);
            this.context = context;
        } catch (Exception e) {
            handleException(this, object, e);
        }
        return payload;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public PipelineContext getContext() {
        return context;
    }

    public void setContext(PipelineContext context) {
        this.context = context;
    }
    
}