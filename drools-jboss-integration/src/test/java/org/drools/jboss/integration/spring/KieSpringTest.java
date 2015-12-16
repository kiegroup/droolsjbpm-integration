/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.drools.jboss.integration.spring;

import java.io.File;
import java.net.URL;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.kie.api.KieBase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Basic test of kie-spring in JBoss AS container.
 */
@Ignore @RunWith(Arquillian.class)
public class KieSpringTest {

    private static final String APL_CONTEXT = "org/drools/jboss/integration/spring/springTestContext.xml";

    @Deployment
    public static WebArchive createDeployment() {
        File[] libs = Maven.resolver()
                           .loadPomFromFile("pom.xml").resolve("org.kie:kie-spring",
                                                               "com.google.protobuf:protobuf-java")
                           .withTransitivity().asFile();

        WebArchive drools = ShrinkWrap.create(WebArchive.class)
                                      .addAsLibraries(libs)
                                      .addAsResource("spring/sample.drl")
                                      .addAsResource(APL_CONTEXT)
                                      .addAsResource("META-INF/maven/pom.properties")
                                      .addAsWebInfResource(EmptyAsset.INSTANCE, ArchivePaths.create("beans.xml"));

        System.out.println(drools.toString(true));
        return drools;
    }

    /**
     * Tests injection of a KieBase defined in Spring application context.
     */
    @Test
    public void testDRL() {
        KieBase kbase = getKieBase("kbaseDRL");

        assertNotNull("KieBase should not be null", kbase);
        assertEquals("Unexpected number of packages in kbase",
                     1, kbase.getKiePackages().size());
    }

    /**
     * Retrieves KieBase bean from spring application context.
     *
     * @param beanName KieBase bean ID in spring XML configuration
     */
    private KieBase getKieBase(String beanName) {
        ApplicationContext ctx = getApplicationContext(KieSpringTest.class.getResource("/" + APL_CONTEXT));
        return (KieBase) ctx.getBean(beanName);
    }

    private ApplicationContext getApplicationContext(URL contextXmlPath) {
        return new ClassPathXmlApplicationContext(contextXmlPath.toExternalForm());
    }
}
