package org.kie.server.integrationtests;

import org.drools.compiler.kie.builder.impl.InternalKieModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.scanner.MavenRepository;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class KieServerBaseIntegrationTest {
    private static Logger logger = LoggerFactory.getLogger(KieServerBaseIntegrationTest.class);

    public static final String BASE_URI = System.getProperty("kie.server.base.uri",
            "http://localhost:8080/kie-server-services/services/rest/server");

    private static MavenRepository repository;

    protected KieServicesClient client;

    @BeforeClass
    public static void logSettings() {
        logger.debug("Kie Server base URI: " + BASE_URI);
    }

    @BeforeClass
    public static void configureCustomSettingsXml() {
        System.setProperty("kie.maven.settings.custom",
                ClassLoader.class.getResource("/kie-server-testing-custom-settings.xml").getFile());
        logger.debug("Value of 'kie.maven.settings.custom' property:" + System.getProperty("kie.maven.settings.custom"));
    }

}
