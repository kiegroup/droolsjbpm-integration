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

import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static org.junit.Assert.assertNotNull;

public class KieSpringImportReleaseIdTest extends AbstractKieSpringDynamicModuleTest {

    static ApplicationContext context = null;
    protected final int FIRST_VALUE = 5;

    @Test
    public void testSpringKieImportReleaseId() throws Exception {

        KieServices ks = KieServices.Factory.get();

        //step 1: deploy the test module to MAVEN Repo
        createAndInstallModule( ks, FIRST_VALUE );

        //step 2: load the spring context
        createSpringContext();

        //step 3: check the basic spring objects
        lookupReleaseId();
        lookupNamedKieBase();

        //step 4: cleanup. Remove the module
        ks.getRepository().removeKieModule(releaseId);
    }

    protected void lookupNamedKieBase() throws Exception {
        KieBase kieBase = context.getBean("KBase1", KieBase.class);
        assertNotNull(kieBase);
    }

    protected void lookupReleaseId() throws Exception {
        ReleaseId releaseId = context.getBean("spring-import-releaseId", ReleaseId.class);
        assertNotNull(releaseId);
    }

    protected void createSpringContext() throws Exception {
        context = new ClassPathXmlApplicationContext("org/kie/spring/kie-import-releaseid.xml");
        assertNotNull(context);
    }

}
