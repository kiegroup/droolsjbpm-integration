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

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.0.5", "3.2.3"})
abstract public class KieMavenPluginBaseIntegrationTest {

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public KieMavenPluginBaseIntegrationTest(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
        this.mavenRuntime = builder
                .forkedBuilder()
                .withCliOptions("-X")
                // To enable logging using slf4j-simple on the internal classes of the plug-in:
                //  .withCliOptions("-Dorg.slf4j.simpleLogger.defaultLogLevel=debug")
                .build();
    }

}
