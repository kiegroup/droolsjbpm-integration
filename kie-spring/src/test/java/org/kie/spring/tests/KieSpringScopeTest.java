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
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.*;

public class KieSpringScopeTest {
    private static final Logger log = LoggerFactory.getLogger(KieSpringScopeTest.class);
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
    @Test
    public void testConcurrentlyGetStatefulPrototypeKieSession() throws Exception {
        final int nThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        final ConcurrentHashMap ksessionMap = new ConcurrentHashMap();

        CountDownLatch latch = new CountDownLatch(nThreads);

        for (int i = 0; i < nThreads; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    KieSession ksession = (KieSession) context.getBean("statefulPrototypeSession");
                    Object put = ksessionMap.put(ksession.hashCode(), new Object());
                    if (put != null) {
                        log.warn("ksession:{} repeated", ksession);
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        assertEquals(nThreads,ksessionMap.size());
    }

    @AfterClass
    public static void tearDown() {

    }

}
