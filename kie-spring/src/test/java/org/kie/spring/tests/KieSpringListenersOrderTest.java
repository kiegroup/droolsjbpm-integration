/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertNotNull;

/**
 * bz761427 reproducer
 * One way of listeners configuration require specific order of elements, another one does not.
 * Probably because of <xsd:all> vs. <xsd:sequence> in drools-spring.xsd
 *
 */
public class KieSpringListenersOrderTest {
    private static ApplicationContext ctx;

    @BeforeClass
    public static void runBeforeClass() {
        ctx = new ClassPathXmlApplicationContext("org/kie/spring/listenersOrderTest.xml");
    }

    private KieSession getSession() {
        return (KieSession) ctx.getBean("ksession2");
    }

    /**
     * reproducer for https://bugzilla.redhat.com/show_bug.cgi?id=761435
     * <p/>
     * See org/drools/container/spring/listenersOrderTest.xml for further details.
     */
    @Test
    public void testListeners() {
        KieSession session = getSession();
        assertNotNull(session);
    }
}
