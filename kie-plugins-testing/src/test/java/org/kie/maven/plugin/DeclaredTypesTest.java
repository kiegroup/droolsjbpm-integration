/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.maven.plugin;

import java.io.IOException;
import java.lang.reflect.Constructor;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static org.junit.Assert.*;

public class DeclaredTypesTest extends KieMavenPluginBaseIntegrationTest {

    private final static String GROUP_ID = "org.kie";
    private final static String ARTIFACT_ID = "kie-maven-plugin-test-kjar-9";
    private final static String VERSION = "1.0.0.Final";
    private final static String KJAR_NAME = "kjar-9-declared-types";

    public DeclaredTypesTest(MavenRuntime.MavenRuntimeBuilder builder) {
        super(builder);
    }

    @Test
    public void testDeclaredTypeWithJavaField() throws Exception {
        testDeclaredTypeWithJavaField(false);
    }

    @Test
    public void testDeclaredTypeWithJavaFieldExecutableModel() throws Exception {
        testDeclaredTypeWithJavaField(true);
    }

    private void testDeclaredTypeWithJavaField(final boolean useExecutableModel) throws Exception {
        if (useExecutableModel) {
            buildKJarProject(KJAR_NAME, getMavenGoalsAndOptions(true));
        } else {
            buildKJarProject(KJAR_NAME, getMavenGoalsAndOptions(false));
        }
        final KieServices kieServices = KieServices.get();
        final ReleaseId releaseId = kieServices.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        final KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        KieSession kSession = null;
        try {

            kSession = kieContainer.newKieSession("DeclaredTypeKBase.session");

            ClassLoader classLoader = kieContainer.getClassLoader();
            Class<?> aClass = Class.forName("org.declaredtype.FactA", true, classLoader);
            Constructor<?> constructor = aClass.getConstructor(String.class);
            Object lucaFactA = constructor.newInstance("Luca");

            kSession.insert(lucaFactA);
            int rulesFired = kSession.fireAllRules();
            kSession.dispose();

            assertEquals(1, rulesFired);
        } finally {
            kSession.dispose();
        }
    }

    private String[] getMavenGoalsAndOptions(final boolean useExecutableModel) throws IOException {
        if (useExecutableModel) {
            return new String[]{"clean", "install", "-Ddrools.version=" + TestUtil.getProjectVersion(), "-DgenerateModel=YES"};
        } else {
            return new String[]{"clean", "install", "-Ddrools.version=" + TestUtil.getProjectVersion()};
        }
    }
}

