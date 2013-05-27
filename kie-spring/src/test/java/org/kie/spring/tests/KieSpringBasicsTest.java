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
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.beans.Person;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KieSpringBasicsTest {

    static ClassPathXmlApplicationContext context = null;

    @BeforeClass
    public static void setup() {
        context = new ClassPathXmlApplicationContext("org/kie/spring/kie-beans.xml");
    }

//    @Test
//    public void testKContainer() throws Exception {
//        KieContainer kieContainer = (KieContainer) context.getBean("kmodule");
//        assertNotNull(kieContainer);
//        System.out.println("kieContainer.getReleaseId() == "+kieContainer.getReleaseId());
//    }

    @Test
    public void testKieBase() throws Exception {
        KieBase kbase = (KieBase) context.getBean("drl_kiesample");
        assertNotNull(kbase);
    }

    @Test
    public void testReleaseId() throws Exception {
        ReleaseId releaseId = (ReleaseId) context.getBean("dummyReleaseId");
        assertNotNull(releaseId);
    }

    @Test
    public void testKieSessionRef() throws Exception {
        StatelessKieSession ksession = (StatelessKieSession) context.getBean("ksession1");
        assertNotNull(ksession);
    }

    @Test
    public void testKieSession() throws Exception {
        StatelessKieSession ksession = (StatelessKieSession) context.getBean("ksession9");
        assertNotNull(ksession);
    }

    @Test
    public void testKieSessionDefaultType() throws Exception {
        Object obj = context.getBean("ksession99");
        assertNotNull(obj);
        assertTrue(obj instanceof KieSession);
    }


    @Test
    public void testKSessionExecution() throws Exception {
        StatelessKieSession ksession = (StatelessKieSession) context.getBean("ksession1");
        assertNotNull(ksession);
        Person person = new Person("HAL", 42);
        person.setHappy(false);
        ksession.execute(person);
        assertTrue(person.isHappy());
    }

    @AfterClass
    public static void tearDown() {
        context.destroy();
    }

}
