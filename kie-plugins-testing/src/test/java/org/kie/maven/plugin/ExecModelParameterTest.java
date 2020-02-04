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

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static org.junit.Assert.*;

public class ExecModelParameterTest extends KieMavenPluginBaseIntegrationTest {

    private final static String GROUP_ID = "org.kie";
    private final static String ARTIFACT_ID = "kie-maven-plugin-test-kjar-11";
    private final static String VERSION = "1.0.0.Final";
    private final static String KJAR_NAME = "kjar-11-exec-model-parameter";

    public ExecModelParameterTest(MavenRuntime.MavenRuntimeBuilder builder) {
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
            buildKJarProject(KJAR_NAME,
                             new String[]{"-Dorg.kie.version=" + TestUtil.getProjectVersion()},
                             "clean", "install");
        } else {
            buildKJarProject(KJAR_NAME,
                             new String[]{"-Dorg.kie.version=" + TestUtil.getProjectVersion(), "-DgenerateModel=NO"},
                             "clean", "install");
        }


        final KieServices kieServices = KieServices.get();
        final ReleaseId releaseId = kieServices.newReleaseId(GROUP_ID, ARTIFACT_ID, VERSION);
        final KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        KieSession kSession = null;
        try {

            kSession = kieContainer.newKieSession("SimpleKBase.session");

            kSession.insert("Hello");
            int rulesFired = kSession.fireAllRules();
            kSession.dispose();

            assertEquals(1, rulesFired);


        } finally {
            kSession.dispose();
        }
    }
}

