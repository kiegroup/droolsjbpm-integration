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

package org.kie.server.services.drools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieContainerCommandServiceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsKieServerExtension implements KieServerExtension {

    private static final Logger logger = LoggerFactory.getLogger(DroolsKieServerExtension.class);

    public static final String EXTENSION_NAME = "Drools";

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED, "false"));

    private KieContainerCommandService batchCommandService;
    private KieServerRegistry registry;

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.batchCommandService = new KieContainerCommandServiceImpl(kieServer, registry);
        this.registry = registry;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        // no-op?
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // do any other bootstrapping rule service requires
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();

        KieModuleMetaData metaData = KieModuleMetaData.Factory.newKieModuleMetaData(kieContainerInstance.getKieContainer().getReleaseId());
        Collection<String> packages = metaData.getPackages();

        for (String p : packages) {
            Collection<String> classes = metaData.getClasses(p);

            for (String c : classes) {
                String type = p + "." + c;
                try {
                    extraClasses.add(Class.forName(type, true, kieContainerInstance.getKieContainer().getClassLoader()));
                    logger.debug("Added {} type into extra jaxb classes set", type);
                } catch (ClassNotFoundException e) {
                    logger.warn("Unable to create instance of type {} due to {}", type, e.getMessage());
                    logger.debug("Complete stack trace for exception while creating type {}", type, e);
                }
            }
        }

        kieContainerInstance.addJaxbClasses(extraClasses);
        kieContainerInstance.addService(batchCommandService);
    }

    @Override
    public void disposeContainer(String id, Map<String, Object> parameters) {
        // no-op?
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
            = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList =  new ArrayList<Object>();
        Object [] services = { 
                batchCommandService,
                registry

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
        return "BRM";
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }
}
