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
package org.kie.maven.plugin;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.3.9", "3.5.0"})
public class BuildMojoIntegrationErroCaseTest {

    @Rule
    public final TestResources resources = new TestResources();

    private final MavenRuntime mavenRuntime;

    public BuildMojoIntegrationErroCaseTest(final MavenRuntime.MavenRuntimeBuilder builder) throws Exception {
        this.mavenRuntime = builder.build();
    }

    @Test
    public void testCheckErrorMessage() throws Exception {
        File basedir = resources.getBasedir("kjar-2-errors-resources");
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute("clean",
                         "org.kie:kie-maven-plugin:build");

        //not sure why assertLogText doesn't work, had to access private field
        Field f = result.getClass().getDeclaredField("log");
        f.setAccessible(true);

        boolean found = false;
        List<String> internalMavenOutput = (List<String>) f.get(result);
        for (String log : internalMavenOutput) {
            if (log.contains("path=some/pkg/simple-rules.drl")) {
                found = true;
                break;
            }
        }

        assertTrue("Package is not present in the path.", found);
    }
}

