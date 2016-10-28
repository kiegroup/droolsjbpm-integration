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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.Policy;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.controller.DefaultRestControllerImpl;
import org.kie.server.services.impl.locator.LatestContainerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Policy that will dispose older container and leave only latest one - latest according to version of ReleaseId.
 *
 * Policy by default is scheduled to run once a day, though it can be reconfigured with system properties:
 * <ul>
 *     <li>policy.klo.interval - interval how often (expressed as duration) the policy should be applied</li>
 *     <li>policy.klo.unit - time unit that the interval was specified in - if not given milliseconds are assumed</li>
 * </ul>
 *
 * Name of this policy (to be used to activate it) is <code>KeepLatestOnly</code>
 */
public class KeepLatestContainerOnlyPolicy implements Policy {

    private static final Logger logger = LoggerFactory.getLogger(KeepLatestContainerOnlyPolicy.class);

    private static final String INTERVAL_VALUE = "policy.klo.interval";
    private static final String INTERVAL_TIME_UNIT = "policy.klo.unit";

    private static final String NAME = "KeepLatestOnly";

    private long interval;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public long getInterval() {
        return interval;
    }

    @Override
    public void start() {
        logger.debug("Starting KeepLatestContainerOnlyPolicy policy...");
        TimeUnit timeUnit = TimeUnit.valueOf(System.getProperty(INTERVAL_TIME_UNIT, TimeUnit.MILLISECONDS.toString()));
        long givenInterval = Long.parseLong(System.getProperty(INTERVAL_VALUE, "0"));

        if (givenInterval > 0) {
            timeUnit.convert(givenInterval, timeUnit);
            this.interval = TimeUnit.MILLISECONDS.convert(givenInterval, timeUnit);
        } else {
            // default once a day
            this.interval =  TimeUnit.MILLISECONDS.convert(1L, TimeUnit.DAYS);
        }
        logger.debug("Started {} policy", this);
    }

    @Override
    public void stop() {
        this.interval = -1;
        logger.debug("Stopped {} policy", this);
    }

    @Override
    public void apply(KieServerRegistry kieServerRegistry, KieServer kieServer) {

        DefaultRestControllerImpl controller = new DefaultRestControllerImpl(kieServerRegistry);

        List<String> containerAliases = kieServerRegistry.getContainerAliases();
        if (containerAliases.isEmpty()) {
            logger.debug("No containers found, quiting");
            return;
        }

        for (String alias : containerAliases) {

            List<KieContainerInstanceImpl> containerInstances = kieServerRegistry.getContainersForAlias(alias);
            if (containerInstances.isEmpty() || containerInstances.size() == 1) {
                logger.debug("Containers for alias {} are already on expected level (number of containers is {})", alias, containerInstances.size());
                continue;
            }

            String latestContainerId = LatestContainerLocator.get().locateContainer(alias, containerInstances);
            final Map<String, String> report = new HashMap<>();
            containerInstances.stream()
                    .filter(kci -> !kci.getContainerId().equals(latestContainerId))
                    .forEach(kci -> {
                        ServiceResponse<Void> response = kieServer.disposeContainer(kci.getContainerId());
                        report.put(kci.getContainerId(), response.getType().toString());
                        logger.debug("Dispose of container {} completed with {} message {}", kci.getContainerId(), response.getType().toString(), response.getMsg());
                        if (response.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
                            controller.stopContainer(kci.getContainerId());
                        }

            });

            logger.info("KeepLatestContainerOnlyPolicy applied to {} successfully (report {})", alias, report);
        }

    }

    @Override
    public String toString() {
        return "KeepLatestContainerOnlyPolicy{" +
                "interval=" + interval + " ms" +
                '}';
    }
}
