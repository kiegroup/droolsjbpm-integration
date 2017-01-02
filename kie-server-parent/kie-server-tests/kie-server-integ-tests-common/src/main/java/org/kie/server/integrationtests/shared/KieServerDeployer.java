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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.cli.MavenCli;
import org.apache.maven.project.MavenProject;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.Assert;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.scanner.MavenRepository;
import org.kie.scanner.embedder.MavenProjectLoader;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.config.TestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerDeployer {

    protected static Logger logger = LoggerFactory.getLogger(KieServerDeployer.class);
    private static MavenRepository repository;

    /*
     Indicates whether the testing common parent maven project has been deployed in this test run. Most of the testing
     kjars depend on that parent, but it is not necessary to deploy it multiple times. This flag is set the first time
     the parent project is deployed and the subsequent requests to deploy are just ignored, because the parent can
     already be found in the maven repo.
     */
    private static boolean commonParentDeployed = false;

    public static void buildAndDeployMavenProject(String basedir) {
        // need to backup (and later restore) the current class loader, because the Maven/Plexus does some classloader
        // magic which then results in CNFE in RestEasy client
        // run the Maven build which will create the kjar. The kjar is then either installed or deployed to local and
        // remote repo
        logger.debug("Building and deploying Maven project from basedir '{}'.", basedir);
        ClassLoader classLoaderBak = Thread.currentThread().getContextClassLoader();
        MavenCli cli = new MavenCli();
        List<String> mvnArgs;
        if (TestConfig.isLocalServer()) {
            // just install into local repository when running the local server. Deploying to remote repo will fail
            // if the repo does not exist.

            // wrapping explicitly in ArrayList as we may need to update the list later (and Arrays.toList() returns
            // just read-only list)
            mvnArgs = new ArrayList<String>(Arrays.asList("-B", "clean", "install"));
        } else {
            mvnArgs = new ArrayList<String>(Arrays.asList("-B", "-e", "clean", "deploy"));
        }
        // use custom settings.xml file, if one specified
        String kjarsBuildSettingsXml = TestConfig.getKjarsBuildSettingsXml();
        if (kjarsBuildSettingsXml != null && !kjarsBuildSettingsXml.isEmpty()) {
            mvnArgs.add("-s");
            mvnArgs.add(kjarsBuildSettingsXml);
        }
        int mvnRunResult = cli.doMain(mvnArgs.toArray(new String[mvnArgs.size()]), basedir, System.out, System.err);

        Thread.currentThread().setContextClassLoader(classLoaderBak);

        if (mvnRunResult != 0) {
            throw new RuntimeException("Error while building Maven project from basedir " + basedir +
                    ". Return code=" + mvnRunResult);
        }
        logger.debug("Maven project successfully built and deployed!");
    }

    public static void buildAndDeployCommonMavenParent() {
        // deploy only once as it is not needed to do that with every request
        if (!commonParentDeployed) {
            buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/common-parent").getFile());
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
        KieServices ks = KieServices.Factory.get();
        createAndDeployJar(ks, releaseId, drl);

        // make sure it is not deployed in the in-memory repository
        ks.getRepository().removeKieModule(releaseId);
    }

    private static void createAndDeployJar(KieServices ks,
            ReleaseId releaseId,
            String... drls) {
        KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(
                releaseId);
        for (int i = 0; i < drls.length; i++) {
            if (drls[i] != null) {
                kfs.write("src/main/resources/org/pkg1/r" + i + ".drl", drls[i]);
            }
        }
        byte[] pom = kfs.read("pom.xml");
        KieBuilder kb = ks.newKieBuilder(kfs).buildAll();
        Assert.assertFalse(kb.getResults().getMessages(org.kie.api.builder.Message.Level.ERROR).toString(),
                kb.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR));
        InternalKieModule kieModule = (InternalKieModule) ks.getRepository().getKieModule(releaseId);
        byte[] jar = kieModule.getBytes();

        getRepository().installArtifact(releaseId, jar, pom);
    }

    public static MavenRepository getRepository() {
        if (repository == null) {
            // Initialize repository with minimal pom file.
            KieServices ks = KieServices.Factory.get();
            ReleaseId initReleaseId = new ReleaseId("org.kie.server.initial", "init-maven-repo", "42");
            KieFileSystem kfs = ks.newKieFileSystem().generateAndWritePomXML(initReleaseId);
            byte[] pom = kfs.read("pom.xml");

            MavenProject minimalMavenProject = MavenProjectLoader.parseMavenPom(new ByteArrayInputStream(pom));
            repository = MavenRepository.getMavenRepository(minimalMavenProject);
        }

        return repository;
    }
}
