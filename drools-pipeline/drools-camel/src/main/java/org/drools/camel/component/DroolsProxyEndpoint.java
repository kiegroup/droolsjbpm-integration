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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.RuntimeCamelException;
import org.apache.camel.impl.DefaultEndpoint;
import org.drools.vsm.ServiceManager;

public class DroolsProxyEndpoint extends DefaultEndpoint {

    private String id;
    private ServiceManager serviceManager;

    public DroolsProxyEndpoint(String endpointUri, String remaining, DroolsComponent component) throws URISyntaxException {
        super(endpointUri, component);
        configure(component, remaining);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        throw new RuntimeCamelException("Drools consumers not supported.");
    }

    public Producer createProducer() throws Exception {
        return new DroolsProducer(this, serviceManager);
    }

    public boolean isSingleton() {
        return true;
    }

    public String getId() {
        return id;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    protected void configure(DroolsComponent component, String uri) {
    }
}
