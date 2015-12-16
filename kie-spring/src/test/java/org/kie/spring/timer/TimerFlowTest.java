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

package org.kie.spring.timer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class TimerFlowTest {

    private static final Logger log = LoggerFactory.getLogger(TimerFlowTest.class);
    private static final String TMPDIR = System.getProperty("java.io.tmpdir");

    private ClassPathXmlApplicationContext ctx;

    @Before
    public void createSpringContext() {
        try {
            log.debug("creating spring context");
            PropertyPlaceholderConfigurer configurer = new PropertyPlaceholderConfigurer();
            Properties properties = new Properties();
            properties.setProperty("temp.dir",
                    TMPDIR);
            configurer.setProperties(properties);
            ctx = new ClassPathXmlApplicationContext();
            ctx.addBeanFactoryPostProcessor(configurer);
            ctx.setConfigLocation("org/kie/spring/timer/conf/spring-conf.xml");
            ctx.refresh();
        } catch (Exception e) {
            log.error("can't create spring context",
                    e);
            throw new RuntimeException(e);
        }
    }

    @Test
    @Ignore // I don't know how to fix the test-resources org/kie/spring/timer/conf/spring-conf.xml file
    public void doTest() throws Exception {

        MyDroolsBean myDroolsBean = (MyDroolsBean) ctx.getBean("myDroolsBean");

        assertEquals(0, myDroolsBean.getTimerTriggerCount());

        myDroolsBean.initStartDisposeAndLoadSession();

        int n = myDroolsBean.getTimerTriggerCount();
        assertTrue(n > 0);

        for( int i = 0; i < 2; ++i ) { 
            // wait 2 more times for the timer to fire
            MyDroolsBean.waitForOtherThread();
        }
        
        myDroolsBean.endTheProcess();
        assertTrue(myDroolsBean.getTimerTriggerCount() > n);
    }
}
