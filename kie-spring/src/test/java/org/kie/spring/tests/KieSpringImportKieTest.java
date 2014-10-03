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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.spring.beans.Person;
import org.kie.spring.beans.annotations.BeanWithReleaseId;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.*;

public class KieSpringImportKieTest {

    static ApplicationContext context = null;

    @BeforeClass
    public static void setup() {
        context = new ClassPathXmlApplicationContext("org/kie/spring/kie-import.xml");
    }

    @Test
    public void testContext() throws Exception {
        assertNotNull(context);
    }

    @Test
    public void testNamedKieBase() throws Exception {
        KieBase kieBase = context.getBean("kbase1", KieBase.class);
        assertNotNull(kieBase);
    }

    @Test
    public void testNamedKieSession() throws Exception {
        KieSession kieSession = context.getBean("ksession1", KieSession.class);
        assertNotNull(kieSession);
    }

    @Test
    public void testRegularPojo() throws Exception {
        Person p = context.getBean("person", Person.class);
        assertNotNull(p);
    }

    @Test
    public void testAnnotatedBeanKContainer() throws Exception {
        BeanWithReleaseId bean = context.getBean("annotatedBean", BeanWithReleaseId.class);
        assertNotNull(bean);
        assertNotNull(bean.getKieContainer());
    }

    @Test
    public void testAnnotatedBeanKieBase() throws Exception {
        BeanWithReleaseId bean = context.getBean("annotatedBean", BeanWithReleaseId.class);
        assertNotNull(bean);
        assertNotNull(bean.getKieBase());
    }

    @Test
    public void testAnnotatedBeanKieSession() throws Exception {
        BeanWithReleaseId bean = context.getBean("annotatedBean", BeanWithReleaseId.class);
        assertNotNull(bean);
        assertNotNull(bean.getKieSession());
    }

    @AfterClass
    public static void tearDown() {

    }

}
