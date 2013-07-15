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
package org.kie.spring.tests;

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.drools.core.marshalling.impl.IdentityPlaceholderResolverStrategy;
import org.drools.core.marshalling.impl.SerializablePlaceholderResolverStrategy;
import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Server;
import org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.builder.ReleaseId;
import org.kie.api.marshalling.ObjectMarshallingStrategy;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.spring.InternalKieSpringUtils;
import org.kie.spring.mocks.MockObjectMarshallingStrategy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

import static org.junit.Assert.*;

public class KieSpringEnvironmentTest {

    static ApplicationContext context = null;
    private static Server h2Server;

    @BeforeClass
    public static void startH2Database() throws Exception {
        DeleteDbFiles.execute("",
                "DroolsFlow",
                true);
        h2Server = Server.createTcpServer(new String[0]);
        h2Server.start();
    }

    @AfterClass
    public static void stopH2Database() throws Exception {
        h2Server.stop();
        DeleteDbFiles.execute("",
                "DroolsFlow",
                true);
    }

    @BeforeClass
    public static void runBeforeClass() {
        ReleaseId releaseId = new ReleaseIdImpl("kie-spring-commands","test-spring","0001");
        context = InternalKieSpringUtils.getSpringContext(releaseId,
                KieSpringEnvironmentTest.class.getResource("/org/kie/spring/environment.xml"),
                new File(KieSpringEnvironmentTest.class.getResource("/").getFile()));
    }

    @Test
    public void testCtxNotNull() throws Exception {
        assertNotNull(context);
    }

    @Test
    public void testEnvRef() throws Exception {
        Environment environment = (Environment) context.getBean("drools-env");
        assertNotNull(environment);

        assertNotNull(environment.get(EnvironmentName.TRANSACTION_MANAGER));
        assertNotNull(environment.get(EnvironmentName.ENTITY_MANAGER_FACTORY));
        assertNotNull(environment.get(EnvironmentName.GLOBALS));
        assertNotNull(environment.get(EnvironmentName.DATE_FORMATS));
        assertNotNull(environment.get(EnvironmentName.CALENDARS));

        assertNotNull(environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES));
        assertEquals(4, ((ObjectMarshallingStrategy[]) environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES)).length);
    }

    @Test
    public void testEnvEmb() throws Exception {
        Environment environment = (Environment) context.getBean("drools-env-embedded");
        assertNotNull(environment);

        assertNotNull(environment.get(EnvironmentName.TRANSACTION_MANAGER));
        assertNotNull(environment.get(EnvironmentName.ENTITY_MANAGER_FACTORY));
        assertNotNull(environment.get(EnvironmentName.GLOBALS));
        assertNotNull(environment.get(EnvironmentName.DATE_FORMATS));
        assertNotNull(environment.get(EnvironmentName.CALENDARS));

        assertNotNull(environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES));
        assertEquals(2, ((ObjectMarshallingStrategy[]) environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES)).length);
    }

    @Test
    public void testEnvCustomMarshallerNested() throws Exception {
        Environment environment = (Environment) context.getBean("drools-env-custom-marshaller-nested");
        assertNotNull(environment);

        assertNotNull(environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES));
        ObjectMarshallingStrategy[] objectMarshallingStrategies = (ObjectMarshallingStrategy[]) environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES);
        assertEquals(1, objectMarshallingStrategies.length);
        assertEquals(objectMarshallingStrategies[0].getClass().getName(), "org.kie.spring.mocks.MockObjectMarshallingStrategy");
    }

    @Test
    public void testEnvCustomMarshallerRef() throws Exception {
        Environment environment = (Environment) context.getBean("drools-env-custom-marshaller-ref");
        assertNotNull(environment);

        ObjectMarshallingStrategy[] objectMarshallingStrategies = (ObjectMarshallingStrategy[]) environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES);
        assertEquals(1, objectMarshallingStrategies.length);
        assertEquals(objectMarshallingStrategies[0].getClass().getName(), "org.kie.spring.mocks.MockObjectMarshallingStrategy");
    }

    @Test
    public void testEnvMarshallerOrder() throws Exception {
        Environment environment = (Environment) context.getBean("drools-env");
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
        Environment environment = (Environment) context.getBean("drools-env-custom-marshaller-mixed");
        assertNotNull(environment);

        ObjectMarshallingStrategy[] objectMarshallingStrategies = (ObjectMarshallingStrategy[]) environment.get(EnvironmentName.OBJECT_MARSHALLING_STRATEGIES);
        assertEquals(5, objectMarshallingStrategies.length);
        assertTrue(objectMarshallingStrategies[0] instanceof SerializablePlaceholderResolverStrategy);
        assertTrue(objectMarshallingStrategies[1] instanceof IdentityPlaceholderResolverStrategy);
        assertTrue(objectMarshallingStrategies[2] instanceof JPAPlaceholderResolverStrategy);
        assertTrue(objectMarshallingStrategies[3] instanceof MockObjectMarshallingStrategy);
        assertTrue(objectMarshallingStrategies[4] instanceof ProcessInstanceResolverStrategy);
    }
}
