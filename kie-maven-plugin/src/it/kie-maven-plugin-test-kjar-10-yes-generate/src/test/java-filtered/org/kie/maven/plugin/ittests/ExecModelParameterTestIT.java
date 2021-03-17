/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.maven.plugin.ittests;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.drools.compiler.kie.builder.impl.KieContainerImpl;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class ExecModelParameterTestIT {

    private static final String GAV_ARTIFACT_ID = "kie-maven-plugin-test-kjar-10-yes-generate";
    private static final String GAV_VERSION = "${org.kie.version}";
    private final static String KBASE_NAME = "SimpleKBase";
    private static final String CANONICAL_KIE_MODULE = "org.drools.modelcompiler.CanonicalKieModule";

    @Test
    public void testWithoutDroolsModelCompilerOnClassPathDoNotRunExecModel() throws Exception {
        KieModule kieModule = fireRule();
        assertNotNull(kieModule);
        assertFalse(kieModule.getClass().getCanonicalName().equals(CANONICAL_KIE_MODULE));
    }

    private KieModule fireRule() throws Exception {
        final URL targetLocation = ExecModelParameterTestIT.class.getProtectionDomain().getCodeSource().getLocation();
        final File basedir = new File(targetLocation.getFile().replace("/test-classes/", ""));
        final File kjarFile = new File(basedir, GAV_ARTIFACT_ID + "-" + GAV_VERSION + ".jar");
        Assertions.assertThat(kjarFile).exists();
        Set<URL> urls = new HashSet<>();
        urls.add(kjarFile.toURI().toURL());
        URLClassLoader projectClassLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]), getClass().getClassLoader());

        final KieServices kieServices = KieServices.get();
        final KieContainer kieContainer =  kieServices.getKieClasspathContainer(projectClassLoader);
        final KieBase kieBase = kieContainer.getKieBase(KBASE_NAME);
        KieSession kSession = null;
        try {

            kSession = kieBase.newKieSession();

            kSession.insert("Hello");
            int rulesFired = kSession.fireAllRules();
            kSession.dispose();

            assertEquals(1, rulesFired);
        } finally {
            kSession.dispose();
        }

        return ((KieContainerImpl) kieContainer).getKieModuleForKBase(KBASE_NAME);
    }
}