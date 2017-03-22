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
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultProducer;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.stream.Collectors.groupingBy;
import static org.kie.camel.KieCamelConstants.*;
import static org.kie.camel.KieCamelUtils.asCamelKieName;
import static org.kie.camel.KieCamelUtils.getResultMessage;
import static org.kie.camel.KieCamelUtils.ucFirst;

public class KieProducer extends DefaultProducer {

    private static final transient Logger log = LoggerFactory.getLogger( KieProducer.class );

    private static final String DEFAULT_CLIENT = "KieServices";

    private final KieEndpoint endpoint;

    private final KieServicesClient client;

    private final Map<String, InternalProducer> producers = new HashMap<>();

    public KieProducer( KieEndpoint endpoint ) {
        super(endpoint);
        this.endpoint = endpoint;
        client = KieServicesFactory.newKieServicesClient( endpoint.getKieServicesConf() );
    }

    @Override
    public void process( Exchange exchange ) throws Exception {
        getProducer(exchange).execute(exchange);
    }

    private InternalProducer getProducer(Exchange exchange) {
        String clientName = endpoint.getClient() != null ?
                            endpoint.getClient() :
                            exchange.getIn().getHeader( KIE_CLIENT, DEFAULT_CLIENT, String.class );
        return producers.computeIfAbsent( clientName, name -> {
            String producerName = KieProducer.class.getName() + "$" + ucFirst( name ) + "Producer";
            try {
                Class<?> producerClass = Class.forName( producerName );
                return (InternalProducer) producerClass.getConstructor( KieServicesClient.class, String.class, KieEndpoint.class )
                                                       .newInstance( client, clientName, endpoint );
            } catch (Exception e) {
                log.error( "Unknown client name: " + clientName );
                return new DummyProducer();
            }
        } );
    }

    interface InternalProducer {
        void execute(Exchange exchange);
    }

    abstract static class AbstractInternalProducer<C> implements InternalProducer {

        protected final C client;

        private final Optional<Method> operationLookupMethod;

        protected AbstractInternalProducer(C client) {
            this.client = client;
           operationLookupMethod = getLookupMethod();
        }

        protected Optional<Operation<C>> getOperation( String operationName ) {
            return operationLookupMethod.flatMap( m -> {
                try {
                    return Optional.of( (Operation<C>) m.invoke( null, operationName ) );
                } catch (IllegalAccessException | InvocationTargetException e) {
                    return Optional.empty();
                }
            } );
        }

        private Optional<Method> getLookupMethod() {
            try {
                Class<?> enumClass = Class.forName( getClass().getName() + "$Operations" );
                return Optional.of(enumClass.getMethod( "valueOf", String.class ));
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    abstract static class AbstractReflectiveProducer<C> extends AbstractInternalProducer<C> {
        private final Map<String, Collection<Method>> methodsMap;

        private final String clientName;
        private final KieEndpoint endpoint;

        protected AbstractReflectiveProducer(C client, String clientName, KieEndpoint endpoint) {
            super(client);
            this.clientName = clientName;
            this.endpoint = endpoint;
            this.methodsMap = indexClientMethod( (Class) ( (ParameterizedType) getClass().getGenericSuperclass() ).getActualTypeArguments()[0] );
        }

        @Override
        public final void execute(Exchange exchange) {
            String operationName = endpoint.getOperation() != null ?
                                   endpoint.getOperation() :
                                   exchange.getIn().getHeader( KIE_OPERATION, String.class );
            Object response = getOperation(operationName).map(op -> op.execute( client, exchange ) )
                                                         .orElseGet( () -> executeViaReflection( operationName, exchange ) );
            writeResponse( exchange, response );
        }

        private void writeResponse( Exchange exchange, Object response ) {
            if (response instanceof ServiceResponse ) {
                ServiceResponse serviceResponse = (ServiceResponse) response;
                Message message = getResultMessage(exchange);
                message.setBody( serviceResponse.getResult() );
                message.setHeader( RESPONSE_TYPE, serviceResponse.getType() );
                message.setHeader( RESPONSE_MESSAGE, serviceResponse.getMsg() );
            } else {
                getResultMessage(exchange).setBody( response );
            }
        }

        private Object executeViaReflection( String operationName, Exchange exchange ) {
            Collection<Method> methods = methodsMap.get( operationName );
            if (methods == null) {
                log.error( "Unknown operation name: " + operationName );
                return null;
            }

            String bodyParam = endpoint.getConfiguration().getBodyParam( clientName, operationName );
            Method method = methods.stream()
                                   .filter( m -> invokable( exchange, m, bodyParam ) )
                                   .findFirst()
                                   .orElseGet( () -> {
                                       log.error( "Unknown operation name: " + operationName );
                                       return null;
                                   } );
            return method != null ? invoke( exchange, method, bodyParam ) : null;
        }

        private boolean invokable(Exchange exchange, Method method, String bodyParam) {
            Set<String> headers = exchange.getIn().getHeaders().keySet();
            return Stream.of(method.getParameters()).allMatch( p -> p.getName().equals( bodyParam ) ||
                                                                    headers.contains( asCamelKieName( p.getName() ) ) );
        }

        private Object invoke(Exchange exchange, Method method, String bodyParam) {
            try {
                Object[] args = Stream.of(method.getParameters())
                                      .map(p -> p.getName().equals( bodyParam ) ?
                                                exchange.getIn().getBody( p.getType() ) :
                                                exchange.getIn().getHeader( asCamelKieName( p.getName() ), p.getType() ) )
                                      .toArray();
                return method.invoke(client, args);
            } catch (Exception e) {
                log.error( "Error executed operation: " + method.getName() + " caused by: " + e.getMessage(), e );
                return null;
            }
        }

        private Map<String, Collection<Method>> indexClientMethod(Class<?> cls) {
            return Stream.of(cls.getMethods()).collect( groupingBy(Method::getName,
                                                                   Collector.of(() -> new TreeSet<Method>( (m1,m2) -> m2.getParameterCount() - m1.getParameterCount() ),
                                                                                Collection::add,
                                                                                (left, right) -> { left.addAll(right); return left; })) );
        }
    }

    interface Operation<C> {
        Object execute(C client, Exchange exchange);
    }

    static class DummyProducer implements InternalProducer {
        @Override
        public void execute( Exchange exchange ) { }
    }

    static class KieServicesProducer extends AbstractReflectiveProducer<KieServicesClient> {
        public KieServicesProducer(KieServicesClient client, String clientName, KieEndpoint endpoint) {
            super(client, clientName, endpoint);
        }

        enum Operations implements Operation<KieServicesClient> {
            myCustomOperation {
                @Override
                public Object execute(KieServicesClient client, Exchange exchange) {
                    return client.getServerInfo();
                }
            }
        }
    }

    static class RuleProducer extends AbstractReflectiveProducer<RuleServicesClient> {
        public RuleProducer(KieServicesClient client, String clientName, KieEndpoint endpoint) {
            super( client.getServicesClient(RuleServicesClient.class), clientName, endpoint );
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

    static class ProcessProducer extends AbstractReflectiveProducer<ProcessServicesClient> {
        public ProcessProducer(KieServicesClient client, String clientName, KieEndpoint endpoint) {
            super( client.getServicesClient(ProcessServicesClient.class), clientName, endpoint );
        }
    }

    static class DmnProducer extends AbstractReflectiveProducer<DMNServicesClient> {
        public DmnProducer(KieServicesClient client, String clientName, KieEndpoint endpoint) {
            super( client.getServicesClient(DMNServicesClient.class), clientName, endpoint );
        }
    }
}
