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

/*
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

import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.DefaultMessage;
import org.apache.camel.spi.DataFormat;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.util.StringUtils;
import org.drools.grid.GridNode;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;

public class DroolsEndpoint extends DefaultEndpoint {

    /**
     * An ENUM to define which action should be executed by the 
     * producer into this end point
     */
    public static enum Action {
        EXECUTE("execute"),
        INSERT_BODY("insertBody"),
        INSERT_MESSAGE("insertMessage"),
        INSERT_EXCHANGE("insertExchange");

        private final String id;

        Action(String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public static Action resolveAction(String id) {
            if ( EXECUTE.getId().equalsIgnoreCase( id ) ) {
                return EXECUTE;
            } else if ( INSERT_BODY.getId().equalsIgnoreCase( id ) ) {
                return INSERT_BODY;
            } else if ( INSERT_MESSAGE.getId().equalsIgnoreCase( id ) ) {
                return INSERT_MESSAGE;
            } else if ( INSERT_EXCHANGE.getId().equalsIgnoreCase( id ) ) {
                return INSERT_EXCHANGE;
            } else {
                throw new IllegalArgumentException( "Invalid action configuring EndPoint = " + id );
            }
        }
    }

    public static final Pattern p          = Pattern.compile( "[\"']?lookup[\"']?\\s*[:=]\\s*[\"']([^\"']+)[\"']" );

    public String               ksessionId;
    public CommandExecutor      executor;
    public GridNode             node;

    public String               dataFormatName;

    public DataFormat           dataFormat;

    public Action               action     = Action.EXECUTE;
    public String               entryPoint = null;
    public String               channel    = null;

    public DroolsEndpoint(String endpointUri,
                          String remaining,
                          DroolsComponent component) throws URISyntaxException {
        super( endpointUri,
               component );
        configure( component,
                   remaining );
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        return new DroolsConsumer( this, 
                                   processor );
    }

    public Producer createProducer() throws Exception {
        if ( Action.EXECUTE.equals( action ) ) {
            return new DroolsExecuteProducer( this,
                                              node );
        } else {
            return new DroolsInsertProducer( this,
                                             node );
        }
    }

    public boolean isSingleton() {
        return true;
    }

    public String getKsessionId() {
        return ksessionId;
    }

    public void setKsessionId(String ksession) {
        this.ksessionId = ksession;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public GridNode getGridNode() {
        return node;
    }

    protected void configure(DroolsComponent component,
                             String uri) {
        String nodeId = getExecuteNodeId( uri );
        ksessionId = getKsessionId( uri );

        if ( !StringUtils.isEmpty( nodeId ) ) {
            // let's look it up
            node = component.getCamelContext().getRegistry().lookup( nodeId,
                                                                     GridNode.class );
            if ( node == null ) {
                throw new RuntimeCamelException( "Could not find GridNode for uri=\"" + uri + "\" in CamelContext. Check configuration." );
            }

            // if id is empty this endpoint is not attached to a CommandExecutor and will have to look it up at runtime.
            if ( !StringUtils.isEmpty( ksessionId ) ) {
                // lookup command executor
                executor = node.get( ksessionId,
                                     CommandExecutor.class );
                if ( executor == null ) {
                    throw new RuntimeCamelException( "Failed to instantiate DroolsEndpoint. " + "Lookup of CommandExecutor with uri=\"" + uri + "\" failed. Check configuration." );
                }
            }
        } else {
            // this is a hanging entity, not attached to an ExecutionNode
            executor = component.getCamelContext().getRegistry().lookup( ksessionId,
                                                                         CommandExecutor.class );
            if ( executor == null ) {
                throw new RuntimeCamelException( "Failed to instantiate DroolsEndpoint. Could not find ksession for " + " uri=\"" + uri + "\". Check configuration." );
            }
            // TODO: test this scenario...
        }

        //        if ( !StringUtils.isEmpty( getDataFormat() ) ) {
        //            String dataFormatName = getDataFormat();
        //            if ( "drools-xstream".equals( dataFormatName ) ) {
        //                XStreamDataFormat xstreaDataFormat = new XStreamDataFormat();
        //                xstreaDataFormat.setXStream( BatchExecutionHelper.newXStreamMarshaller() );
        //                this.dataFormat = xstreaDataFormat;
        //            } else if ( "drools-json".equals( dataFormatName ) ) {
        //                JsonDataFormat xstreaDataFormat = new JsonDataFormat();
        //                xstreaDataFormat.setXStream( BatchExecutionHelper.newJSonMarshaller() );
        //                this.dataFormat = xstreaDataFormat;
        //            } else if ( "jaxb".equals( dataFormatName ) ) {
        //                // to specify jaxb must be 
        //                //JaxbDataFormat jaxbDataFormat = new JaxbDataFormat();
        //                //jaxbDataFormat.setContextPath( contextPath )
        //            } else {
        //                this.dataFormat = getCamelContext().getRegistry().lookup( getDataFormat(),
        //                                                                          DataFormat.class );
        //            }
        //        }

    }

    public static final String getExecuteNodeId(String uri) {
        int pos = uri.indexOf( '/' );
        return (pos < 0) ? uri : uri.substring( 0,
                                                pos );

    }

    public static final String getKsessionId(String uri) {
        int pos = uri.indexOf( '/' );
        return (pos < 0) ? "" : uri.substring( pos + 1 );
    }

    public String getDataFormat() {
        return this.dataFormatName;
    }

    public void setDataFormat(String dataFormatName) {
        this.dataFormatName = dataFormatName;
    }

    public String getLookup(String body) {
        Matcher m = p.matcher( body );
        String name = null;
        if ( m.find() ) {
            name = m.group( 1 );
        }

        return name;
    }

    public CommandExecutor getCommandExecutor(String name) {
        if ( this.executor != null ) {
            return this.executor;
        }

        if ( this.node == null ) {
            throw new RuntimeException( "ExecutionNode for CommandExecutor lookup cannot be null" );
        }

        return node.get( name,
                         CommandExecutor.class );
    }

    public ClassLoader getClassLoader(CommandExecutor exec) {
        ClassLoader cl = null;

        if ( exec instanceof StatefulKnowledgeSessionImpl ) {
            cl = ((ReteooRuleBase) ((StatefulKnowledgeSessionImpl) exec).getRuleBase()).getRootClassLoader();
        } else if ( exec instanceof StatelessKnowledgeSessionImpl ) {
            cl = ((ReteooRuleBase) ((StatelessKnowledgeSessionImpl) exec).getRuleBase()).getRootClassLoader();
        } else if ( exec instanceof CommandBasedStatefulKnowledgeSession ) {
            cl = ((ReteooRuleBase) ((KnowledgeBaseImpl) ((CommandBasedStatefulKnowledgeSession) exec).getKnowledgeBase()).getRuleBase()).getRootClassLoader();
        }

        return cl;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setAction(String action) {
        this.action = Action.resolveAction( action );
    }

    public String getEntryPoint() {
        return entryPoint;
    }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
    
    public Exchange createExchange( Object pojo ) {
        DefaultMessage msg = new DefaultMessage();
        msg.setBody( pojo );
        DefaultExchange exchange = new DefaultExchange(this, getExchangePattern());
        exchange.setIn( msg );
        return exchange;
    }    
    
}
