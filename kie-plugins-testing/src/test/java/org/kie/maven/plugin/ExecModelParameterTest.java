/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.modelcompiler.CanonicalKieModule;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static java.lang.String.format;
import static org.junit.Assert.*;
import static org.kie.maven.plugin.TestUtil.getProjectVersion;

public class ExecModelParameterTest extends KieMavenPluginBaseIntegrationTest {

    private final static String GROUP_ID = "org.kie";

    private final static String ARTIFACT_ID_WITHOUT_EXEC_MODEL = "kie-maven-plugin-test-kjar-10";
    private final static String KJAR_NAME_WITHOUT_EXEC_MODEL = "kjar-10-simple-kjar";
    private final static String KBASE_NAME_WITHOUT_EXEC_MODE = "SimpleKBase";

    private final static String ARTIFACT_ID_WITH_EXEC_MODEL = "kie-maven-plugin-test-kjar-11";
    private final static String KJAR_NAME_WITH_EXEC_MODEL = "kjar-11-simple-kjar-with-droolsmodelcompiler";
    private final static String KBASE_NAME_WITH_EXEC_MODEL = "SimpleKBase-execmodel";

    private final static String VERSION = "1.0.0.Final";

    public ExecModelParameterTest(MavenRuntime.MavenRuntimeBuilder builder) {
        super(builder);
    }

    @Test
    public void testDefaultMavenWithDroolsModelCompilerOnClassPathRunsExecModel() throws Exception {
        buildKJarProject(KJAR_NAME_WITH_EXEC_MODEL,
                         new String[]{"-Dorg.kie.version=" + getProjectVersion()},
                         "clean", "install");
        KieModule kieModule = fireRule(ARTIFACT_ID_WITH_EXEC_MODEL, KBASE_NAME_WITH_EXEC_MODEL);
        System.out.println(kieModule.getClass().getCanonicalName());
        assertTrue(kieModule instanceof CanonicalKieModule);
    }

    @Test
    public void testYesMavenWithDroolsModelCompilerOnClassPathRunsExecModel() throws Exception {
        buildKJarProject(KJAR_NAME_WITH_EXEC_MODEL,
                         new String[]{format("-Dorg.kie.version=%s", getProjectVersion()), "-DgenerateModel=YES"},
                         "clean", "install");
        KieModule kieModule = fireRule(ARTIFACT_ID_WITH_EXEC_MODEL, KBASE_NAME_WITH_EXEC_MODEL);
        assertTrue(kieModule instanceof CanonicalKieModule);
    }

    @Test
    public void testDefaultMavenWithoutDroolsModelCompilerOnClassPathDoNotRunExecModel() throws Exception {
        buildKJarProject(KJAR_NAME_WITHOUT_EXEC_MODEL,
                         new String[]{"-Dorg.kie.version=" + getProjectVersion()},
                         "clean", "install");
        KieModule kieModule = fireRule(ARTIFACT_ID_WITHOUT_EXEC_MODEL, KBASE_NAME_WITHOUT_EXEC_MODE);
        assertFalse(kieModule instanceof CanonicalKieModule);
    }

    @Test
    public void testYesMavenWithoutDroolsModelCompilerOnClassPathDoNotRunExecModel() throws Exception {
        buildKJarProject(KJAR_NAME_WITHOUT_EXEC_MODEL,
                         new String[]{format("-Dorg.kie.version=%s", getProjectVersion()), "-DgenerateModel=YES"},
                         "clean", "install");
        KieModule kieModule = fireRule(ARTIFACT_ID_WITHOUT_EXEC_MODEL, KBASE_NAME_WITHOUT_EXEC_MODE);
        assertFalse(kieModule instanceof CanonicalKieModule);
    }

    @Test
    public void testNoMavenWithoutDroolsModelCompilerOnClassPathDoNotRunExecModel() throws Exception {
        buildKJarProject(KJAR_NAME_WITHOUT_EXEC_MODEL,
                         new String[]{format("-Dorg.kie.version=%s", getProjectVersion()), "-DgenerateModel=NO"},
                         "clean", "install");
        KieModule kieModule = fireRule(ARTIFACT_ID_WITHOUT_EXEC_MODEL, KBASE_NAME_WITHOUT_EXEC_MODE);
        assertFalse(kieModule instanceof CanonicalKieModule);
    }

    @Test
    public void testNoMavenWithDroolsModelCompilerOnClassPathDoNotRunExecModel() throws Exception {
        buildKJarProject(KJAR_NAME_WITH_EXEC_MODEL,
                         new String[]{format("-Dorg.kie.version=%s", getProjectVersion()), "-DgenerateModel=NO"},
                         "clean", "install");
        KieModule kieModule = fireRule(ARTIFACT_ID_WITH_EXEC_MODEL, KBASE_NAME_WITH_EXEC_MODEL);
        assertFalse(kieModule instanceof CanonicalKieModule);
    }

    private KieModule fireRule(String artifactId, String kBaseName) {
        final KieServices kieServices = KieServices.get();
        final ReleaseId releaseId = kieServices.newReleaseId(GROUP_ID, artifactId, VERSION);
        final KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        KieSession kSession = null;
        try {

            kSession = kieContainer.newKieSession(kBaseName + ".session");

            kSession.insert("Hello");
            int rulesFired = kSession.fireAllRules();
            kSession.dispose();

            assertEquals(1, rulesFired);
        } finally {
            kSession.dispose();
        }

        return ((KieContainerImpl) kieContainer).getKieModuleForKBase(kBaseName);
    }
}

