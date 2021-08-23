/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.services.jbpm.locator;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;

import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.ContainerLocator;
import org.kie.server.services.api.KieContainerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ProcessContainerLocator implements ContainerLocator  {
    private static final Logger logger = LoggerFactory.getLogger(ByContextMappingInfoContainerLocator.class);

    private final Long processInstanceId;
    private String containerId;

    protected ProcessContainerLocator(final Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
    public String locateContainer(final String alias, final List<? extends KieContainerInstance> containerInstances) {
        if (containerId != null) {
            logger.debug("Container id has already be found for process instance {} and is {}", processInstanceId, containerId);
            return containerId;
        }

        logger.debug("Searching for container id for process instance id {} and alias {}", processInstanceId, alias);
        EntityManager em = EntityManagerFactoryManager.get()
            .getOrCreate(KieServerConstants.KIE_SERVER_PERSISTENCE_UNIT_NAME)
            .createEntityManager();

        try {

            containerId = invokeQuery(em, processInstanceId);
            logger.debug("Found container id '{}' for process instance id {}", containerId, processInstanceId);
            return containerId;

        } catch (NoResultException e) {
            throw new IllegalArgumentException("ProcessInstance with id " + processInstanceId + " not found");
        } catch (NonUniqueResultException e) {
            throw new IllegalArgumentException("Multiple containerIds found for processInstanceId " + processInstanceId);
        } finally {
            em.close();
        }
    }

    protected abstract String invokeQuery(final EntityManager em, final Long processInstanceId);
}
