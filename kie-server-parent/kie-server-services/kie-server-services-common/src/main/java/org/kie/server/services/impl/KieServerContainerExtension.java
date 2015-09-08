/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;

public class KieServerContainerExtension implements KieServerExtension {

    private static final String EXTENSION_NAME = "KieServer";
    private KieContainerCommandService batchCommandService;

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.batchCommandService = new KieContainerCommandServiceImpl(kieServer, registry);
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        // no-op
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // no-op
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // no-op
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices  = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList =  new ArrayList<Object>();
        Object [] services = {
                batchCommandService
        };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(batchCommandService.getClass())) {
            return (T) batchCommandService;
        }

        return null;
    }

    @Override
    public String getImplementedCapability() {
        return "KieServer";
    }

    @Override
    public String toString() {
        return "Server Default Extension";
    }
}
