/*
 * Copyright 2016 JBoss by Red Hat.
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
package org.kie.server.integrationtests.shared;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.appformer.maven.integration.MavenRepository;
import org.appformer.maven.integration.embedder.MavenProjectLoader;
import org.appformer.maven.integration.embedder.MavenSettings;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.Assert;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.scanner.KieMavenRepository;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.config.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerDeployer {

    protected static Logger logger = LoggerFactory.getLogger(KieServerDeployer.class);
    private static KieMavenRepository repository;

    /*
     Indicates whether the testing common parent maven project has been deployed in this test run. Most of the testing
     kjars depend on that parent, but it is not necessary to deploy it multiple times. This flag is set the first time
     the parent project is deployed and the subsequent requests to deploy are just ignored, because the parent can
     already be found in the maven repo.
     */
    private static boolean commonParentDeployed = false;

    public static void buildAndDeployMavenProjectFromResource(String resourcePath) {
        buildAndDeployMavenProject(KieServerDeployer.class.getResource(resourcePath).getFile());
    }

    public static void buildAndDeployMavenProject(String basedir) {
        // run the Maven build which will create the kjar. The kjar is then either installed or deployed to local and
        // remote repo
        logger.debug("Building and deploying Maven project from basedir '{}'.", basedir);
        List<String> mvnArgs;
        if (TestConfig.isLocalServer()) {
            // just install into local repository when running the local server. Deploying to remote repo will fail
            // if the repo does not exist.

            // wrapping explicitly in ArrayList as we may need to update the list later (and Arrays.toList() returns
            // just read-only list)
            mvnArgs = new ArrayList<>(Arrays.asList("mvn", "--quiet", "clean", "install"));
        } else {
            mvnArgs = new ArrayList<>(Arrays.asList("mvn", "--quiet", "-e", "clean", "deploy"));
        }

        // use custom settings.xml file, if one specified
        String kjarsBuildSettingsXml = TestConfig.getKjarsBuildSettingsXml();
        if (kjarsBuildSettingsXml != null && !kjarsBuildSettingsXml.isEmpty()) {
            mvnArgs.add("-s");
            mvnArgs.add(kjarsBuildSettingsXml);
        }

        // Execute the Maven build using installed Maven binary
        try (ProcessExecutor executor = new ProcessExecutor()) {
            String command = mvnArgs.stream().collect(Collectors.joining(" "));
            boolean executionSuccessful = executor.executeProcessCommand(command, Paths.get(basedir));

            if (!executionSuccessful) {
                throw new RuntimeException("Error while building Maven project from basedir " + basedir);
            }
        }
        logger.debug("Maven project successfully built and deployed!");
    }

    public static void buildAndDeployCommonMavenParent() {
        // deploy only once as it is not needed to do that with every request
        if (!commonParentDeployed) {
            buildAndDeployMavenProjectFromResource("/kjars-sources/common-parent");
        } else {
            logger.debug("Common parent project already deployed, nothing to do here.");
        }
    }

    public static void createAndDeployKJar(ReleaseId releaseId) {
        String drl = "package org.pkg1\n"
                + "global java.util.List list;"
                + "declare Message\n"
                + "    text : String\n"
                + "end\n"
                + "rule echo dialect \"mvel\"\n"
                + "when\n"
                + "    $m : Message()\n"
                + "then\n"
                + "    $m.text = \"echo:\" + $m.text;\n"
                + "end\n"
                + "rule X when\n"
                + "    msg : String()\n"
                + "then\n"
                + "    list.add(msg);\n"
                + "end\n";
        createAndDeployKJar(releaseId, Collections.singletonMap("src/main/resources/org/pkg1/r0.drl", drl));
    }

    /**
     * Dynamically deploy kjar with content.
     *
     * @param releaseId Release id.
     * @param files Map of file names and file content.
     */
    public static void createAndDeployKJar(ReleaseId releaseId, Map<String, String> files) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(releaseId);

        for (Entry<String, String> file : files.entrySet()) {
            kfs.write(file.getKey(), file.getValue());
        }

        byte[] pom = kfs.read("pom.xml");
        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
        Assert.assertFalse(kb.getResults().getMessages(org.kie.api.builder.Message.Level.ERROR).toString(),
                kb.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR));
        InternalKieModule kieModule = (InternalKieModule) ks.getRepository().getKieModule(releaseId);
        byte[] jar = kieModule.getBytes();

        getRepository().installArtifact(releaseId, jar, pom);

        // make sure it is not deployed in the in-memory repository
        ks.getRepository().removeKieModule(releaseId);
    }

    /**
     * Remove artifact from Kie server's local maven repository.
     *
     * @param releaseId Release id.
     */
    public static void removeLocalArtifact(ReleaseId releaseId) {
        String originalMavenSettings = System.getProperty(KieServerConstants.CFG_KIE_MVN_SETTINGS);
        String kieServerMavenSettings = TestConfig.getKieServerMavenSettings();

        System.setProperty(KieServerConstants.CFG_KIE_MVN_SETTINGS, kieServerMavenSettings);
        MavenSettings.reinitSettings();

        getRepository().removeLocalArtifact(releaseId);

        System.setProperty(KieServerConstants.CFG_KIE_MVN_SETTINGS, originalMavenSettings);
        MavenSettings.reinitSettings();
    }

    public static void cleanAllRepositories() {
        cleanDirectory(TestConfig.getKieServerRemoteRepoDir());
        cleanDirectory(TestConfig.getKieServerLocalRepoDir());
    }

    private static void cleanDirectory(String directoryPath) {
        File directory = new File(directoryPath);
        try {
            FileUtils.deleteDirectory(directory);
            directory.mkdir();
        } catch (IOException e) {
            logger.error("Cannot delete directory" + directory, e);
        }
    }

    public static MavenRepository getRepository() {
        if (repository == null) {
            // Initialize repository with minimal pom file.
            KieServices ks = KieServices.Factory.get();
            ReleaseId initReleaseId = new ReleaseId("org.kie.server.initial", "init-maven-repo", "42");
            KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(initReleaseId);
            byte[] pom = kfs.read("pom.xml");

            MavenProject minimalMavenProject = MavenProjectLoader.parseMavenPom( new ByteArrayInputStream( pom) );
            repository = KieMavenRepository.getKieMavenRepository(minimalMavenProject);
        }

        return repository;
    }
}
