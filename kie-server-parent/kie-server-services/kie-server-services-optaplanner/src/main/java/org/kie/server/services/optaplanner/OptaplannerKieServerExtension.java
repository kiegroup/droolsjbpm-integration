/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.DroolsKieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OptaplannerKieServerExtension
        implements KieServerExtension {

    private static final Logger logger = LoggerFactory.getLogger(OptaplannerKieServerExtension.class);

    public static final String EXTENSION_NAME = "OptaPlanner";

    private static final Boolean DROOLS_DISABLED = Boolean.parseBoolean(
            System.getProperty(KieServerConstants.KIE_DROOLS_SERVER_EXT_DISABLED, "false"));
    private static final Boolean OPTAPLANNER_DISABLED = Boolean.parseBoolean(
            System.getProperty(KieServerConstants.KIE_OPTAPLANNER_SERVER_EXT_DISABLED, "false"));

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

    private final List<Object> services = new ArrayList<>();
    private boolean initialized = false;
    private OptaplannerCommandServiceImpl optaplannerCommandService;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return !OPTAPLANNER_DISABLED && !DROOLS_DISABLED;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        KieServerExtension droolsExtension = registry.getServerExtension(DroolsKieServerExtension.EXTENSION_NAME);
        if (droolsExtension == null) {
            logger.warn("No Drools extension available, quiting...");
            return;
        }
        this.registry = registry;
        // The following thread pool will have a max thread count equal to the number of cores on the machine minus 2,
        // leaving a few cores unoccupied to handle REST/JMS requests and run the OS.
        // If new jobs are submitted and all threads are busy, the default reject policy will kick in.
        int availableProcessorCount = Runtime.getRuntime().availableProcessors();
        int resolvedActiveThreadCount = Math.max(1, availableProcessorCount - 2);
        int queueSize = Integer.parseInt(System.getProperty(
                KieServerConstants.KIE_OPTAPLANNER_THREAD_POOL_QUEUE_SIZE, String.valueOf(resolvedActiveThreadCount)));
        logger.info("Creating a ThreadPoolExecutor with corePoolSize = " + resolvedActiveThreadCount + ","
                + " maximumPoolSize = " + resolvedActiveThreadCount + ", queueSize = " + queueSize);
        this.threadPool = new ThreadPoolExecutor(
                resolvedActiveThreadCount,
                resolvedActiveThreadCount,
                10, // thread keep alive time
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueSize)); // queue with a size
        this.solverServiceBase = new SolverServiceBase(registry, threadPool);

        this.optaplannerCommandService = new OptaplannerCommandServiceImpl(registry, solverServiceBase);

        this.services.add(solverServiceBase);

        initialized = true;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        if (this.threadPool != null) {
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
        solverServiceBase.disposeSolversForContainer(id, kieContainerInstance);
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<>();
        Object[] services = {solverServiceBase, registry};
        for (KieServerApplicationComponentsService appComponentsService : appComponentsServices) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(optaplannerCommandService.getClass())) {
            return (T) optaplannerCommandService;
        }
        if (serviceType.isAssignableFrom(solverServiceBase.getClass())) {
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

    @Override
    public List<Message> healthCheck(boolean report) {
        List<Message> messages = KieServerExtension.super.healthCheck(report);

        if (this.threadPool.isTerminated() && this.initialized) {
            messages.add(new Message(Severity.ERROR, getExtensionName() + " failed due to thread pool is terminated while the extension is still alive"));
        } else {
            if (report) {
                messages.add(new Message(Severity.INFO, getExtensionName() + " is alive"));
            }
        }
        return messages;
    }
}
