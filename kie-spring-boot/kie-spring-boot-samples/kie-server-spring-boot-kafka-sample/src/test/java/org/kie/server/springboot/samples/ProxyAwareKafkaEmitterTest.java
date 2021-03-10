/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.springboot.samples;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ABORTED;
import static org.kie.api.runtime.process.ProcessInstance.STATE_ACTIVE;

import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.springboot.samples.kafka.KieServerApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;

import eu.rekawek.toxiproxy.model.Toxic;
import eu.rekawek.toxiproxy.model.ToxicDirection;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class, TestAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode= DirtiesContext.ClassMode.BEFORE_CLASS)
public class ProxyAwareKafkaEmitterTest extends KafkaFixture{

    protected static final int TOXY_PROXY_PORT = Integer.parseInt(System.getProperty("toxiproxy.port"));

    protected static Network network = Network.newNetwork();
    
    protected static ProxyAwareKafkaContainer kafka = (ProxyAwareKafkaContainer) new ProxyAwareKafkaContainer()
                                                     .withExposedPorts(TOXY_PROXY_PORT)
                                                     .withNetwork(network);

    protected static ToxiproxyContainer toxiproxy  = new ToxiproxyContainer().withNetwork(network);
    
    protected static ToxiproxyContainer.ContainerProxy kafkaProxy;

    @Autowired
    protected DeploymentService deploymentService;

    @Autowired
    protected ProcessService processService;

    protected String deploymentId;
    
    @BeforeClass
    public static void beforeClass() {
        assumeTrue(isDockerAvailable());
        System.setProperty("org.kie.jbpm.event.emitters.kafka.topic.processes", CUSTOM_PROCESSES_TOPIC);
        toxiproxy.start();
        kafkaProxy = toxiproxy.getProxy(kafka, TOXY_PROXY_PORT);
        
        String proxyBootstrapServer = kafkaProxy.getContainerIpAddress()+":"+kafkaProxy.getProxyPort();
        kafka.setHost(proxyBootstrapServer);
        
        kafka.start();
        
        bootstrapServers = proxyBootstrapServer;
        generalSetup(true);
    }


    @After
    public void cleanup() {
        cleanup(deploymentService);
    }
    
    @AfterClass
    public static void teardown() {
        kafka.stop();
        toxiproxy.stop();
        System.clearProperty("org.kie.jbpm.event.emitters.kafka.topic.processes");
        System.clearProperty("org.kie.jbpm.event.emitters.kafka.boopstrap.servers");
        System.clearProperty("org.kie.jbpm.event.emitters.kafka.client.id");
    }
    
    @Test(timeout = 30000)
    public void testKafkaEmitterProcessStartAndAbortWithToxics() throws Exception {
        deploymentId = setup(deploymentService, EVALUATION_PROCESS_ID);
        
        Long processInstanceId = processService.startProcess(deploymentId, EVALUATION_PROCESS_ID, initParameters());

        assertNotNull(processInstanceId);
        assertTrue(processInstanceId > 0);
        
        consumeAndAssertRecords(CUSTOM_PROCESSES_TOPIC, PROCESS_TYPE, STATE_ACTIVE, 1);
        
        kafkaProxy.toxics().timeout("timing", ToxicDirection.DOWNSTREAM, 0);
        
        processService.abortProcessInstance(processInstanceId);
        
        ProcessInstance pi = processService.getProcessInstance(processInstanceId);
        assertNull(pi);
        
        for (Toxic t : kafkaProxy.toxics().getAll()) {
            t.remove();
        }
        
        consumeAndAssertRecords(CUSTOM_PROCESSES_TOPIC, PROCESS_TYPE, STATE_ABORTED, 1);
    }

}
