/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm.locator;

import javax.persistence.EntityManager;

import org.kie.server.services.api.ContainerLocator;

/**
 * Locates container id for given process instance id.
 * To improve performance the operation of locating the container id is done only once
 * and stored as part of the instance of this class so in case of multiple method calls will require it
 * single instance of this class should be used to avoid too many look ups.
 */
public class ByContextMappingInfoContainerLocator extends ProcessContainerLocator {
    private static final String CONTAINER_ID_QUERY = "select cmi.ownerId from ContextMappingInfo cmi where cmi.contextId = :piId";

    private ByContextMappingInfoContainerLocator(final Long processInstanceId) {
        super(processInstanceId);
    }

    @Override
    protected String invokeQuery(final EntityManager em, final Long processInstanceId) {
        return (String) em.createQuery(CONTAINER_ID_QUERY)
            .setParameter("piId", String.valueOf(processInstanceId))
            .getSingleResult();
    }

    private static ByContextMappingInfoContainerLocator get(final Number processInstanceId) {
        return new ByContextMappingInfoContainerLocator(processInstanceId.longValue());
    }

    public static class Factory implements ContainerLocatorFactory{
        private static final Factory INSTANCE = new Factory();

        public static Factory get() {
            return INSTANCE;
        }

        @Override
        public ContainerLocator create(final Number processInstanceId) {
            return ByContextMappingInfoContainerLocator.get(processInstanceId);
        }
    }
}
