/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.spring.jbpm;

import org.kie.spring.jbpm.tools.RuntimeManagerHolder;
import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.manager.RuntimeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test verifying that multiple beans created by RuntimeManagerFactoryBean with same identifier
 * reference to same runtime manager instance.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:jbpm/multiple-runtime-managers/local-emf-singleton.xml")
@DirtiesContext
public class MultipleRuntimeManagersTest extends AbstractJbpmSpringTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    @Qualifier("holderOne")
    private RuntimeManagerHolder holderOne;

    @Autowired
    @Qualifier("holderTwo")
    private RuntimeManagerHolder holderTwo;

    private static final String RUNTIME_MANAGER_ONE = "runtimeManager";
    private static final String RUNTIME_MANAGER_TWO = "runtimeManagerTwo";

    @Test
    public void testGettingMultipleRuntimeManagersByInjection() throws Exception {
        assertNotEquals(holderOne, holderTwo);
        assertEquals(holderOne.getRuntimeManager(), holderTwo.getRuntimeManager());
    }

    @Test
    public void testGettingMultipleRuntimeManagersByContextRetrieve() throws Exception {
        RuntimeManager managerOne = context.getBean(RUNTIME_MANAGER_ONE, RuntimeManager.class);
        RuntimeManager managerTwo = context.getBean(RUNTIME_MANAGER_TWO, RuntimeManager.class);

        assertEquals(managerOne, managerTwo);
    }
}
