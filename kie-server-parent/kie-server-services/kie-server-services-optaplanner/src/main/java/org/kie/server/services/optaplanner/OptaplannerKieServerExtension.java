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

package org.kie.server.services.optaplanner;

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

public class OptaplannerKieServerExtension
        implements KieServerExtension {

    private static final Logger logger = LoggerFactory.getLogger( OptaplannerKieServerExtension.class );

    public static final String EXTENSION_NAME = "OptaPlanner";

    private static final Boolean droolsDisabled = Boolean.parseBoolean( System.getProperty( KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED, "false" ) );
    private static final Boolean disabled       = Boolean.parseBoolean( System.getProperty( KieServerConstants.KIE_OPTAPLANNER_SERVER_EXT_DISABLED, "false" ) );

    private KieServerRegistry registry;
    private SolverServiceBase solverServiceBase;

    // Optaplanner requires threads to run solvers
    // asynchronously. We can't use JEE managed threads
    // for this because they would red-flag the threads
    // that run for long periods. This first implementation
    // will use a standard java thread pool for the job.
    // If necessary, we will need to look for alternatives
    // in the future.
    private ExecutorService threadPool = null;

    private List<Object> services = new ArrayList<Object>();
    private boolean initialized = false;
    private OptaplannerCommandServiceImpl optaplannerCommandService;

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
        KieServerExtension droolsExtension = registry.getServerExtension( "Drools" );
        if ( droolsExtension == null ) {
            logger.warn( "No Drools extension available, quiting..." );
            return;
        }
        this.registry = registry;
        // the following threadpool will have a max thread count equal to the number of cores on the machine.
        // if new jobs are submited and all threads are busy, the reject policy will kick in.
        int poolSize = Runtime.getRuntime().availableProcessors();
        if ( poolSize >= 4 ) {
            // Leave 1 processor alone to handle REST/JMS requests and run the OS
            poolSize--;
        }
        this.threadPool = new ThreadPoolExecutor(
                Math.min(2, poolSize), // core size
                poolSize, // max size
                120, // idle timeout
                                                 TimeUnit.SECONDS,
                                                 new ArrayBlockingQueue<Runnable>(poolSize)); // queue with a size
        this.solverServiceBase = new SolverServiceBase( registry, threadPool );

        this.optaplannerCommandService = new OptaplannerCommandServiceImpl(registry, solverServiceBase);

        this.services.add( solverServiceBase );

        initialized = true;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        if( this.threadPool != null ) {
            this.threadPool.shutdownNow();
        }
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // same as createContainer - no op
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        solverServiceBase.disposeSolversForContainer( id, kieContainerInstance );
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load( KieServerApplicationComponentsService.class );
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {solverServiceBase, registry};
        for ( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll( appComponentsService.getAppComponents( EXTENSION_NAME, type, services ) );
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(optaplannerCommandService.getClass())) {
            return (T) optaplannerCommandService;
        }
        if ( serviceType.isAssignableFrom( solverServiceBase.getClass() ) ) {
            return (T) solverServiceBase;
        }
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_BRP;
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
        return 25;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

}
