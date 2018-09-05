/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.maven.plugin;

import java.io.File;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.maven.plugin.BuildMojoIntegrationTest;

public class BuildPMMLTest extends KieMavenPluginBaseIntegrationTest {

    private static final String PROJECT_NAME = "kjar-6-with-pmml";

    private static final String GAV_GROUP_ID = "org.kie";
    private static final String GAV_ARTIFACT_ID = "kie-maven-plugin-test-kjar-6";
    private static final String GAV_VERSION = "1.0.0.Final";

    private static final String KIE_SESSION_NAME = "PMML.session";
    private static final String KIE_PACKAGE_WITH_PMML = "org.kie.scorecards.example.SampleScore";

    private static final String PMML_FILE_NAME = "PMMLResources/simple-pmml.pmml";
    private static final String EXAMPLE_PMML_CLASS = "org/kie/scorecards/example/SampleScore/OverallScore.class";

    public BuildPMMLTest(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
        super(builder);
    }

    @Test
    public void testCleanInstallWithPMML() throws Exception {
        final File basedir = resources.getBasedir(PROJECT_NAME);
        final MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("clean",
                         "install");
        result.assertErrorFreeLog();
    }

    @Test
    public void testUseBuildKjarWithPMML() throws Exception {
        final File basedir = resources.getBasedir(PROJECT_NAME);
        final MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("clean",
                         "install");
        result.assertErrorFreeLog();

        final KieServices kieServices = KieServices.Factory.get();
        final ReleaseId releaseId = kieServices.newReleaseId(GAV_GROUP_ID, GAV_ARTIFACT_ID, GAV_VERSION);
        final KieContainer kieContainer = kieServices.newKieContainer(releaseId);
        Assertions.assertThat(kieContainer).isNotNull();
        final KieSession kieSession = kieContainer.newKieSession(KIE_SESSION_NAME);
        Assertions.assertThat(kieSession).isNotNull();
        final KieBase kieBase = kieSession.getKieBase();
        Assertions.assertThat(kieBase).isNotNull();
        final KiePackage kiePackageWithPMML = kieBase.getKiePackage(KIE_PACKAGE_WITH_PMML);
        Assertions.assertThat(kiePackageWithPMML).isNotNull();
        Assertions.assertThat(kiePackageWithPMML.getRules()).isNotEmpty();
        kieSession.dispose();
    }

    @Test
    public void testContentKjarWithPMML() throws Exception {
        final File basedir = resources.getBasedir(PROJECT_NAME);
        final MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("clean",
                         "install");
        result.assertErrorFreeLog();

        final File kjarFile = new File(basedir, "target/" + GAV_ARTIFACT_ID + "-" + GAV_VERSION + ".jar");
        Assertions.assertThat(kjarFile.exists()).isTrue();

        final JarFile jarFile = new JarFile(kjarFile);
        final Set<String> jarContent = new HashSet<>();
        final Enumeration<JarEntry> kjarEntries = jarFile.entries();
        while (kjarEntries.hasMoreElements()) {
            String entryName = kjarEntries.nextElement().getName();
            jarContent.add(entryName);
        }

        Assertions.assertThat(jarContent).isNotEmpty();
        Assertions.assertThat(jarContent).contains(PMML_FILE_NAME);
        Assertions.assertThat(jarContent).contains(EXAMPLE_PMML_CLASS);
    }
}
