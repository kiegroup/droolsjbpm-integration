/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl.policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.naming.InitialContext;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for complete life cycle of the policies and their execution.
 */
public class PolicyManager {

    private static final Logger logger = LoggerFactory.getLogger(PolicyManager.class);
    private static final ServiceLoader<Policy> policyServiceLoader = ServiceLoader.load(Policy.class);

    private Map<String, Policy> registeredPolicies = new HashMap<>();
    private Map<String, ScheduledFuture<?>> activatedPolicies = new HashMap<>();

    private KieServer kieServer;
    private KieServerRegistry kieServerRegistry;

    private ScheduledExecutorService scheduledExecutorService = getScheduledExecutorService();


    public synchronized void start(KieServer kieServer, KieServerRegistry kieServerRegistry) {
        this.kieServer = kieServer;
        this.kieServerRegistry = kieServerRegistry;
        logger.debug("Starting policy manager...");
        policyServiceLoader.forEach( p -> {
            registeredPolicies.put(p.getName(), p);
            logger.info("Registered {} policy under name {}", p, p.getName());
        });

        String toActivate = System.getProperty(KieServerConstants.KIE_SERVER_ACTIVATE_POLICIES);
        if (toActivate != null) {
            String[] policies = toActivate.split(",");
            logger.debug("Following policies will be activated {}", policies);
            for (String policy : policies) {
                String policyName = policy.trim();

                activatePolicy(policyName);
            }
        }
        logger.info("Policy manager started successfully, activated policies are {}", activatedPolicies.keySet());
    }

    public synchronized void stop() {
        logger.debug("Stopping policy manager...");

        List<String> activePolicies = new ArrayList<>(activatedPolicies.keySet());
        activePolicies.forEach(policyName -> {
            deactivatePolicy(policyName);
        });
        activatedPolicies.clear();
        logger.info("Policy manager stopped successfully");
    }

    public void activatePolicy(String policyName) {
        Policy policyInstance = registeredPolicies.get(policyName);
        if (policyInstance == null) {
            logger.warn("Policy '{}' requested to be activated but was not registered, known policies are {}", policyName, registeredPolicies.keySet());
            return;
        }

        try {
            logger.debug("Starting policy {}", policyInstance);
            policyInstance.start();
            logger.debug("Policy {} successfully started", policyInstance);

            long interval = policyInstance.getInterval();
            if (interval <= 0) {
                logger.error("Policy {} returned invalid (must be bigger than 0) interval {}, won't be activated", policyInstance, interval);
                return;
            }

            ScheduledFuture<?> future = scheduledExecutorService.scheduleAtFixedRate(() -> {
                    logger.debug("About to apply policy {} at {}", policyInstance, new Date());
                    try {
                        policyInstance.apply(this.kieServerRegistry, this.kieServer);
                        logger.debug("Policy {} applied successfully at {}", policyInstance, new Date());
                    } catch (Throwable e) {
                        logger.error("Policy {} failed to be applied due to {}", policyInstance, e.getMessage(), e);
                    }

                },
                interval,
                    interval,
                TimeUnit.MILLISECONDS);

            logger.debug("Policy {} successfully activated, will be applied at {}", policyInstance, new Date(System.currentTimeMillis() + future.getDelay(TimeUnit.MILLISECONDS)));
            activatedPolicies.put(policyName, future);
        } catch (Exception e) {
            logger.error("Failed during activation of policy {} due to {}", policyInstance, e.getMessage(), e);
        }
    }

    public void deactivatePolicy(String policyName) {
        Policy policy = registeredPolicies.get(policyName);
        ScheduledFuture<?> future = activatedPolicies.remove(policyName);
        future.cancel(true);
        logger.debug("Policy {} deactivated successfully", policy);

        policy.stop();
        logger.debug("Policy {} stopped successfully", policy);
    }

    protected ScheduledExecutorService getScheduledExecutorService() {
        ScheduledExecutorService executorService = null;
        try {
            executorService = InitialContext.doLookup("java:comp/DefaultManagedScheduledExecutorService");
            logger.debug("JEE version of scheduled executor service found");
        } catch (Exception e) {
            executorService = Executors.newSingleThreadScheduledExecutor();
            logger.debug("Cannot find managed scheduled executor service using standard one instead", e);
        }
        logger.debug("Executor service to be used is {}", executorService);
        return executorService;
    }
}
