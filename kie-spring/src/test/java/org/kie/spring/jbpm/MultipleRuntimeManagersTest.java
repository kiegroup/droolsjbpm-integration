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

import static org.junit.Assert.*;

import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Test verifying that multiple beans created by RuntimeManagerFactoryBean with same identifier
 * reference to same runtime manager instance.
 */
public class MultipleRuntimeManagersTest extends AbstractJbpmSpringTest {

    @Test
    public void testGettingMultipleRuntimeManagersByContextRetrieve() throws Exception {

        context = new ClassPathXmlApplicationContext("jbpm/multiple-runtime-managers/local-emf-singleton.xml");

        RuntimeManager managerOne = (RuntimeManager) context.getBean("runtimeManager");
        RuntimeManager managerTwo = (RuntimeManager) context.getBean("runtimeManagerTwo");

        assertEquals(managerOne, managerTwo);
    }
}
