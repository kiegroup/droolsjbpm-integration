/*
 * Copyright 2015 JBoss by Red Hat
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
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

@Ignore("The test takes insane amount of time (minutes) to complete, because the serialization takes into account" +
        "also huge amount of DRLs coming from drools-pmml. Will be investigated and fixed by psiroky.")
public class SerializeMojoIntegrationTest extends KieMavenPluginBaseIntegrationTest {

    public SerializeMojoIntegrationTest(MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
        super(builder);
    }

    @Test
    public void testCleanInstallWithSerialize() throws Exception {
        File basedir = resources.getBasedir("kjar-1-with-serialize");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("clean", "install");
        result.assertErrorFreeLog();
    }

}
