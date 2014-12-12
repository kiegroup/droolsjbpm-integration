/*
 * Copyright 2012 Red Hat
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.drools.karaf.itest;

import org.h2.tools.Server;
import org.jbpm.process.instance.impl.demo.SystemOutWorkItemHandler;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.manager.*;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.karaf.options.LogLevelOption;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.support.OsgiBundleXmlApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.drools.karaf.itest.KarafIntegrationTestSupport.getKarafDistributionOption;
import static org.drools.karaf.itest.KarafIntegrationTestSupport.loadCamelFeatures;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.*;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerMethod.class)
public class KieSpringOnKarafIntegrationTest extends KieSpringIntegrationTestSupport {

    protected static final transient Logger LOG = LoggerFactory.getLogger(KieSpringOnKarafIntegrationTest.class);

    protected static final String DroolsVersion;
    static {
        Properties testProps = new Properties();
        try {
            testProps.load(KieSpringOnKarafIntegrationTest.class.getResourceAsStream("/test.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize DroolsVersion property: " + e.getMessage(), e);
        }
        DroolsVersion = testProps.getProperty("project.version");
        LOG.info("Drools Project Version : " + DroolsVersion);
    }

    @Before
    public void init() {
        applicationContext = createApplicationContext();
        assertNotNull("Should have created a valid spring context", applicationContext);
    }

    @Test
    public void testKieBase() throws Exception {
        refresh();
        KieBase kbase = (KieBase) applicationContext.getBean("drl_kiesample");
        assertNotNull(kbase);
    }

    @Test
    public void testKieSession() throws Exception {
        refresh();
        StatelessKieSession ksession = (StatelessKieSession) applicationContext.getBean("ksession9");
        assertNotNull(ksession);
    }

    @Test
    public void testKieSessionDefaultType() throws Exception {
        refresh();
        Object obj = applicationContext.getBean("ksession99");
        assertNotNull(obj);
        assertTrue(obj instanceof KieSession);
    }

    
    @Configuration
    public static Option[] configure() {
        return new Option[]{

                // Install Karaf Container
                getKarafDistributionOption(),

                // It is really nice if the container sticks around after the test so you can check the contents
                // of the data directory when things go wrong.
                keepRuntimeFolder(),
                // Don't bother with local console output as it just ends up cluttering the logs
                configureConsole().ignoreLocalConsole(),
                // Force the log level to INFO so we have more details during the test.  It defaults to WARN.
                logLevel(LogLevelOption.LogLevel.INFO),

                // Option to be used to do remote debugging
//                debugConfiguration("5005", true),

                // Load camel-test as it is required by pax-exam
                loadCamelFeatures(),

                // Load Kie-Spring
                loadDroolsKieFeatures("kie-spring")

        };

    }

    protected OsgiBundleXmlApplicationContext createApplicationContext() {
        return new OsgiBundleXmlApplicationContext(new String[]{"org/drools/karaf/itest/kie-beans.xml"});
    }
}