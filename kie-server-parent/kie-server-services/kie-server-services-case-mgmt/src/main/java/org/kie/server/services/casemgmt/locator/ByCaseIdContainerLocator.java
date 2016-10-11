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

package org.kie.server.services.casemgmt.locator;

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

/**
 * Locates container id for given case id.
 * To improve performance the operation of locating the container id is done only once
 * and stored as part of the instance of this class so in case of multiple method calls will require it
 * single instance of this class should be used to avoid too many look ups.
 */
public class ByCaseIdContainerLocator implements ContainerLocator {

    private static final Logger logger = LoggerFactory.getLogger(ByCaseIdContainerLocator.class);

    private static final String CONTAINER_ID_QUERY = "select log.externalId from ProcessInstanceLog log where log.correlationKey = :caseId";
    private String caseId;

    private String containerId;

    public ByCaseIdContainerLocator(String caseId) {
        this.caseId = caseId;
    }

    @Override
    public String locateContainer(String alias, List<? extends KieContainerInstance> containerInstances) {
        if (containerId != null) {
            logger.debug("Container id has already been found for case {} and is {}", caseId, containerId);
            return containerId;
        }
        logger.debug("Searching for container id for case id {} and alias {}", caseId, alias);
        EntityManager em = EntityManagerFactoryManager.get().getOrCreate(KieServerConstants.KIE_SERVER_PERSISTENCE_UNIT_NAME).createEntityManager();

        try {

            containerId = (String)em.createQuery(CONTAINER_ID_QUERY)
                    .setParameter("caseId", caseId)
                    .getSingleResult();
            logger.debug("Found container id '{}' for case id {}", containerId, caseId);
            return containerId;

        } catch (NoResultException | NonUniqueResultException e) {
            throw new IllegalArgumentException("Case with id " + caseId + " not found");
        } finally {
            em.close();
        }

    }
}
