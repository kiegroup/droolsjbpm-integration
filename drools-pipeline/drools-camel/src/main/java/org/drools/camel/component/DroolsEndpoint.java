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
import org.drools.runtime.CommandExecutor;
import org.drools.vsm.ServiceManager;

public class DroolsEndpoint extends DefaultEndpoint {
    // Property name *must* follow the Camel conventions (see org.apache.camel.Exchange)
    public static final String DROOLS_CONTEXT_PROPERTY = "CamelDroolsContext";

    private String id;
    private String method;
    private CommandExecutor executor;
    private ServiceManager serviceManager;

    public DroolsEndpoint(String endpointUri, String remaining, DroolsComponent component) throws URISyntaxException {
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

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    protected void configure(DroolsComponent component, String uri) {
        int pos = uri.indexOf('/');
        String smId = (pos < 0) ? uri : uri.substring(0, pos);
        id = (pos < 0) ? "" : uri.substring(pos + 1);
        
        if (smId.length() > 0) {
            // initialize the component if needed
            serviceManager = component.getServiceManager();
            if (serviceManager == null) {
                // let's look it up
                serviceManager = component.getCamelContext().getRegistry().lookup(smId, ServiceManager.class);
                if (serviceManager == null) {
                    throw new RuntimeCamelException("Could not find ServiceManager with id=\""
                        + smId + "\" in CamelContext. Check configuration.");
                }
                // use this ServiceManager
                component.setServiceManagerId(smId);
                component.setServiceManager(serviceManager);
            } else if (!smId.equals(component.getServiceManagerId())) {
                // make sure we deal with the same ServiceManager.
                // having multiple ServiceManagers instances in the same process is not supported
                throw new RuntimeCamelException("ServiceManager already initialized from id=\""
                    + component.getServiceManagerId() + "\" yet current endpoint requries id=\"" + id + "\"");
            }
            
            // if id is empty this endpoint is not attached to a CommandExecutor and will have to look it up at runtime.
            if (id.length() > 0) {
                // lookup command executor on 
                executor = serviceManager.lookup(id);
                if (executor == null) {
                    throw new RuntimeCamelException("Failed to instantiate DroolsEndpoint. " 
                        + "Lookup of CommandExecutor with id=\"" + uri + "\" failed. Check configuration.");
                }
            }
        } else {
            // this is a hanging entity, not attached to an SM
            executor = component.getCamelContext().getRegistry().lookup(id, CommandExecutor.class);
            
            // TODO: test this scenario...
        }
    }
}
