package org.kie.server.spring.boot.autoconfiguration.audit.replication;

import static org.kie.scanner.KieMavenRepository.getKieMavenRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.appformer.maven.integration.MavenRepository;
import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.Message;
import org.kie.scanner.KieMavenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

public class KieJarBuildHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieJarBuildHelper.class);

    public static void createKieJar(String resource) {       
        
        KieServices ks = KieServices.get();
        KieBuilder kieBuilder = ks.newKieBuilder(new File(resource));
        KieBuilder build = kieBuilder.buildAll();
        InternalKieModule kjar = (InternalKieModule) build.getKieModule();

        List<Message> messages = kieBuilder.buildAll().getResults().getMessages();
        if (!messages.isEmpty()) {
            for (Message message : messages) {
                LOGGER.error("Error Message: ({}) {}", message.getPath(), message.getText());
            }
            throw new RuntimeException("There are errors building the package, please check your knowledge assets!");
        }
        
        String pomFileName = MavenRepository.toFileName(kjar.getReleaseId(), null) + ".pom";
        File pomFile = new File(System.getProperty("java.io.tmpdir"), pomFileName);
        try (FileOutputStream fos = new FileOutputStream(pomFile)) {
            fos.write(Files.toByteArray(new File(resource + "/pom.xml")));
            fos.flush();
        } catch (IOException ioe) {
            throw new RuntimeException("Unable to write pom.xml to temporary file : " + ioe.getMessage(), ioe);
        }
    
        KieMavenRepository repository = getKieMavenRepository();
        repository.installArtifact(kjar.getReleaseId(), kjar, pomFile);
    }

}
