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

package org.kie.camel.component;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultProducer;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.util.StringUtils;
import org.kie.api.command.Command;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.ExecutionResults;

import java.util.Arrays;

public class KieExecuteProducer extends DefaultProducer {

    public KieExecuteProducer(Endpoint endpoint) {
        super( endpoint );
    }

    public void process(Exchange exchange) throws Exception {
        KieEndpoint de = (KieEndpoint) getEndpoint();

        Command<?> cmd = exchange.getIn().getBody( Command.class );

        if ( cmd == null ) {
            throw new RuntimeCamelException( "Body of in message not of the expected type 'org.kie.api.command.Command' for uri" + de.getEndpointUri() );
        }

        if ( !(cmd instanceof BatchExecutionCommandImpl) ) {
            cmd = new BatchExecutionCommandImpl( Arrays.asList( new GenericCommand< ? >[]{(GenericCommand<?>) cmd} ) );
        }

        CommandExecutor exec;
        ExecutionNodePipelineContextImpl droolsContext = exchange.getProperty( "kie-context",
                                                                               ExecutionNodePipelineContextImpl.class );
        if ( droolsContext != null ) {
            exec = droolsContext.getCommandExecutor();
        } else {
            exec = de.getExecutor();
            if ( exec == null ) {
                String lookup = exchange.getIn().getHeader( KieComponent.KIE_LOOKUP,
                                                            String.class );
                if ( StringUtils.isEmpty(lookup) && (cmd instanceof BatchExecutionCommandImpl) ) {
                    lookup = ((BatchExecutionCommandImpl) cmd).getLookup();
                }

                if ( !StringUtils.isEmpty( lookup ) ) {
                    exec = de.getComponent().getCamelContext().getRegistry().lookup( lookup,
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
            throw new RuntimeException( "No defined ksession for uri " + de.getEndpointUri() );
        }

        ExecutionResults results = exec.execute( (BatchExecutionCommandImpl) cmd );;
        exchange.getOut().setBody( results );
        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
    }
}
