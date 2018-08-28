/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.controller.common;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.kie.server.controller.service.StandaloneKieServerControllerImpl;
import org.kie.server.controller.service.StandaloneRuntimeManagementServiceImpl;
import org.kie.server.controller.service.StandaloneSpecManagementServiceImpl;


@ApplicationPath("/")
public class StandaloneControllerApplication extends Application {
    
    private final Set<Object> instances;
    
    public StandaloneControllerApplication() {
        instances = new CopyOnWriteArraySet<Object>() {
            private static final long serialVersionUID = 1763183096852523317L;
            {                
                add(new StandaloneKieServerControllerImpl());
                add(new StandaloneSpecManagementServiceImpl());
                add(new StandaloneRuntimeManagementServiceImpl());
            }
        };
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Collections.emptySet();
    }

    @Override
    public Set<Object> getSingletons() {
        return instances;
    }
}
