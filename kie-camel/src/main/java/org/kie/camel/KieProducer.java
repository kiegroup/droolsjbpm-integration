/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.camel.KieCamelUtils.getResultMessage;
import static org.kie.camel.KieComponent.KIE_CLIENT;
import static org.kie.camel.KieComponent.KIE_OPERATION;

public class KieProducer extends DefaultProducer {

    private static final transient Logger log = LoggerFactory.getLogger( KieProducer.class );

    private final String DEFAULT_CLIENT = "KieServices";

    private final KieServicesClient client;

    private final Map<String, InternalProducer> producers = new HashMap<>();

    public KieProducer( KieEndpoint endpoint ) {
        super(endpoint);
        client = KieServicesFactory.newKieServicesClient( endpoint.getConfiguration() );
    }

    @Override
    public void process( Exchange exchange ) throws Exception {
        writeResponse( exchange, getProducer(exchange).execute(exchange) );
    }

    private void writeResponse( Exchange exchange, Object response ) {
        if (response instanceof ServiceResponse ) {
            getResultMessage(exchange).setBody( ( (ServiceResponse) response ).getResult() );
        } else {
            getResultMessage(exchange).setBody( response );
        }
    }

    private InternalProducer getProducer(Exchange exchange) {
        String clientName = exchange.getIn().getHeader( KIE_CLIENT, DEFAULT_CLIENT, String.class );
        return producers.computeIfAbsent( clientName, name -> {
            String producerName = KieProducer.class.getName() + "$" +
                                  name.substring(0, 1).toUpperCase() + name.substring(1) + "Producer";
            try {
                Class<?> producerClass = Class.forName( producerName );
                return (InternalProducer) producerClass.getConstructor( KieServicesClient.class ).newInstance( client );
            } catch (Exception e) {
                log.error( "Unknown client name: " + clientName );
                return new DummyProducer();
            }
        } );
    }

    interface InternalProducer {
        Object execute(Exchange exchange);
    }

    abstract static class AbstractInternalProducer<C> implements InternalProducer {

        private final C client;

        private final Method operationLookupMethod;

        protected AbstractInternalProducer(C client) {
            this.client = client;
            operationLookupMethod = getLookupMethod();
        }

        @Override
        public Object execute(Exchange exchange) {
            String operationName = exchange.getIn().getHeader( KIE_OPERATION, String.class );
            try {
                return getOperation(operationName).map(op -> op.execute( client, exchange ) ).orElse( null );
            } catch (Exception e) {
                log.error( "Error executed operation: " + operationName + " caused by: " + e.getMessage(), e );
                return null;
            }
        }

        private Optional<Operation<C>> getOperation( String operationName ) {
            Operation<C> operation;
            try {
                return Optional.of( (Operation<C>) operationLookupMethod.invoke( null, operationName ) );
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error( "Unknown operation name: " + operationName );
                return Optional.empty();
            }
        }

        private Method getLookupMethod() {
            try {
                Class<?> enumClass = Class.forName( getClass().getName() + "$Operations" );
                return enumClass.getMethod( "valueOf", String.class );
            } catch (Exception e) {
                log.error( "Initialization error", e );
                return null;
            }
        }
    }

    interface Operation<C> {
        Object execute(C client, Exchange exchange);
    }

    static class DummyProducer implements InternalProducer {
        @Override
        public Object execute( Exchange exchange ) { return null; }
    }

    static class KieServicesProducer extends AbstractInternalProducer<KieServicesClient> {
        public KieServicesProducer(KieServicesClient client) {
            super(client);
        }

        enum Operations implements Operation<KieServicesClient> {
            getServerInfo {
                @Override
                public Object execute(KieServicesClient client, Exchange exchange) {
                    return client.getServerInfo();
                }
            }, listContainers {
                @Override
                public Object execute(KieServicesClient client, Exchange exchange) {
                    return client.listContainers();
                }
            }
        }
    }

    static class RuleProducer extends AbstractInternalProducer<RuleServicesClient> {
        public RuleProducer(KieServicesClient client) {
            super( client.getServicesClient(RuleServicesClient.class) );
        }

        enum Operations implements Operation<RuleServicesClient> {
            fireAllRules {
                @Override
                public Object execute(RuleServicesClient client, Exchange exchange) {
                    String containerId = exchange.getIn().getHeader( "containerId", String.class );
                    Command fireCommand = KieServices.get().getCommands().newFireAllRules();
                    return client.executeCommandsWithResults(containerId, fireCommand);
                }
            }
        }
    }

    static class ProcessProducer extends AbstractInternalProducer<ProcessServicesClient> {
        public ProcessProducer(KieServicesClient client) {
            super( client.getServicesClient(ProcessServicesClient.class) );
        }

        enum Operations implements Operation<ProcessServicesClient> {
            getProcessDefinition {
                @Override
                public Object execute(ProcessServicesClient client, Exchange exchange) {
                    String containerId = exchange.getIn().getHeader( "containerId", String.class );
                    String processId = exchange.getIn().getHeader( "processId", String.class );
                    return client.getProcessDefinition(containerId, processId);
                }
            }
        }
    }
}
