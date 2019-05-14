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

import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.junit.Test;
import org.kie.api.KieServices;

import static org.junit.Assert.*;

public class MultiModuleTest extends KieMavenPluginBaseIntegrationTest {

    private final static String GROUP_ID = "org.kie";
    private final static String ARTIFACT_ID = "kie-maven-plugin-test-kjar-8";
    private final static String VERSION = "1.0.0.Final";
    private final static String KJAR_NAME = "kjar-8-multimodule";

    public MultiModuleTest(MavenRuntime.MavenRuntimeBuilder builder) {
        super(builder);
    }

    @Test
    public void testMultiModule() throws Exception {
        MavenExecutionResult mavenExecutionResult = buildKJarProject(KJAR_NAME, getMavenGoalsAndOptions());
        try {
            KieContainerImpl kContainer =
                    (KieContainerImpl) KieServices.Factory.get().getKieClasspathContainer(Thread.currentThread().getContextClassLoader());

            for (String b : kContainer.getKieBaseNames()) {
                System.out.println("BASE: " + b);
//                KieBase kieBase = kContainer.getKieBase(b);
//                for (KiePackage kiePackage : kieBase.getKiePackages()) {
//                    System.out.println("    PACKAGE: " + kiePackage.getName());
//
//                    for (FactType factType : kiePackage.getFactTypes()) {
//                        System.out.println("        FACTTYPE: " + factType.getName());
//                    }
//                }
            }

            assertTrue(true);
        } finally {
        }
    }

    private String[] getMavenGoalsAndOptions() throws IOException {
        return new String[]{"clean", "install", "-Ddrools.version=" + TestUtil.getProjectVersion(), "-DgenerateModel=YES"};
    }
}

