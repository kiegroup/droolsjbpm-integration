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

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.drools.vsm.ServiceManager;

public class DroolsComponent extends DefaultComponent {
    private String smId = "";
    private ServiceManager serviceManager;

    public DroolsComponent() {
    }

    public DroolsComponent(CamelContext context) {
        super(context);
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

    public void setServiceManager (ServiceManager sm) {
        serviceManager = sm;
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new DroolsEndpoint(uri, remaining, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
