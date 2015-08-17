/*
 * Copyright 2013 JBoss Inc
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
package org.kie.aries.blueprint.tests;

import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.drools.core.base.CalendarsImpl;
import org.drools.core.base.MapGlobalResolver;
import org.drools.core.marshalling.impl.IdentityPlaceholderResolverStrategy;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.aries.blueprint.KieBlueprintContainer;
import org.kie.aries.blueprint.mocks.MockEntityManager;
import org.kie.aries.blueprint.mocks.MockJpaTransactionManager;
import org.kie.aries.blueprint.mocks.MockObjectMarshallingStrategy;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

@Ignore("Add when org.apache.aries.blueprint.noosgi 1.0.1 is released")
public class KieBlueprintEnvironmentTest {

    static BlueprintContainerImpl container = null;
    
    @BeforeClass
    public static void runBeforeClass() throws Exception {
        List<URL> urls = new ArrayList<URL>();
        urls.add(KieBlueprintListenerTest.class.getResource("/org/kie/aries/blueprint/environment.xml"));
        container = new KieBlueprintContainer(ClassLoader.getSystemClassLoader(), urls);
    }

    @Test
    public void testCtxNotNull() throws Exception {
        assertNotNull(container);
    }

    @Test
    public void testEnvRef() throws Exception {
        Environment environment = (Environment) container.getComponentInstance("drools-env");
        assertNotNull(environment);
    }

    @Test
    public void testEnvRefTransManager() throws Exception {
        Environment environment = (Environment) container.getComponentInstance("drools-env");
        assertNotNull(environment);

        assertNotNull(environment.get(EnvironmentName.TRANSACTION_MANAGER));
        assertTrue(environment.get(EnvironmentName.TRANSACTION_MANAGER) instanceof MockJpaTransactionManager);
    }

    @Test
    public void testEnvRefEMF() throws Exception {
        Environment environment = (Environment) container.getComponentInstance("drools-env");
        assertNotNull(environment);

        assertNotNull(environment.get(EnvironmentName.ENTITY_MANAGER_FACTORY));
        assertTrue(environment.get(EnvironmentName.ENTITY_MANAGER_FACTORY) instanceof MockEntityManager);
    }

    @Test
    public void testEnvRefGlobals() throws Exception {
        Environment environment = (Environment) container.getComponentInstance("drools-env");
        assertNotNull(environment);

        assertNotNull(environment.get(EnvironmentName.GLOBALS));
        assertTrue(environment.get(EnvironmentName.GLOBALS) instanceof MapGlobalResolver);
    }

    @Test
    public void testEnvRefCalendars() throws Exception {
        Environment environment = (Environment) container.getComponentInstance("drools-env");
        assertNotNull(environment);

        assertNotNull(environment.get(EnvironmentName.CALENDARS));
        assertTrue(environment.get(EnvironmentName.CALENDARS) instanceof CalendarsImpl);
    }

    @Test
    public void testEmptyEnvRef() throws Exception {
        Environment environment = (Environment) container.getComponentInstance("drools-empty-env");
        assertNotNull(environment);
    }

    @Test
    public void testEnvCustomMarshallerRef() throws Exception {
        Environment environment = (Environment) container.getComponentInstance("drools-env-custom-marshaller-ref");
        assertNotNull(environment);

        ObjectMarshallingStrategy[] objectMarshallingStrategies = (ObjectMarshallingStrategy[]) environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES);
        assertEquals(1, objectMarshallingStrategies.length);
        assertEquals(objectMarshallingStrategies[0].getClass().getName(), "org.kie.aries.blueprint.mocks.MockObjectMarshallingStrategy");
    }

    @Test
    public void testEnvMarshallerOrder() throws Exception {
        Environment environment = (Environment) container.getComponentInstance("drools-env");
        assertNotNull(environment);

        ObjectMarshallingStrategy[] objectMarshallingStrategies = (ObjectMarshallingStrategy[]) environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES);
        assertEquals(4, objectMarshallingStrategies.length);
        assertTrue(objectMarshallingStrategies[0] instanceof SerializablePlaceholderResolverStrategy);
        assertTrue(objectMarshallingStrategies[1] instanceof IdentityPlaceholderResolverStrategy);
        assertTrue(objectMarshallingStrategies[2] instanceof JPAPlaceholderResolverStrategy);
        assertTrue(objectMarshallingStrategies[3] instanceof ProcessInstanceResolverStrategy);
    }

    @Test
    public void testEnvMarshallerOrderWithCustom() throws Exception {
        Environment environment = (Environment) container.getComponentInstance("drools-env-custom-marshaller-mixed");
        assertNotNull(environment);

        ObjectMarshallingStrategy[] objectMarshallingStrategies = (ObjectMarshallingStrategy[]) environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES);
        assertEquals(5, objectMarshallingStrategies.length);
        assertTrue(objectMarshallingStrategies[0] instanceof SerializablePlaceholderResolverStrategy);
        assertTrue(objectMarshallingStrategies[1] instanceof IdentityPlaceholderResolverStrategy);
        assertTrue(objectMarshallingStrategies[2] instanceof JPAPlaceholderResolverStrategy);
        assertTrue(objectMarshallingStrategies[3] instanceof MockObjectMarshallingStrategy);
        assertTrue(objectMarshallingStrategies[4] instanceof ProcessInstanceResolverStrategy);
    }

    @AfterClass
    public static void tearDown(){
        container.destroy();
    }
}
