package org.kie.services.remote.basic;

import static org.kie.services.remote.setup.TestConstants.*;
import static org.junit.Assert.*;
import static org.kie.scanner.MavenRepository.getMavenRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.drools.compiler.kie.builder.impl.KieRepositoryImpl;
import org.drools.compiler.kie.builder.impl.KieServicesImpl;
import org.jbpm.kie.services.api.DeployedUnit;
import org.jbpm.kie.services.api.DeploymentService;
import org.jbpm.kie.services.api.DeploymentUnit.RuntimeStrategy;
import org.jbpm.kie.services.api.Kjar;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.scanner.MavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Startup
public class TestKjarDeploymentLoader {

    private static final Logger logger = LoggerFactory.getLogger(TestKjarDeploymentLoader.class);

    @Inject
    @Kjar
    private DeploymentService deploymentService;

    @PostConstruct
    public void init() throws Exception {
        KModuleDeploymentUnit deploymentUnit 
            = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION, KBASE_NAME, KSESSION_NAME);
        deploymentUnit.setStrategy(RuntimeStrategy.SINGLETON);

        DeployedUnit alreadyDeployedUnit = deploymentService.getDeployedUnit(deploymentUnit.getIdentifier());
        if (alreadyDeployedUnit == null) {
            deploymentService.deploy(deploymentUnit);
        }
        logger.info("Deployed [" + deploymentUnit.getIdentifier() + "]");
    }

    private static class BpmnResource {
        public String fileName;
        public String path;

        public BpmnResource(String path) {
            this.path = path;
            this.fileName = path.replaceAll("^.*/", "");
        }
    }
    
    public static void deployKjarToMaven() { 
        try { 
            deployKjarToMaven(GROUP_ID, ARTIFACT_ID, VERSION, KBASE_NAME, KSESSION_NAME);
        } catch(Exception e ) { 
            e.printStackTrace();
            fail( "Unable to deploy kjar to maven: " + e.getMessage());
        }
    }
    
    public static void deployKjarToMaven(String group, String artifact, String version, String kbaseName, String ksessionName) {
        List<BpmnResource> bpmnResources = new ArrayList<BpmnResource>();
        bpmnResources.add(new BpmnResource("/repo/test/humanTask.bpmn2"));
        bpmnResources.add(new BpmnResource("/repo/test/scriptTask.bpmn2"));
        bpmnResources.add(new BpmnResource("/repo/test/signal.bpmn2"));
        bpmnResources.add(new BpmnResource("/repo/test/varScriptTask.bpmn2"));
        
        final KieServices ks = new KieServicesImpl(){
            @Override
            public KieRepository getRepository() {
                return new KieRepositoryImpl(); // override repository to not store the artifact on deploy to trigger load from maven repo
            }
        };
        ReleaseId releaseId = ks.newReleaseId(group, artifact, version);
        InternalKieModule kjar = createKieJar(ks, releaseId, bpmnResources, kbaseName, ksessionName);
        
        String pomText = getPom(releaseId);
        String pomFileName = MavenRepository.toFileName(releaseId, null) + ".pom";
        File pomFile = new File( System.getProperty( "java.io.tmpdir" ), pomFileName );
        try {
            FileOutputStream fos = new FileOutputStream(pomFile);
            fos.write(pomText.getBytes());
            fos.flush();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        MavenRepository repository = getMavenRepository();
        repository.deployArtifact(releaseId, kjar, pomFile);
    }

    protected static InternalKieModule createKieJar(KieServices ks, ReleaseId releaseId, List<BpmnResource> bpmns, String kbaseName, String ksessionName) {
        KieFileSystem kfs = createKieFileSystemWithKProject(ks, kbaseName, ksessionName);
        kfs.writePomXML( getPom(releaseId) );

        for (BpmnResource bpmn : bpmns) {
            kfs.write("src/main/resources/" + kbaseName + "/" + bpmn.fileName, convertFileToString(bpmn.path));
        }

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        assertTrue(kieBuilder.buildAll().getResults().getMessages().isEmpty());
        return ( InternalKieModule ) kieBuilder.getKieModule();
    }
    
    private static String convertFileToString(String fileName) { 
        InputStreamReader input = new InputStreamReader(TestKjarDeploymentLoader.class.getResourceAsStream(fileName));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter output = new OutputStreamWriter(baos);
        char[] buffer = new char[4096];
        int n = 0;
        try {
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toString();
    }
    
    protected static KieFileSystem createKieFileSystemWithKProject(KieServices ks, String kbaseName, String ksessionName) {
        KieModuleModel kproj = ks.newKieModuleModel();

        KieBaseModel kieBaseModel = kproj.newKieBaseModel(kbaseName).setDefault(true)
                .setEqualsBehavior( EqualityBehaviorOption.EQUALITY )
                .setEventProcessingMode( EventProcessingOption.STREAM );

        KieSessionModel ksession = kieBaseModel.newKieSessionModel(ksessionName).setDefault(true)
                .setType(KieSessionModel.KieSessionType.STATEFUL)
                .setClockType( ClockTypeOption.get("realtime") );

        KieFileSystem kfs = ks.newKieFileSystem();
        kfs.writeKModuleXML(kproj.toXML());
        return kfs;
    }

    protected static String getPom(ReleaseId releaseId, ReleaseId... dependencies) {
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
}
