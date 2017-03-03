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

package org.kie.server.services.dmn;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.*;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.*;

public class DMNKieServerExtension
        implements KieServerExtension {

    private static final Logger LOG = LoggerFactory.getLogger( DMNKieServerExtension.class );

    public static final String EXTENSION_NAME = "DMN";
    
    private static final Boolean droolsDisabled = Boolean.parseBoolean( System.getProperty( KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED, "false" ) );
    private static final Boolean disabled       = Boolean.parseBoolean( System.getProperty( KieServerConstants.KIE_DMN_SERVER_EXT_DISABLED, "false" ) );

    private KieServerRegistry registry;

    private List<Object> services = new ArrayList<Object>();
    private boolean initialized = false;

    private ModelEvaluatorServiceBase modelEvaluatorServiceBase;
    private DMNKieContainerCommandServiceImpl commandService;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return disabled == false && droolsDisabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.registry = registry;
        this.modelEvaluatorServiceBase = new ModelEvaluatorServiceBase(registry);
        this.commandService = new DMNKieContainerCommandServiceImpl(registry, modelEvaluatorServiceBase);
        initialized = true;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        if (!initialized) {
            return;
        }
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        if (!initialized) {
            return;
        }
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        if (!initialized) {
            return;
        }
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        if (!initialized) {
            return;
        }
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load( KieServerApplicationComponentsService.class );
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {modelEvaluatorServiceBase, registry};
        for ( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll( appComponentsService.getAppComponents( EXTENSION_NAME, type, services ) );
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if ( serviceType.isAssignableFrom( modelEvaluatorServiceBase.getClass() ) ) {
            return (T) modelEvaluatorServiceBase;
        }
        if ( serviceType.isAssignableFrom( commandService.getClass() ) ) {
            return (T) commandService;
        }
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_DMN;
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return 47;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

}
