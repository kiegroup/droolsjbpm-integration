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

import org.apache.camel.CamelException;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverter;
import org.apache.camel.impl.DefaultProducer;
import org.w3c.dom.Document;

public class DroolsProducer extends DefaultProducer {

    public DroolsProducer(Endpoint endpoint) {
        super(endpoint);
    }

    public void process(Exchange exchange) throws Exception {
	    
        TypeConverter converter = exchange.getContext().getTypeConverter();
        Document body = exchange.getIn().getBody(Document.class);
        DroolsPayload payload = converter.convertTo(DroolsPayload.class, exchange, body);
        if (payload == null) {
            // throw new CamelException("foo");
        }
    }
}
