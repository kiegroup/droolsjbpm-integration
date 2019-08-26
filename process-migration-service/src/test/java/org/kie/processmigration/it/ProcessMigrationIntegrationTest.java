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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.appformer.maven.integration.MavenRepository;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.processmigration.model.Execution;
import org.kie.processmigration.model.Migration;
import org.kie.processmigration.model.MigrationDefinition;
import org.kie.processmigration.model.Plan;
import org.kie.processmigration.model.ProcessRef;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class ProcessMigrationIntegrationTest {

    private static final String ARTIFACT_ID = "test";
    private static final String GROUP_ID = "com.myspace.test";
    private static final String SOURCE_CONTAINER_ID = "test_1.0.0";
    private static final String TARGET_CONTAINER_ID = "test_2.0.0";
    private static final String PIM_ENDPOINT = System.getProperty("pim.endpoint");

    private static String CONTAINER_ID = "test";
    private static String PROCESS_ID = "test.myprocess";

    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    private HttpClient client;
    private String kieServerId;

    @Before
    public void init() throws IOException {
        KieServicesClient kieClient = createClient();
        kieServerId = kieClient.getServerInfo().getResult().getServerId();
        deployProcesses(kieClient);
        ProcessServicesClient processClient = kieClient.getServicesClient(ProcessServicesClient.class);
        ProcessDefinition definition = processClient.getProcessDefinition(CONTAINER_ID, PROCESS_ID);
        assertNotNull(definition);
        assertEquals(PROCESS_ID, definition.getId());
        client = HttpClientBuilder.create().setDefaultCredentialsProvider(getBasicAuth()).build();
    }

    @Test
    public void testHealthCheck() throws IOException {
        HttpGet get = new HttpGet(PIM_ENDPOINT + "/health");
        HttpResponse response = client.execute(get);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode());
    }

    @Test
    public void testMigration() throws IOException, JAXBException {
        // Given
        startProcesses();

        // When
        createMigration();

        // Then
        ProcessServicesClient processClient = createClient().getServicesClient(ProcessServicesClient.class);
        List<ProcessInstance> instances = processClient.findProcessInstances(SOURCE_CONTAINER_ID, 0, 10);
        assertEquals(1, instances.size());
        assertEquals(Long.valueOf(2L), instances.get(0).getId());

        instances = processClient.findProcessInstances(TARGET_CONTAINER_ID, 0, 10);
        assertEquals(1, instances.size());
        assertEquals(Long.valueOf(1L), instances.get(0).getId());
    }

    private Migration createMigration() throws IOException, JAXBException {
        Plan plan = createPlan();
        MigrationDefinition def = new MigrationDefinition();
        def.setPlanId(plan.getId());
        def.setKieServerId(kieServerId);
        def.setProcessInstanceIds(Arrays.asList(1L));
        def.setExecution(new Execution().setType(Execution.ExecutionType.SYNC));

        HttpPost post = preparePost("/migrations");

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, def);
        post.setEntity(new StringEntity(writer.toString()));
        HttpResponse r = client.execute(post);
        assertEquals(HttpStatus.SC_OK, r.getStatusLine().getStatusCode());
        StringReader reader = new StringReader(EntityUtils.toString(r.getEntity()));
        Migration migration = mapper.readValue(reader, Migration.class);
        assertNotNull(migration);
        assertEquals("kermit", migration.getDefinition().getRequester());
        return migration;
    }

    private Plan createPlan() throws IOException, JAXBException {
        Plan plan = new Plan();
        plan.setSource(new ProcessRef().setContainerId(SOURCE_CONTAINER_ID).setProcessId(PROCESS_ID));
        plan.setTarget(new ProcessRef().setContainerId(TARGET_CONTAINER_ID).setProcessId(PROCESS_ID));

        HttpPost post = preparePost("/plans");

        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, plan);
        post.setEntity(new StringEntity(writer.toString()));
        HttpResponse r = client.execute(post);
        assertEquals(HttpStatus.SC_OK, r.getStatusLine().getStatusCode());
        StringReader reader = new StringReader(EntityUtils.toString(r.getEntity()));
        return mapper.readValue(reader, Plan.class);
    }

    private HttpPost preparePost(String path) {
        HttpPost post = new HttpPost(PIM_ENDPOINT + path);
        post.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        post.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        return post;
    }

    private void startProcesses() {
        ProcessServicesClient client = createClient().getServicesClient(ProcessServicesClient.class);
        client.startProcess(SOURCE_CONTAINER_ID, PROCESS_ID);
        client.startProcess(SOURCE_CONTAINER_ID, PROCESS_ID);
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
            InputStream is = ProcessMigrationIntegrationTest.class.getResource("/kjars/" + resource).openStream();
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            os.write(buffer);
        }
        return tmpFile;
    }

    private KieServicesClient createClient() {
        String kieEndpoint = System.getProperty("kie.server.endpoint");
        String kieUsername = System.getProperty("kie.server.username");
        String kiePassword = System.getProperty("kie.server.password");
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(kieEndpoint, kieUsername, kiePassword);
        configuration.setTimeout(60000);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        return KieServicesFactory.newKieServicesClient(configuration);
    }

    private CredentialsProvider getBasicAuth() {
        String pimUsername = System.getProperty("pim.username");
        String pimPassword = System.getProperty("pim.password");
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(pimUsername, pimPassword));
        return provider;
    }
}
