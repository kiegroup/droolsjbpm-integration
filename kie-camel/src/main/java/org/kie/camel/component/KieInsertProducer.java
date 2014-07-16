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
import org.apache.camel.impl.DefaultProducer;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.runtime.StatelessKnowledgeSession;

/**
 * A producer that inserts incoming messages as facts into the
 * Drools session.
 * 
 * It can be configured to insert the message body only, or the
 * whole message (that includes headers) or even the whole Exchange 
 * object.
 */
public class KieInsertProducer extends DefaultProducer {

    // the corresponding endpoint
    private KieEndpoint ke;
    // the actual insert is executed by a worker class that 
    // implements the GoF strategy pattern to avoid conditionals
    // at insert time and hopefully improve performance
    private InsertWorker   worker;

    public KieInsertProducer(Endpoint endpoint) {
        super( endpoint );
        ke = (KieEndpoint) endpoint;

        // Configures this Producer with the proper action
        // by composing strategy objects
        KieEndpoint.Action action = ke.getAction();
        Unwrapper unwrapper = null;
        switch ( action ) {
            case INSERT_BODY :
                unwrapper = BodyUnwrapper.INSTANCE;
                break;
            case INSERT_MESSAGE :
                unwrapper = MessageUnwrapper.INSTANCE;
                break;
            case INSERT_EXCHANGE :
                unwrapper = ExchangeUnwrapper.INSTANCE;
                break;
        }

        // Creates the actual worker
        CommandExecutor exec = ke.getExecutor();
        if ( exec instanceof StatefulKnowledgeSession ) {
        	EntryPoint wmep;
            String ep = ke.getEntryPoint();
            if ( ep != null ) {
                wmep = ((StatefulKnowledgeSession) exec).getEntryPoint( ep );
            } else {
                wmep = (EntryPoint) exec;
            }
            worker = new StatefulSessionInsertWorker( wmep,
                                                      unwrapper );
        } else {
            worker = new StatelessSessionInsertWorker( (StatelessKnowledgeSession) exec,
                                                       unwrapper );
        }
    }

    public void process(Exchange exchange) throws Exception {
        worker.process( exchange );
    }

    /**
     * An interface for the worker strategy
     */
    private static interface InsertWorker {
        public void process(Exchange exchange) throws Exception;
    }

    /**
     * A stateful implementation for the worker
     */
    private static class StatefulSessionInsertWorker
        implements
        InsertWorker {
        private EntryPoint wmep;
        private Unwrapper               unwrapper;

        public StatefulSessionInsertWorker(EntryPoint wmep,
                                           Unwrapper unwrapper) {
            this.wmep = wmep;
            this.unwrapper = unwrapper;
        }

        public void process(Exchange exchange) throws Exception {
            this.wmep.insert( unwrapper.getObject( exchange ) );
        }
    }

    /**
     * A stateless implementation for the worker
     */
    private static class StatelessSessionInsertWorker
        implements
        InsertWorker {
        private StatelessKnowledgeSession ksession;
        private Unwrapper                 unwrapper;

        public StatelessSessionInsertWorker(StatelessKnowledgeSession ksession,
                                            Unwrapper unwrapper) {
            this.ksession = ksession;
            this.unwrapper = unwrapper;
        }

        public void process(Exchange exchange) throws Exception {
            this.ksession.execute( unwrapper.getObject( exchange ) );
        }
    }

    /**
     * Another strategy interface to properly process incoming objects
     * selecting between body, message or exchange
     */
    private static interface Unwrapper {
        public Object getObject(Exchange exchange);
    }

    private static class BodyUnwrapper
        implements
        Unwrapper {
        public static final BodyUnwrapper INSTANCE = new BodyUnwrapper();

        public Object getObject(Exchange exchange) {
            return exchange.getIn().getBody();
        }
    }

    private static class MessageUnwrapper
        implements
        Unwrapper {
        public static final MessageUnwrapper INSTANCE = new MessageUnwrapper();

        public Object getObject(Exchange exchange) {
            return exchange.getIn();
        }
    }

    private static class ExchangeUnwrapper
        implements
        Unwrapper {
        public static final ExchangeUnwrapper INSTANCE = new ExchangeUnwrapper();

        public Object getObject(Exchange exchange) {
            return exchange;
        }
    }
}
