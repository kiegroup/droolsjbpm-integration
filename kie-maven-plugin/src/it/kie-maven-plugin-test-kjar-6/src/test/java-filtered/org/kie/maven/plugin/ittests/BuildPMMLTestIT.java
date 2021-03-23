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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.kie.api.pmml.PMMLConstants.KIE_PMML_IMPLEMENTATION;
import static org.kie.api.pmml.PMMLConstants.LEGACY;

public class BuildPMMLTestIT {

    private static final String GAV_GROUP_ID = "org.kie";
    private static final String GAV_ARTIFACT_ID = "kie-maven-plugin-test-kjar-6";
    private static final String GAV_VERSION = "${org.kie.version}";

    private static final String KIE_PACKAGE_WITH_PMML = "PMMLResources.SampleScore";

    private static final String PMML_FILE_NAME = "PMMLResources/simple-pmml.pmml";
    private static final String EXAMPLE_PMML_CLASS = "PMMLResources/SampleScore/OverallScore.class";


    @BeforeClass
    public static void setup() {
        System.setProperty(KIE_PMML_IMPLEMENTATION.getName(), LEGACY.getName());
    }

    @AfterClass
    public static void cleanup() {
        System.clearProperty(KIE_PMML_IMPLEMENTATION.getName());
    }

    @Test
    public void testBuildKjarWithPMML() throws Exception {
        final URL targetLocation = BuildPMMLTestIT.class.getProtectionDomain().getCodeSource().getLocation();
        final File kjarFile = ITTestsUtils.getKjarFile(targetLocation, GAV_ARTIFACT_ID, GAV_VERSION);
        final JarFile jarFile = new JarFile(kjarFile);
        final Set<String> jarContent = new HashSet<>();
        final Enumeration<JarEntry> kjarEntries = jarFile.entries();
        while (kjarEntries.hasMoreElements()) {
            final String entryName = kjarEntries.nextElement().getName();
            jarContent.add(entryName);
        }

        Assertions.assertThat(jarContent).isNotEmpty();
        Assertions.assertThat(jarContent).contains(PMML_FILE_NAME);
        Assertions.assertThat(jarContent).contains(EXAMPLE_PMML_CLASS);
    }
}