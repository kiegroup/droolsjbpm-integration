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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import io.takari.maven.testing.TestResources;
import io.takari.maven.testing.executor.MavenExecutionResult;
import io.takari.maven.testing.executor.MavenRuntime;
import io.takari.maven.testing.executor.MavenVersions;
import io.takari.maven.testing.executor.junit.MavenJUnitTestRunner;
import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(MavenJUnitTestRunner.class)
@MavenVersions({"3.3.9", "3.5.0"})
abstract public class KieMavenPluginBaseIntegrationTest {

    @Rule
    public final TestResources resources = new TestResources();

    public final MavenRuntime mavenRuntime;

    public KieMavenPluginBaseIntegrationTest(MavenRuntime.MavenRuntimeBuilder builder) {
        this.mavenRuntime = builder
                .forkedBuilder()
                // To enable logging using slf4j-simple on the internal classes of the plug-in:
                //.withCliOptions("-Dorg.slf4j.simpleLogger.defaultLogLevel=debug")
                .withEnvironment(System.getenv())
                .build();
    }

    protected void prepareTakariPom(String projectName) throws Exception {
        File basedir = getBasedir(projectName);
        File pomTakari = new File(basedir, "pom-takari.xml");
        Files.move(pomTakari.toPath(), new File(basedir, "pom.xml").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    protected void restoreKiePom(String projectName) throws Exception {
        File basedir = getBasedir(projectName);
        File pomKie = new File(basedir, "pom-kie.xml");
        Files.move(pomKie.toPath(), new File(basedir, "pom.xml").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    protected MavenExecutionResult buildKJarProject(String kjarName, String... mavenGoals) throws Exception {
        File basedir = resources.getBasedir(kjarName);
        MavenExecutionResult result = mavenRuntime
                .forProject(basedir)
                .execute(mavenGoals);
        result.assertErrorFreeLog();
        return result;
    }

    private File getBasedir(String projectName) throws Exception {
        return resources.getBasedir(projectName);
    }
}
