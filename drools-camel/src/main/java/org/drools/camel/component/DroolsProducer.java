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

import java.util.Arrays;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultProducer;
import org.drools.command.Command;
import org.drools.command.impl.GenericCommand;
import org.drools.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.util.StringUtils;
import org.drools.grid.GridNode;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.ExecutionResults;

public class DroolsProducer extends DefaultProducer {

    DroolsEndpoint de;

    public DroolsProducer(Endpoint endpoint,
                          GridNode node) {
        super( endpoint );
        de = (DroolsEndpoint) endpoint;
    }

    public void process(Exchange exchange) throws Exception {

        try {
            Command cmd = null;
            //            if ( de.dataFormat != null ) {
            //                String str = exchange.getIn().getBody( String.class );
            //                ByteArrayInputStream bais = new ByteArrayInputStream( str.getBytes() );
            //                cmd = (Command) de.dataFormat.unmarshal( exchange,
            //                                                         bais );
            //            } else {
            //                // no data format set, so we assume it's already concrete
            //                cmd = exchange.getIn().getBody( Command.class );
            //            }

            cmd = exchange.getIn().getBody( Command.class );

            if ( cmd == null ) {
                throw new RuntimeCamelException( "Body of in message not of the expected type 'org.drools.command.Command' for uri" + de.getEndpointUri() );
            }

            if ( !(cmd instanceof BatchExecutionCommandImpl) ) {
                cmd = new BatchExecutionCommandImpl( Arrays.asList( new GenericCommand< ? >[]{(GenericCommand) cmd} ) );
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
                    if ( StringUtils.isEmpty( lookup ) && (cmd instanceof BatchExecutionCommandImpl) ) {
                        lookup = ((BatchExecutionCommandImpl) cmd).getLookup();
                    }

                    if ( de.getGridNode() != null && !StringUtils.isEmpty( lookup ) ) {
                        exec = de.getGridNode().get( lookup,
                                                     CommandExecutor.class );
                        if ( exec == null ) {
                            throw new RuntimeException( "ExecutionNode is unable to find ksession=" + lookup + " for uri" + de.getEndpointUri() );
                        }
                    } else {
                        throw new RuntimeException( "No ExecutionNode, unable to find ksession=" + lookup + " for uri" + de.getEndpointUri() );
                    }
                }
            }

            if ( exec == null ) {
                throw new RuntimeException( "No defined ksession for uri" + de.getEndpointUri() );
            }

            ExecutionResults results = exec.execute( (BatchExecutionCommandImpl) cmd );;

            //            if ( de.dataFormat != null ) {
            //                ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //                de.dataFormat.marshal( exchange,
            //                                       results,
            //                                       baos );
            //                exchange.getOut().setBody( baos.toByteArray() );
            //            } else {
            //                exchange.getOut().setBody( results );
            //            }
            exchange.getOut().setBody( results );
        } finally {
            // we must restore the ClassLoader
            //            Thread.currentThread().setContextClassLoader( originalClassLoader );
        }
    }
}
