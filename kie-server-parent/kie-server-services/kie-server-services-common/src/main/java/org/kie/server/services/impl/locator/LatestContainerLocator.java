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

package org.kie.server.services.impl.locator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.compiler.kie.builder.impl.KieRepositoryImpl;
import org.kie.server.services.api.ContainerLocator;
import org.kie.server.services.api.KieContainerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Finds latest container for given alias based on GAV comparison.
 * It searches by same group id and artifact id and then uses maven resolution
 * to fine the latest version of all found containers.
 */
public class LatestContainerLocator implements ContainerLocator {
    private static final Logger logger = LoggerFactory.getLogger(LatestContainerLocator.class);

    private static LatestContainerLocator INSTANCE = new LatestContainerLocator();

    public static LatestContainerLocator get() {
        return INSTANCE;
    }

    @Override
    public String locateContainer(String alias, List<? extends KieContainerInstance> containerInstances) {
        if (containerInstances.isEmpty()) {
            return alias;
        }
        logger.debug("Searching for latest container for alias {} within available containers {}", alias, containerInstances);
        List<KieRepositoryImpl.ComparableVersion> comparableVersions = new ArrayList<KieRepositoryImpl.ComparableVersion>();
        Map<String, String> versionToIdentifier = new HashMap<String, String>();
        containerInstances.forEach(c ->
                {
                    comparableVersions.add(new KieRepositoryImpl.ComparableVersion(c.getKieContainer().getReleaseId().getVersion()));
                    versionToIdentifier.put(c.getKieContainer().getReleaseId().getVersion(), c.getContainerId());
                }
        );
        KieRepositoryImpl.ComparableVersion latest = Collections.max(comparableVersions);
        logger.debug("Latest version for alias {} is {}", alias, comparableVersions);
        return versionToIdentifier.get(latest.toString());
    }

}
