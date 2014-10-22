/*
 * Copyright 2013 JBoss Inc
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

package org.kie.spring.tests;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.core.util.FileManager;
import org.junit.*;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieScanner;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.scanner.MavenRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.kie.scanner.MavenRepository.getMavenRepository;

public class KieSpringScannerTest {

    static ApplicationContext context = null;
    private File kPom;
    private ReleaseId releaseId;
    private FileManager fileManager;
    private final int FIRST_VALUE = 5;
    private final int SECOND_VALUE = 10;

    @Test
    public void testSpringKieScanner() throws Exception {

        KieServices ks = KieServices.Factory.get();

        //step 1: deploy the test module to MAVEN Repo
        MavenRepository repository = createAndDeployModule(ks);

        //step 2: load the spring context
        createSpringContext();

        //step 3: check the basic spring objects
        lookupNamedKieBase();
        lookupReleaseId();
        lookupReleaseIdScanner();

        //step 4: test the value
        checkForValue(FIRST_VALUE);

        //step 5: reploy the module
        redeployModule(repository, ks);

        //step 6: force the kie-scanner to scan
        KieScanner releaseIdScanner = context.getBean("spring-scanner-releaseId#scanner", KieScanner.class);
        releaseIdScanner.scanNow();

        //step 7: retest the value to ensure the scanner has picked up the new value
        checkForValue(SECOND_VALUE);

        //step 8: cleanup. Remove the module
        ks.getRepository().removeKieModule(releaseId);
    }

    protected MavenRepository createAndDeployModule(KieServices ks) throws IOException {
        this.fileManager = new FileManager();
        this.fileManager.setUp();
        releaseId = KieServices.Factory.get().newReleaseId("org.kie.spring", "spring-scanner-test", "1.0-SNAPSHOT");
        kPom = createKPom(releaseId);

        MavenRepository repository = getMavenRepository();

        InternalKieModule kJar1 = createKieJarWithClass(ks, releaseId, FIRST_VALUE);
        repository.deployArtifact(releaseId, kJar1, kPom);
        return repository;
    }

    protected void createSpringContext() throws Exception {
        context = new ClassPathXmlApplicationContext("org/kie/spring/kie-scanner.xml");
        assertNotNull(context);
    }

    protected void lookupNamedKieBase() throws Exception {
        KieBase kieBase = context.getBean("KBase1", KieBase.class);
        assertNotNull(kieBase);
    }

    protected void lookupReleaseId() throws Exception {
        ReleaseId releaseId = context.getBean("spring-scanner-releaseId", ReleaseId.class);
        assertNotNull(releaseId);
    }

    protected void lookupReleaseIdScanner() throws Exception {
        KieScanner releaseIdScanner = context.getBean("spring-scanner-releaseId#scanner", KieScanner.class);
        assertNotNull(releaseIdScanner);
    }

    protected void redeployModule(MavenRepository repository, KieServices ks) throws IOException {
        InternalKieModule kJar2 = createKieJarWithClass(ks, releaseId, SECOND_VALUE);
        repository.deployArtifact(releaseId, kJar2, kPom);
    }

    protected void checkForValue(int value) {
        List<Integer> list = new ArrayList<Integer>();
        KieBase kieBase = context.getBean("KBase1", KieBase.class);
        KieSession ksession = kieBase.newKieSession();

        ksession.setGlobal( "list", list );
        ksession.fireAllRules();
        ksession.dispose();
        assertTrue("Expected:<" + value + "> but was:<" + list.get(0)  + ">", list.get(0) == value);
    }

    protected InternalKieModule createKieJarWithClass(KieServices ks, ReleaseId releaseId, int value) throws IOException {
        KieFileSystem kfs = createKieFileSystemWithKProject(ks, false);
        kfs.writePomXML(getPom(releaseId));


        kfs.write("src/main/resources/KBase1/rule1.drl", createDRL(value));

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        assertTrue("", kieBuilder.buildAll().getResults().getMessages().isEmpty());
        return (InternalKieModule) kieBuilder.getKieModule();
    }

    protected KieFileSystem createKieFileSystemWithKProject(KieServices ks, boolean isdefault) {
        KieModuleModel kproj = ks.newKieModuleModel();

        KieBaseModel kieBaseModel1 = kproj.newKieBaseModel("KBase1").setDefault(isdefault)
                .setEqualsBehavior(EqualityBehaviorOption.EQUALITY)
                .setEventProcessingMode(EventProcessingOption.STREAM);

        KieSessionModel ksession1 = kieBaseModel1.newKieSessionModel("KSession1").setDefault(isdefault)
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType(ClockTypeOption.get("realtime"));

        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(kproj.toXML());
        return kfs;
    }

    protected File createKPom(ReleaseId releaseId) throws IOException {
        File pomFile = fileManager.newFile("pom.xml");
        fileManager.write(pomFile, getPom(releaseId));
        return pomFile;
    }

    protected String getPom(ReleaseId releaseId, ReleaseId... dependencies) {
        String pom =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                        "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                        "  <modelVersion>4.0.0</modelVersion>\n" +
                        "\n" +
                        "  <groupId>" + releaseId.getGroupId() + "</groupId>\n" +
                        "  <artifactId>" + releaseId.getArtifactId() + "</artifactId>\n" +
                        "  <version>" + releaseId.getVersion() + "</version>\n" +
                        "\n";
        if (dependencies != null && dependencies.length > 0) {
            pom += "<dependencies>\n";
            for (ReleaseId dep : dependencies) {
                pom += "<dependency>\n";
                pom += "  <groupId>" + dep.getGroupId() + "</groupId>\n";
                pom += "  <artifactId>" + dep.getArtifactId() + "</artifactId>\n";
                pom += "  <version>" + dep.getVersion() + "</version>\n";
                pom += "</dependency>\n";
            }
            pom += "</dependencies>\n";
        }
        pom += "</project>";
        return pom;
    }

    protected String createDRL(int value) {
        return "package org.kie.test\n" +
                "global java.util.List list\n" +
                "rule simple\n" +
                "when\n" +
                "then\n" +
                "   list.add(" + value + ");\n" +
                "end\n";
    }
}
