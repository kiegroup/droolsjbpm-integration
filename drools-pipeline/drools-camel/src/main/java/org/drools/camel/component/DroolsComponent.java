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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.UuidGenerator;
import org.drools.vsm.ServiceManager;

public class DroolsComponent extends DefaultComponent {
    // Property name *must* follow the Camel conventions (see org.apache.camel.Exchange)
    public static final String DROOLS_LOOKUP = "DroolsLookup";
    public static final String DROOLS_OUT_IDENTIFIER = "DroolsOutIdentifier";
    public static final String DROOLS_HANDLE = "DroolsHandle";
    
    private static final String EMBEDDED_SCHEME = "drools-embedded";
    private static final String UUID_PREFIX = "drools-";
    private static final AtomicInteger counter = new AtomicInteger();
    
    private CamelContext embeddedContext;
    private ServiceManager serviceManager;
    private String smId = "";

    public DroolsComponent() {
    }

    public DroolsComponent(CamelContext context) {
        super(context);
    }

    public CamelContext getEmbeddedContext() {
        if (embeddedContext == null) {
            createEmbeddedContext();
        }
        return embeddedContext;
    }

    public void setEmbeddedContext(CamelContext context) {
        embeddedContext = context;
    }

    public String getServiceManagerId() {
        return smId;
    }

    public void setServiceManagerId (String smId) {
        this.smId = smId == null ? "" : smId;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager sm) {
        serviceManager = sm;
    }

    /**
     * There are two kinds of drools endpoints. One is the regular endpoint, one would refer two in 
     * a camel route and is the only one a user should be aware of. However such drools endpoint
     * are actually proxies for an entire hidden route (see documentation) because of many things that
     * have to happen within a drools context that is normally not available on a regular camel route.
     * This kind of endpoints would set up a new drools context aware route in a separate, hidden
     * CamelContext embedded in the DroolsComponent. The second kind of endpoint is the one
     * referred to in such an embedded route and must have a 'pipeline' parameter set.
     * 
     * The choice of using a pipeline parameter may be revisited. Another option would be to have the url
     * contain a keyword something like drools:proxy://sm/ksession1.
     */
    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint;
        if (EMBEDDED_SCHEME.equals(new URI(uri).getScheme())) {
            endpoint = new DroolsEndpoint(uri, remaining, this);
        } else {
            endpoint = new DroolsProxyEndpoint(uri, remaining, this);
        }
        setProperties(endpoint, parameters);
        return endpoint;
    }

    protected void createEmbeddedContext() {
        // TODO: fix this temporary solution. Should be able to create a context from spring configuration as well
        if (embeddedContext == null) {
            CamelContext context;
            try {
                context = new DefaultCamelContext(getCamelContext().getRegistry());
                context.disableJMX();
                context.start();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return;
            }
            embeddedContext = context;
        }
    }
    
    public static final String getSessionManagerId(String uri) {
        int pos = uri.indexOf('/');
        return (pos < 0) ? uri : uri.substring(0, pos);

    }

    public static final String getKsessionId(String uri) {
        int pos = uri.indexOf('/');
        return (pos < 0) ? "" : uri.substring(pos + 1);
    }

    public static final String generateUuid() {
        // Using the Camel uuid generator would be an option, but those are pretty long ids
        // UuidGenerator.get().generateUuid()
        return UUID_PREFIX + counter.incrementAndGet();
    }
    @Override
    protected void validateParameters(String uri, Map<String, Object> parameters, String optionPrefix) {

    }
}
