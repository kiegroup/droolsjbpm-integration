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

import java.net.URI;
import java.util.Map;
import javax.naming.InitialContext;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;

public class KieComponent extends DefaultComponent {

    public static final String KIE_CLIENT = "kie-client";
    public static final String KIE_OPERATION = "kie-operation";

    public KieComponent() { }

    public KieComponent(CamelContext context ) {
        super(context);
    }

    protected Endpoint createEndpoint( String uri, String remaining, Map<String, Object> parameters ) throws Exception {
        KieConfiguration kieConfiguration = new KieConfiguration();
        kieConfiguration.configure( new URI( uri ) );
        setProperties(kieConfiguration, parameters);

        KieServicesConfiguration config = remaining.startsWith( "jms" ) ?
                                          KieServicesFactory.newJMSConfiguration( (InitialContext)null, null, null ) :
                                          KieServicesFactory.newRestConfiguration( remaining, null, null );

        config.setUserName( kieConfiguration.getUsername() );
        config.setPassword( kieConfiguration.getPassword() );
        return new KieEndpoint(uri, this, config);
    }
}
