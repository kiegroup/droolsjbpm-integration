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
package org.kie.camel.component;

import java.util.EventListener;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.event.kiebase.DefaultKieBaseEventListener;
import org.kie.api.event.kiebase.KieBaseEventManager;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.Channel;
import org.kie.internal.runtime.KnowledgeRuntime;

/**
 * A consumer that consumes objects sent into channels of a drools
 * session
 */
public class KieConsumer extends DefaultConsumer {

    private KieEndpoint de;
    private KnowledgeRuntime krt;
    private String channelId;
    private String eventType;
    private CamelEventListener camelEventListener;

    public KieConsumer(Endpoint endpoint,
                       Processor processor) {
        super( endpoint, processor );
        de = (KieEndpoint) endpoint;
        krt = (KnowledgeRuntime) de.getExecutor();
        channelId = de.getChannel();
    }

    @Override
    protected void doStop() throws Exception {
        if (channelId != null) {
            krt.unregisterChannel( channelId );
        } else if (eventType != null) {
            camelEventListener.removeEventListener(eventType);
        }
        super.doStop();
    }

    @Override
    protected void doStart() throws Exception {
        if (channelId != null) {
            krt.registerChannel(channelId, new KSessionChannel());
        } else if (eventType != null) {
            camelEventListener = new CamelEventListener(krt, this);
            camelEventListener.addEventListener(eventType);
        } else {
            throw new IllegalStateException("channelId or eventType is required");
        }
        super.doStart();
    }

    class KSessionChannel implements Channel {
        public void send(Object pojo) {
            Exchange exchange = de.createExchange( pojo );
            process(pojo);
        }
    }

    void process(Object pojo) {
        Exchange exchange = de.createExchange( pojo );
        try {
            getProcessor().process(exchange);
        } catch (Exception e) {
            handleException(e);
        }
    }
}
