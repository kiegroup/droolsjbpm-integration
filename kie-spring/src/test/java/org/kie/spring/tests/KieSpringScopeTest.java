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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.beans.Person;
import org.kie.spring.beans.SampleBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KieSpringScopeTest {

    static ApplicationContext context = null;

    @BeforeClass
    public static void setup() {
        context = new ClassPathXmlApplicationContext("org/kie/spring/beans-with-scope.xml");
    }

    @Test
    public void testContext() throws Exception {
        assertNotNull(context);
    }


    @Test
    public void testStatelessPrototypeKieSession() throws Exception {
        StatelessKieSession ksession = (StatelessKieSession) context.getBean("statelessPrototypeSession");
        assertNotNull(ksession);

        StatelessKieSession anotherKsession = (StatelessKieSession) context.getBean("statelessPrototypeSession");
        assertNotNull(anotherKsession);
        assertNotEquals(ksession.hashCode(), anotherKsession.hashCode());
    }

    @Test
    public void testStatelessSingletonKieSession() throws Exception {
        StatelessKieSession ksession = (StatelessKieSession) context.getBean("statelessSingletonSession");
        assertNotNull(ksession);

        StatelessKieSession anotherKsession = (StatelessKieSession) context.getBean("statelessSingletonSession");
        assertNotNull(anotherKsession);
        assertEquals(ksession.hashCode(), anotherKsession.hashCode());
    }

    @Test
    public void testStatefulSingletonKieSession() throws Exception {
        KieSession ksession = (KieSession) context.getBean("statefulSingletonSession");
        assertNotNull(ksession);

        KieSession anotherKsession = (KieSession) context.getBean("statefulSingletonSession");
        assertNotNull(anotherKsession);

        assertEquals(ksession.hashCode(), anotherKsession.hashCode());
    }

    @Test
    public void testStatefulPrototypeKieSession() throws Exception {
        KieSession ksession = (KieSession) context.getBean("statefulPrototypeSession");
        assertNotNull(ksession);

        KieSession anotherKsession = (KieSession) context.getBean("statefulPrototypeSession");
        assertNotNull(anotherKsession);

        assertNotEquals(ksession.hashCode(), anotherKsession.hashCode());
    }

    @AfterClass
    public static void tearDown() {

    }

}
