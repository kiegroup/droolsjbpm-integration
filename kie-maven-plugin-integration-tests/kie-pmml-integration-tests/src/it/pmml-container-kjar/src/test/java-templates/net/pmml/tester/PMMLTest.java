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

package net.pmml.tester;

import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kie.builder.impl.KieModuleKieProject;
import org.drools.compiler.kie.builder.impl.ZipKieModule;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.definition.KiePackage;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class PMMLTest {

    public static final String GROUP_ID = "org.drools";
    public static final String ARTIFACT_ID = "kie-pmml-integration-tests";
    public static final String VERSION = "@kjar.version@";

    private static final String KIE_SESSION_NAME = "PMML.session";
    private static final String KIE_PACKAGE_WITH_PMML = "PMMLResources.SampleScore";

    private static final String PMML_FILE_NAME = "PMMLResources/simple-pmml.pmml";
    private static final String EXAMPLE_PMML_CLASS = "PMMLResources/SampleScore/OverallScore.class";
    private static KieContainer KIE_CONTAINER;


    @BeforeClass
    public static void setup() {
        final KieServices kieServices = KieServices.get();
        final ReleaseId releaseId = new ReleaseIdImpl(GROUP_ID, ARTIFACT_ID, VERSION);
        KIE_CONTAINER = kieServices.newKieContainer(releaseId);
        Assertions.assertThat(KIE_CONTAINER).isNotNull();
    }

    @Test
    public void testUseBuildKjarWithPMML() throws Exception {
        final KieSession kieSession = KIE_CONTAINER.newKieSession(KIE_SESSION_NAME);
        Assertions.assertThat(kieSession).isNotNull();
        final KieBase kieBase = kieSession.getKieBase();
        Assertions.assertThat(kieBase).isNotNull();
        final KiePackage kiePackageWithPMML = kieBase.getKiePackage(KIE_PACKAGE_WITH_PMML);
        Assertions.assertThat(kiePackageWithPMML).isNotNull();
        Assertions.assertThat(kiePackageWithPMML.getRules()).isNotEmpty();
        kieSession.dispose();
    }

    @Test
    public void testContentKjarWithPMML() {
        KieModuleKieProject kieModuleKieProject = (KieModuleKieProject) ((KieContainerImpl) KIE_CONTAINER).getKieProject();
        ZipKieModule zipKieModule = (ZipKieModule)kieModuleKieProject.getInternalKieModule();
        final Collection<String> fileNames = zipKieModule.getFileNames();
        Assertions.assertThat(fileNames).contains(PMML_FILE_NAME);
        Assertions.assertThat(fileNames).contains(EXAMPLE_PMML_CLASS);
    }
}
