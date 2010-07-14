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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.RuntimeErrorException;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.dataformat.xstream.JsonDataFormat;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.apache.camel.spi.DataFormat;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.command.Command;
import org.drools.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.command.impl.GenericCommand;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.core.util.StringUtils;
import org.drools.grid.ExecutionNode;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.impl.StatelessKnowledgeSessionImpl;
import org.drools.reteoo.ReteooRuleBase;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.impl.ExecutionNodePipelineContextImpl;

public class DroolsProducer extends DefaultProducer {

    DroolsEndpoint de;

    public DroolsProducer(Endpoint endpoint,
                          ExecutionNode node) {
        super( endpoint );
        de = (DroolsEndpoint) endpoint;
    }

    public void process(Exchange exchange) throws Exception {

        // Lookup the original ClassLoaders, so we can restore after execution
        //        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {

            //            String lookup = null;
            //            if ( executor == null ) {
            //                lookup = getLookup(str);
            //                exchange.setProperty( DroolsComponent.DROOLS_LOOKUP, 
            //                                      lookup );
            //            }
            //            
            //            
            //            CommandExecutor exec = getCommandExecutor( lookup );
            //            if ( exec == null ) {
            //                throw new RuntimeException( "CommandExecutor cannot be found for uri " + this.getEndpoint().getEndpointUri() );
            //            }
            //            
            //            ClassLoader localClassLoader = getClassLoader( exec );
            //            if ( exec == null ) {
            //                throw new RuntimeException( "CommandExecutor Classloader cannot be null for uri " + this.getEndpoint().getEndpointUri() );
            //            }            
            //            
            //            // Set the classloader to the one used by the CommandExecutor
            //            Thread.currentThread().setContextClassLoader( localClassLoader );

            Command cmd = null;
            if ( de.dataFormat != null ) {
                String str = exchange.getIn().getBody( String.class );
                ByteArrayInputStream bais = new ByteArrayInputStream( str.getBytes() );
                cmd = (Command) de.dataFormat.unmarshal( exchange,
                                                         bais );
            } else {
                // no data format set, so we assume it's already concrete
                cmd = exchange.getIn().getBody( Command.class );
            }

            if ( cmd == null ) {
                throw new RuntimeCamelException( "Body of in message not of the expected type 'org.drools.command.Command' for uri" + de.getEndpointUri()  );
            }

            if ( !(cmd instanceof BatchExecutionCommand) ) {
                cmd = new BatchExecutionCommand( Arrays.asList( new GenericCommand< ? >[]{(GenericCommand) cmd} ) );
            }

            CommandExecutor exec;
            ExecutionNodePipelineContextImpl droolsContext = exchange.getProperty( "drools-context",
                                                                                   ExecutionNodePipelineContextImpl.class );
            if ( droolsContext != null ) {
                exec = droolsContext.getCommandExecutor();
            } else {
                exec = de.getExecutor();
                if ( exec == null ) {
                    String lookup = exchange.getIn().getHeader( DroolsComponent.DROOLS_LOOKUP,
                                                                String.class );
                    if ( StringUtils.isEmpty( lookup ) && (cmd instanceof BatchExecutionCommand) ) {
                        lookup = ((BatchExecutionCommand) cmd).getLookup();
                    }

                    if ( de.getExecutionNode() != null && !StringUtils.isEmpty( lookup ) ) {
                        exec = de.getExecutionNode().get( DirectoryLookupFactoryService.class ).lookup( lookup );
                        if ( exec == null ) {
                            throw new RuntimeException( "ExecutionNode is unable to find ksession=" + lookup  +" for uri" + de.getEndpointUri() );
                        }
                    } else {
                        throw new RuntimeException( "No ExecutionNode, unable to find ksession=" + lookup +" for uri" + de.getEndpointUri());
                    }
                }
            }
            
            if ( exec == null ) {
                throw new RuntimeException( "No defined ksession for uri" + de.getEndpointUri() );
            }            

            ExecutionResults results = exec.execute( (BatchExecutionCommand) cmd );;

            if ( de.dataFormat != null ) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                de.dataFormat.marshal( exchange,
                                       results,
                                       baos );
                exchange.getOut().setBody( baos.toByteArray() );
            } else {
                exchange.getOut().setBody( results );
            }
        } finally {
            // we must restore the ClassLoader
            //            Thread.currentThread().setContextClassLoader( originalClassLoader );
        }
    }

    // There are nicer ways of doing this
    public static class ResultHandlerImpl
        implements
        ResultHandler {
        Object object;

        public void handleResult(Object object) {
            this.object = object;
        }

        public Object getObject() {
            return this.object;
        }
    }
}
