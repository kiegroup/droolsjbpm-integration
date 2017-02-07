/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.remote.rest.common;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.kie.server.remote.rest.common.resource.KieServerRestImpl;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;

@ApplicationPath("/")
public class KieServerApplication extends Application {

    private final Set<Object> instances = new CopyOnWriteArraySet<Object>() {
        private static final long serialVersionUID = 1763183096852523317L;
        {
            KieServerImpl server = KieServerLocator.getInstance();

            add(new KieServerRestImpl(server));

            // next add any resources from server extensions
            List<KieServerExtension> extensions = server.getServerExtensions();

            for (KieServerExtension extension : extensions) {
                addAll(extension.getAppComponents(SupportedTransports.REST));
            }
        }
    };

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }

    @Override
    public Set<Object> getSingletons() {
        return instances;
    }

}
