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

package org.kie.server.services.jbpm.taskqueries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.jbpm.services.api.query.QueryService;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmTaskQueriesKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "jBPMTaskQueries";
    
    private static final Logger logger = LoggerFactory.getLogger(JbpmTaskQueriesKieServerExtension.class);

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_JBPM_SERVER_EXT_DISABLED, "false"));

    /*
    private boolean isExecutorAvailable = false;

    private String persistenceUnitName = KieServerConstants.KIE_SERVER_PERSISTENCE_UNIT_NAME;
    */

    private KieServerImpl kieServer;
    private KieServerRegistry context;
    
    private QueryService queryService;
    
    private List<Object> services = new ArrayList<Object>();
    private boolean initialized = false;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
    	this.kieServer = kieServer;
    	this.context = registry;
    	
    	//TODO: Double check if this is correct.
        //Requires jBPM Extension, as we depend on the QueryService.
        //KieServerExtension jBpmExtension = registry.getServerExtension( "jBPM" );
    	KieServerExtension jBpmExtension = registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        if ( jBpmExtension == null ) {
        	logger.error("No jBPM extension available, quiting...");
            return;
        }
        
        queryService = jBpmExtension.getAppComponents(QueryService.class);
        
        //Build the TaskQueryServiceBase
        //TODO: This is done by the REST extension.
        //this.taskQueryServiceBase = new TaskQueryServiceBase(null, registry, TaskQueriesStrategyFactory.getTaskQueriesStrategy());
        
        services.add(queryService);
        initialized = true;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        //TODO: Implement destroy logic (if applicable ...).
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    	//no-op
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    	//no-op
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }
    
    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load( KieServerApplicationComponentsService.class );
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {queryService, context};
        for ( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll( appComponentsService.getAppComponents( EXTENSION_NAME, type, services ) );
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if ( serviceType.isAssignableFrom( queryService.getClass() ) ) {
            return (T) queryService;
        }
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_BPM_TASK_QUERIES;
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
        return 50;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }
   
}
