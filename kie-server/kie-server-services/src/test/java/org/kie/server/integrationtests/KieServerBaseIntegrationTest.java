package org.kie.server.integrationtests;

public abstract class KieServerBaseIntegrationTest {

    public static final String BASE_URI = System.getProperty("kie.server.base.uri",
            "http://localhost:8080/kie-server-services/services/rest/server");
}
