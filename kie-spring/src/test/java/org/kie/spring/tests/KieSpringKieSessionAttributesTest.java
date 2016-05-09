/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import org.drools.core.time.SessionPseudoClock;
import org.drools.core.time.impl.JDKTimerService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class KieSpringKieSessionAttributesTest {

    private static AbstractApplicationContext context = null;

    @BeforeClass
    public static void setup() {
        context = new ClassPathXmlApplicationContext("org/kie/spring/kiesession-attributes.xml");
    }

    @Test
    public void testContext() throws Exception {
        assertNotNull(context);
    }

    @Test
    public void testStatefulRealTimeClockKieSession() throws Exception {
        KieSession ksession = context.getBean("statefulSessionRealTime", KieSession.class);
        assertNotNull(ksession);
        assertEquals("Session has configured different clock type", ClockTypeOption.get("realtime"), ksession.getSessionConfiguration().getOption(ClockTypeOption.class));
        assertTrue(String.format("Session clock not an instance of '~s', but: '~s'.", JDKTimerService.class.getSimpleName(), ksession.getSessionClock().getClass().getSimpleName()),
                   ksession.getSessionClock() instanceof JDKTimerService);
    }

    @Test
    public void testStatefulPseudoClockKieSession() throws Exception {
        KieSession ksession = context.getBean("statefulSessionPseudo", KieSession.class);
        assertNotNull(ksession);
        assertEquals("Session has configured different clock type", ClockTypeOption.get("pseudo"), ksession.getSessionConfiguration().getOption(ClockTypeOption.class));
        assertTrue(String.format("Session clock not an instance of '~s', but '~s'.", SessionPseudoClock.class.getSimpleName(), ksession.getSessionClock().getClass().getSimpleName()),
                ksession.getSessionClock() instanceof SessionPseudoClock);
    }

    @AfterClass
    public static void tearDown() {
        context.destroy();
    }

}
