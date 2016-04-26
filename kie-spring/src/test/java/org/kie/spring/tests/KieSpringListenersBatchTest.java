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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.api.runtime.KieSession;
import org.kie.spring.beans.Person;
import org.kie.spring.mocks.MockIncrementingRuleRuntimeEventListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.util.AssertionErrors.assertEquals;

public class KieSpringListenersBatchTest {

    static ApplicationContext context = null;
    List<Person> list = new ArrayList<Person>();

    @BeforeClass
    public static void runBeforeClass() {
        context = new ClassPathXmlApplicationContext("org/kie/spring/listeners-with-batch.xml");
    }

    @Before
    public void clearGlobalList() {
        list.clear();
    }

    @Test
    public void testStatefulWMEventListener() throws Exception {
        KieSession kSession = (KieSession) context.getBean("ksession1");
        assertTrue(kSession.getRuleRuntimeEventListeners().size() > 0);
        final MockIncrementingRuleRuntimeEventListener listener = findMockIncrementingRuleRuntimeEventListener(kSession);

        // if a Person is inserted here into the KieSession and fireAllRules called, the assertion succeeds
        assertEquals("Counter incremented unexpected number of times.", 1, listener.getCounter());
    }

    private MockIncrementingRuleRuntimeEventListener findMockIncrementingRuleRuntimeEventListener(KieSession kieSession) {
        for (RuleRuntimeEventListener listener : kieSession.getRuleRuntimeEventListeners()) {
            if (listener instanceof MockIncrementingRuleRuntimeEventListener) {
                return (MockIncrementingRuleRuntimeEventListener) listener;
            }
        }
        throw new IllegalArgumentException("Given KieSession has attached no listeners of type: " + MockIncrementingRuleRuntimeEventListener.class.getName());
    }
}
