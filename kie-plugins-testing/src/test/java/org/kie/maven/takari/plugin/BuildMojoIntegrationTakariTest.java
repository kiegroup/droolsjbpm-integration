/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
package org.kie.maven.takari.plugin;

import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.maven.plugin.BuildMojoIntegrationTest;

public class BuildMojoIntegrationTakariTest extends BuildMojoIntegrationTest {

    private String projectName = "kjar-2-all-resources";

    public BuildMojoIntegrationTakariTest(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
        super(builder);
    }

    @Before
    public void preparePom() throws Exception {
        prepareTakariPom(projectName);
    }

    @After
    public void restorePom() throws Exception {
        restoreKiePom(projectName);
    }

    @Test
    public void testCleanInstallWithAllSupportedResourceTypes() throws Exception {
        super.testCleanInstallWithAllSupportedResourceTypes();
    }
}

