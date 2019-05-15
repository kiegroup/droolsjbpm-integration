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

import java.util.Collection;
import java.util.List;

import io.takari.maven.testing.executor.MavenRuntime;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.drools.compiler.kproject.ReleaseIdImpl;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.type.FactType;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.*;

public class MultiModuleTest extends KieMavenPluginBaseIntegrationTest {

    private final static String GROUP_ID = "org.kie";
    private final static String ARTIFACT_ID = "kie-maven-plugin-test-kjar-8-modA";
    private final static String VERSION = "1.0.0.Final";
    private final static String KJAR_NAME = "kjar-8-multimodule";

    public MultiModuleTest(MavenRuntime.MavenRuntimeBuilder builder) {
        super(builder);
    }

    @Test
    public void testMultiModule() throws Exception {
        String droolsVersionParameter = String.format("-Ddrools.version=%s", TestUtil.getProjectVersion());
        buildKJarProject(KJAR_NAME, new String[]{"clean", "install", droolsVersionParameter, "-DgenerateModel=YES"});
        KieContainerImpl kContainer =
                (KieContainerImpl) KieServices.Factory.get().newKieContainer(new ReleaseIdImpl(GROUP_ID, ARTIFACT_ID, VERSION));

        Collection<String> kieBaseNames = kContainer.getKieBaseNames();
        assertThat(kieBaseNames).hasSameElementsAs(asList("modC", "modB", "modA"));

        List<KiePackage> kiePackages = kieBaseNames.stream()
                .map(kContainer::getKieBase)
                .flatMap(kb -> kb.getKiePackages().stream())
                .collect(toList());

        assertThat(kiePackages.stream()
                           .map(KiePackage::getName)
                           .collect(toList()))
                .hasSameElementsAs(asList("olijohns.modC", "olijohns.modB", "olijohns.modA"));

        List<FactType> factTypes = kiePackages.stream()
                .flatMap(kb -> kb.getFactTypes().stream())
                .collect(toList());

        assertThat(factTypes.stream()
                           .map(FactType::getName)
                           .collect(toList()))
                .hasSameElementsAs(asList("olijohns.modC.FactC", "olijohns.modB.FactB", "olijohns.modA.FactA"));
    }
}

