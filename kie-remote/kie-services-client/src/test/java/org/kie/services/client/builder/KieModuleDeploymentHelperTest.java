package org.kie.services.client.builder;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.drools.core.impl.EnvironmentImpl;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.scanner.MavenRepository;
import org.kie.services.client.deployment.FluentKieModuleDeploymentHelper;
import org.kie.services.client.deployment.KieModuleDeploymentHelper;
import org.kie.services.client.deployment.SingleKieModuleDeploymentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.artifact.Artifact;

public class KieModuleDeploymentHelperTest {

    protected static Logger logger = LoggerFactory.getLogger(KieModuleDeploymentHelperTest.class);
    
    private ZipInputStream zip;

    @After
    public void cleanUp() {
        if (zip != null) {
            try {
                zip.close();
            } catch (IOException e) {
                // do nothing
            }

        }
    }

    @Test
    @Ignore
    public void testSingleDeploymentHelper() throws Exception {
        SingleKieModuleDeploymentHelper deploymentHelper = KieModuleDeploymentHelper.newSingleInstance();

        List<String> resourceFilePaths = new ArrayList<String>();
        resourceFilePaths.add("repo/test/");
        resourceFilePaths.add("repo/scriptTask.bpmn2");

        List<Class<?>> kjarClasses = new ArrayList<Class<?>>();
        kjarClasses.add(KieModuleDeploymentHelper.class);
        kjarClasses.add(EnvironmentImpl.class);

        String groupId = "org.kie.services.client";
        String artifactId = "test-kjar";
        String version = "0.1-SNAPSHOT";
        deploymentHelper.createKieJarAndDeployToMaven(groupId, artifactId, version, 
                "defaultKieBase", "defaultKieSession",
                resourceFilePaths, kjarClasses);

        Artifact artifact = MavenRepository.getMavenRepository().resolveArtifact(groupId + ":" + artifactId + ":" + version);
        zip = new ZipInputStream(new FileInputStream(artifact.getFile()));

        List<String> jarFiles = new ArrayList<String>();
        ZipEntry ze = zip.getNextEntry();
        while( ze != null ) { 
            jarFiles.add(ze.getName());
            logger.debug(ze.getName());
            ze = zip.getNextEntry();
        }
        assertEquals("Num files in kjar", 10, jarFiles.size());
        Set<String> correctJarFiles = new HashSet<String>();
        for( String kjarFileName : jarFiles ) { 
            
        }
    }

    @Test
    @Ignore
    public void testFluentDeploymentHelper() throws Exception {
        String content = "test file created by " + this.getClass().getSimpleName();
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".tst");
        tempFile.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(content.getBytes());
        fos.close();
        
        FluentKieModuleDeploymentHelper deploymentHelper = KieModuleDeploymentHelper.newFluentInstance();

        String groupId = "org.kie.services.client.fluent";
        String artifactId = "test-kjar";
        String version = "0.1-SNAPSHOT";
        deploymentHelper = deploymentHelper.setGroupId(groupId)
                .setArtifactId(artifactId)
                .setVersion(version)
                .addResourceFilePath("repo/test/", "repo/scriptTask.bpmn2")
                .addResourceFilePath(tempFile.getAbsolutePath())
                .addResourceFilePath("/META-INF/Taskorm.xml")
                .addClass(KieModuleDeploymentHelperTest.class)
                .addClass(KieModule.class);

        KieBaseModel kbaseModel = deploymentHelper.getKieModuleModel().newKieBaseModel("otherKieBase");
        kbaseModel.setEqualsBehavior(EqualityBehaviorOption.EQUALITY).setEventProcessingMode(EventProcessingOption.STREAM);
        kbaseModel.newKieSessionModel("otherKieSession").setClockType(ClockTypeOption.get("realtime"));

        deploymentHelper.getKieModuleModel().getKieBaseModels().get("defaultKieBase").newKieSessionModel("secondKieSession");

        deploymentHelper.createKieJarAndDeployToMaven();

        Artifact artifact = MavenRepository.getMavenRepository().resolveArtifact(groupId + ":" + artifactId + ":" + version);
        zip = new ZipInputStream(new FileInputStream(artifact.getFile()));

        List<String> jarFiles = new ArrayList<String>();
        ZipEntry ze = zip.getNextEntry();
        while (ze != null) {
            jarFiles.add(ze.getName());
            logger.debug(ze.getName());
            ze = zip.getNextEntry();
        }
        assertEquals("Num files in kjar", 13, jarFiles.size());
        for( String name : jarFiles ) { 
            logger.debug(name);
        }
        // TODO FINISH checks on files: .class files, kmodule.xml, .bpmn files.. 
    }
}
