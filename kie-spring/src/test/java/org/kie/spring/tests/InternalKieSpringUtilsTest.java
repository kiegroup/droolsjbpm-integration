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

import org.drools.compiler.kproject.ReleaseIdImpl;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.InternalKieSpringUtils;
import org.springframework.context.ApplicationContext;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InternalKieSpringUtilsTest {

    static ApplicationContext context = null;

    @BeforeClass
    public static void setup() {
        ReleaseId releaseId = new ReleaseIdImpl("sample-group","test-spring","0001");
        context = InternalKieSpringUtils.getSpringContext(releaseId,
                InternalKieSpringUtilsTest.class.getResource("/org/kie/spring/beans-internal.xml"),
                new File(InternalKieSpringUtilsTest.class.getResource("/").getFile()));
    }

    @Test
    public void testContextNotNull() throws Exception {
        assertNotNull(context);
    }

    @Test
    public void testKBase() throws Exception {
        Object object = context.getBean("drl_kiesample3");
        assertNotNull(object);
        assertTrue(object instanceof KieBase);
    }

    @Test
    public void testKieSession() throws Exception {
        Object object = context.getBean("ksession2");
        assertNotNull(object);
        assertTrue(object instanceof KieSession);
    }

    @Test
    public void testKieStatelessSession() throws Exception {
        Object object = context.getBean("ksession1");
        assertNotNull(object);
        assertTrue(object instanceof StatelessKieSession);
    }

    @AfterClass
    public static void tearDown() {

    }

}