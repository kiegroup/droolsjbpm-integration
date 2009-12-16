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

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;

public class DroolsProxyProducer extends DefaultProducer {
    private Endpoint droolsEndpoint;
    private ProducerTemplate template;

    public DroolsProxyProducer(Endpoint endpoint) {
        super(endpoint);
        DroolsProxyEndpoint de = (DroolsProxyEndpoint)endpoint;
        droolsEndpoint = de.getEmbeddedContext().getEndpoint("direct:" + de.getId());
        template = endpoint.getCamelContext().createProducerTemplate();
        
    }

    public void process(Exchange exchange) throws Exception {
        Exchange result = template.send(droolsEndpoint, exchange.copy());
        exchange.getOut().copyFrom(ExchangeHelper.getResultMessage(result));
    }
}
