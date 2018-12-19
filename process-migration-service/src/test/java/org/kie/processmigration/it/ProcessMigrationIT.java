/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.it;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.appformer.maven.integration.MavenRepository;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.KieServices;
import org.kie.processmigration.model.Migration;
import org.kie.processmigration.model.MigrationDefinition;
import org.kie.processmigration.model.Plan;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.wildfly.swarm.arquillian.DefaultDeployment;
import org.wildfly.swarm.arquillian.DefaultDeployment.Type;

@RunWith(Arquillian.class)
@DefaultDeployment(type = Type.WAR, testable = true)
public class ProcessMigrationIT {

    private static final String ARTIFACT_ID = "test";
    private static final String GROUP_ID = "com.myspace.test";
    private static final String SOURCE_CONTAINER_ID = "test_1.0.0";
    private static final String TARGET_CONTAINER_ID = "test_2.0.0";
    private static final String BASIC_AUTH = getBasicAuth();
    private static final String PIM_ENDPOINT = System.getProperty("pim.endpoint");

    private static String CONTAINER_ID = "test";
    private static String PROCESS_ID = "test.myprocess";

    private String kieEndpoint;
    private String kieUsername;
    private String kiePassword;
    private String kieServerId;

    @ArquillianResource
    private InitialContext context;

    public ProcessMigrationIT() throws IOException {
        kieEndpoint = System.getProperty("kie-server.endpoint");
        kieUsername = System.getProperty("cargo.remote.username");
        kiePassword = System.getProperty("cargo.remote.password");

        KieServicesClient client = createClient();
        kieServerId = client.getServerInfo().getResult().getServerId();
        deployProcesses(client);
    }

    private static String getBasicAuth() {
        String pimUsername = System.getProperty("pim.username");
        String pimPassword = System.getProperty("pim.password");
        String credentials = Base64.getEncoder()
                                   .encodeToString(new StringBuilder(pimUsername)
                                                                                 .append(":")
                                                                                 .append(pimPassword)
                                                                                 .toString()
                                                                                 .getBytes());
        return new StringBuilder("Basic ").append(credentials).toString();
    }

    @Test
    public void testDataSource() throws NamingException {
        DataSource ds = (DataSource) context.lookup("java:jboss/datasources/pimDS");
        assertNotNull(ds);
    }

    @Test
    public void testContainersCreated() {
        ProcessServicesClient processClient = createClient().getServicesClient(ProcessServicesClient.class);
        ProcessDefinition definition = processClient.getProcessDefinition(CONTAINER_ID, PROCESS_ID);
        assertNotNull(definition);
        assertEquals(PROCESS_ID, definition.getId());
    }

    @Test
    public void testMigration() {
        List<Long> pids = startProcesses();
        createMigration();
        ProcessServicesClient processClient = createClient().getServicesClient(ProcessServicesClient.class);
        List<ProcessInstance> instances = processClient.findProcessInstances(TARGET_CONTAINER_ID, 1, 10);
        assertEquals(pids.size(), instances.size());
        assertTrue(instances.stream().map(pi -> pi.getId()).allMatch(id -> pids.contains(id)));
    }

    private Migration createMigration() {
        Plan plan = createPlan();
        MigrationDefinition def = new MigrationDefinition();
        def.setPlanId(plan.getId());
        def.setKieserverId(kieServerId);
        def.setProcessInstanceIds(Arrays.asList(1L));
        Response response = ClientBuilder.newClient()
                                         .target(PIM_ENDPOINT + "/migrations")
                                         .request(MediaType.APPLICATION_JSON)
                                         .header(HttpHeaders.AUTHORIZATION, getBasicAuth())
                                         .buildPost(Entity.json(def))
                                         .invoke();
        Migration migration = (Migration) response.getEntity();
        assertNotNull(migration);
        assertEquals("kermit", migration.getDefinition().getRequester());
        return migration;
    }

    private Plan createPlan() {
        Plan plan = new Plan();
        plan.setSourceContainerId(SOURCE_CONTAINER_ID);
        plan.setTargetContainerId(TARGET_CONTAINER_ID);
        plan.setTargetProcessId(PROCESS_ID);
        Response response = ClientBuilder.newClient()
                                         .target(PIM_ENDPOINT + "/plans")
                                         .request(MediaType.APPLICATION_JSON)
                                         .header(HttpHeaders.AUTHORIZATION, BASIC_AUTH)
                                         .buildPost(Entity.json(plan))
                                         .invoke();
        assertEquals(HttpStatus.SC_OK, response.getStatus());
        Plan createdPlan = (Plan) response.getEntity();
        assertEquals(1L, createdPlan.getId());
        return createdPlan;
    }

    private List<Long> startProcesses() {
        ProcessServicesClient client = createClient().getServicesClient(ProcessServicesClient.class);
        List<Long> pids = new ArrayList<>();
        pids.add(client.startProcess(SOURCE_CONTAINER_ID, PROCESS_ID));
        pids.add(client.startProcess(SOURCE_CONTAINER_ID, PROCESS_ID));
        return pids;
    }

    private void deployProcesses(KieServicesClient kieServicesClient) throws IOException {
        KieServices ks = KieServices.Factory.get();
        MavenRepository repo = MavenRepository.getMavenRepository();
        for (String version : Arrays.asList("1.0.0", "2.0.0")) {
            org.kie.api.builder.ReleaseId builderReleaseId = ks.newReleaseId(GROUP_ID, ARTIFACT_ID, version);
            File kjar = readFile(CONTAINER_ID + "-" + version + ".jar");
            File pom = readFile(CONTAINER_ID + "-" + version + ".pom");
            repo.installArtifact(builderReleaseId, kjar, pom);
            ReleaseId releaseId = new ReleaseId(GROUP_ID, ARTIFACT_ID, version);
            KieContainerResource resource = new KieContainerResource(CONTAINER_ID, releaseId);
            kieServicesClient.createContainer(CONTAINER_ID + "_" + version, resource);
        }
    }

    private File readFile(String resource) throws IOException {
        File tmpFile = new File(resource);
        tmpFile.deleteOnExit();
        try (OutputStream os = new FileOutputStream(tmpFile)) {
            InputStream is = ProcessMigrationIT.class.getResource("/kjars/" + resource).openStream();
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            os.write(buffer);
        }
        return tmpFile;
    }

    private KieServicesClient createClient() {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(kieEndpoint, kieUsername, kiePassword);
        configuration.setTimeout(60000);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        return KieServicesFactory.newKieServicesClient(configuration);
    }
}
