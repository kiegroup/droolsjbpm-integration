/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import org.junit.Test;

public class AdditionalPropertiesIntegrationTest extends KieMavenPluginBaseIntegrationTest {

    public AdditionalPropertiesIntegrationTest(MavenRuntime.MavenRuntimeBuilder builder) {
        super(builder);
    }

    @Test
    public void testAdditionalPropertiesCorrectlySet() throws Exception {
        MavenExecutionResult result = buildKJarProject("kjar-3-properties-only",
                                                       "clean",
                                                       "install",
                                                       "-X");
        // additional properties are logged during debug (-X) build
        // following string is created directly inside the KIE Maven plugin execution (the property names and values
        // are logged multiple by maven itself as well, so we should check directly against that string)
        result.assertLogText("Additional system properties: {drools.dialect.java.compiler.lnglevel=1.6, my.property=some-value}");
    }
}
