/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.integration.testcoverage.instrumentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.drools.core.impl.InternalKieContainer;
import org.drools.modelcompiler.CanonicalKieModule;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.integration.testcoverage.instrumentation.model.Dog;
import org.kie.integration.testcoverage.instrumentation.model.Person;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests using a KJAR built previously by kie-maven-plugin with instrumentation enabled.
 */
public class InstrumentationTest {

    private static final KieServices KIE_SERVICES = KieServices.get();
    private static final ReleaseId RELEASE_ID = KIE_SERVICES.newReleaseId("org.drools.testcoverage", "kjar-with-instrumentation", TestUtil.getProjectVersion());

    private static KieContainer kieContainer;
    private KieSession kieSession;
    private List<String> results;

    @BeforeClass
    public static void loadKieContainer() {
        kieContainer = KIE_SERVICES.newKieContainer(RELEASE_ID);
    }

    @Before
    public void prepareSession() {
        kieSession = kieContainer.newKieSession();
        results = new ArrayList<>();
        kieSession.setGlobal("results", results);
    }

    @After
    public void disposeSession() {
        if (kieSession != null) {
            kieSession.dispose();
        }
        results = null;
    }

    @Test
    public void testLoadingKJarAndFiringRules() {
        final String dogName = "Azor";
        final Person person = new Person("Bruno", 17);
        person.addPet(new Dog(dogName, 2));

        kieSession.insert(person);
        kieSession.fireAllRules();
        assertThat(results).containsExactlyInAnyOrder(dogName);
    }

    @Test
    public void testOOPathReactivityWithInstrumentedModel() {
        final Person person = new Person("Bruno", 17);

        FactHandle fh = kieSession.insert(person);
        kieSession.fireAllRules();
        assertThat(results).isEmpty();

        person.setAge(18);
        kieSession.update(fh, person);
        kieSession.fireAllRules();
        assertThat(results).containsExactlyInAnyOrder("Lassie", "The Cat");
    }

    @Test
    public void testMetaInfoExists() {
        CanonicalKieModule kieModule = (CanonicalKieModule)((InternalKieContainer)kieContainer).getMainKieModule();
        Collection<String> fileNames = kieModule.getFileNames();

        assertThat(fileNames).contains("META-INF/kmodule.info");
        assertThat(fileNames).contains("META-INF/instrumentationKBase/kbase.cache");
    }
}
