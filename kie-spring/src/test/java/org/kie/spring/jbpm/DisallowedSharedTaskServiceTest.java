/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.kie.spring.jbpm;

import org.junit.Test;
import org.kie.api.runtime.manager.RuntimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class DisallowedSharedTaskServiceTest extends AbstractJbpmSpringTest {

    private static final Logger LOG = LoggerFactory.getLogger(DisallowedSharedTaskServiceTest.class);


    @Test(expected = BeanCreationException.class)
    public void testDisallowedSharedTaskServiceOnPerProcessInstance() {
        String contextPath = SHARED_TASKSERVICE_PER_PROCESS_INSTANCE_PATH;

        LOG.info("Creating spring context - " + contextPath);
        context = new ClassPathXmlApplicationContext(contextPath);

        context.getBean("runtimeManager", RuntimeManager.class);
    }

    @Test(expected = BeanCreationException.class)
    public void testDisallowedSharedTaskServiceOnPerRequest() {
        String contextPath = SHARED_TASKSERVICE_PER_REQUEST_PATH;

        LOG.info("Creating spring context - " + contextPath);
        context = new ClassPathXmlApplicationContext(contextPath);

        context.getBean("runtimeManager", RuntimeManager.class);
    }

    @Test
    public void testDisallowedSharedTaskServiceOnSingleton() {
        String contextPath = SHARED_TASKSERVICE_JTA_EMF_SINGLETON_PATH;

        LOG.info("Creating spring context - " + contextPath);
        context = new ClassPathXmlApplicationContext(contextPath);

        context.getBean("runtimeManager", RuntimeManager.class);
    }
}
