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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.ContainerLocator;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ByContextMappingInfoContainerLocatorTest {
    private static final String CONTAINER_ID_QUERY = "select cmi.ownerId from ContextMappingInfo cmi where cmi.contextId = :piId";
    private static final String CONTAINER_ID = "containerId";

    @Mock
    private EntityManagerFactory entityManagerFactory;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @Before
    public void setUp() {
        reset(entityManagerFactory, entityManager, query);
        when(entityManagerFactory.isOpen()).thenReturn(true);
        when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);

        when(entityManager.createQuery(CONTAINER_ID_QUERY)).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getSingleResult()).thenReturn(CONTAINER_ID);

        EntityManagerFactoryManager.get().clear();
    }

    @Test
    public void testLocateContainer() {
        EntityManagerFactoryManager.get()
            .addEntityManagerFactory(KieServerConstants.KIE_SERVER_PERSISTENCE_UNIT_NAME, entityManagerFactory);
        ContainerLocator locator = ByContextMappingInfoContainerLocator.Factory.get().create(1L);

        String containerId = locator.locateContainer("alias", null);

        assertEquals(CONTAINER_ID, containerId);
        verify(query, times(1)).getSingleResult();
    }

    @Test
    public void testQueryShouldBeExecutedOnes() {
        EntityManagerFactoryManager.get()
            .addEntityManagerFactory(KieServerConstants.KIE_SERVER_PERSISTENCE_UNIT_NAME, entityManagerFactory);
        ContainerLocator locator = ByContextMappingInfoContainerLocator.Factory.get().create(1L);

        String containerId = locator.locateContainer("alias", null);

        assertEquals(CONTAINER_ID, containerId);

        containerId = locator.locateContainer("alias", null);

        assertEquals(CONTAINER_ID, containerId);
        verify(query, times(1)).getSingleResult();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoResultException() {
        when(query.getSingleResult()).thenThrow(NoResultException.class);

        EntityManagerFactoryManager.get()
            .addEntityManagerFactory(KieServerConstants.KIE_SERVER_PERSISTENCE_UNIT_NAME, entityManagerFactory);
        ContainerLocator locator = ByContextMappingInfoContainerLocator.Factory.get().create(1L);

        locator.locateContainer("alias", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonUniqueResultException() {
        when(query.getSingleResult()).thenThrow(NonUniqueResultException.class);

        EntityManagerFactoryManager.get()
            .addEntityManagerFactory(KieServerConstants.KIE_SERVER_PERSISTENCE_UNIT_NAME, entityManagerFactory);
        ContainerLocator locator = ByContextMappingInfoContainerLocator.Factory.get().create(1L);

        locator.locateContainer("alias", null);
    }
}
