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

package org.jbpm.springboot.samples;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.common.serialization.StringSerializer;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.springboot.samples.events.listeners.CountDownLatchEventListener;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;

import static org.jbpm.springboot.samples.KafkaFixture.KAFKA_PROCESS_ID;
import static org.jbpm.springboot.samples.KafkaFixture.KAFKA_RESULT;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
@SpringBootTest(classes = {JBPMApplication.class, TestAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaProxySampleTest extends KafkaBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProxySampleTest.class);
    
    private static final int TOXY_PROXY_PORT = Integer.parseInt(System.getProperty("toxiproxy.port"));
    
    @ClassRule
    public static final SpringClassRule scr = new SpringClassRule();
 
    @Rule
    public final SpringMethodRule smr = new SpringMethodRule();

    @Rule
    public Network network = Network.newNetwork();

    @Rule
    public KafkaContainer kafka = new KafkaContainer()
                                        .withExposedPorts(TOXY_PROXY_PORT)
                                        .withNetwork(network);

    @Rule
    public ToxiproxyContainer toxiproxy  = new ToxiproxyContainer().withNetwork(network);

    @Autowired
    private DeploymentService deploymentService;
    
    @Autowired
    private ProcessService processService;
    
    @Autowired
    CountDownLatchEventListener countDownLatchEventListener;
    
    protected static KafkaFixture kafkaFixture = new KafkaFixture();
    
    protected String deploymentId;
    
    protected String proxyBootstrap;

    protected ToxiproxyContainer.ContainerProxy kafkaProxy;
    
    @BeforeClass
    public static void generalSetup() {
        kafkaFixture.generalSetup();
    }

    @Before
    public void setup() throws IOException, InterruptedException {
        toxiproxy.start();
        kafkaProxy = toxiproxy.getProxy(kafka, TOXY_PROXY_PORT);
        kafka.start();
        kafkaFixture.createTopic(kafka);
        proxyBootstrap = kafkaProxy.getContainerIpAddress()+":"+kafkaProxy.getProxyPort();
        
        System.setProperty(BOOTSTRAP_SERVERS_CONFIG, proxyBootstrap);
        System.setProperty(CLIENT_ID_CONFIG, "test_jbpm");
        System.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        System.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        
        deploymentId = kafkaFixture.setup(deploymentService, strategy);
        
        countDownLatchEventListener.setVariable(KAFKA_RESULT);
    }

    @After
    public void cleanup() {
        kafka.stop();
        toxiproxy.stop();
        kafkaFixture.cleanup(deploymentService);
    }

    @Test(timeout = 240000)
    public void testKafkaWIHNoConnection() throws Exception {

        countDownLatchEventListener.configureNode(KAFKA_PROCESS_ID, "TaskErrorAfterKafkaMessageSent", 2);
        
        kafkaProxy.setConnectionCut(true);
        
        //Kafka WIH will try during  publish config max.block.ms -60 seconds by default- to get connected to Kafka
        //TimeoutException: Topic mytopic not present in metadata after 60000 ms.
        Long processInstanceId = processService.startProcess(deploymentId,
                                                             KAFKA_PROCESS_ID,
                                                             kafkaFixture.getProcessVariables());

        assertTrue(processInstanceId > 0);

        //Countdown decrements the count of the latch twice: 
        //TaskErrorAfterKafkaMessageSent node and before process ends
        countDownLatchEventListener.getCountDown().await();

        assertEquals("failure", (String)countDownLatchEventListener.getResult());
    }

    
    @Test(timeout = 60000)
    public void testKafkaWIHReconnect() throws Exception {

        countDownLatchEventListener.configure(KAFKA_PROCESS_ID, 1);
        
        kafkaProxy.setConnectionCut(true);
        
        reconnectProxyLater(10);
        
        Long processInstanceId = processService.startProcess(deploymentId,
                                                             KAFKA_PROCESS_ID,
                                                             kafkaFixture.getProcessVariables());

        assertTrue(processInstanceId > 0);

        kafkaFixture.assertConsumerMessages(proxyBootstrap);

        //Countdown decrements the count of the latch before process ends
        countDownLatchEventListener.getCountDown().await();
        
        assertEquals("success", countDownLatchEventListener.getResult());
    }

    private void reconnectProxyLater(int reconnectTime) {
        new Thread(() -> {
            CountDownLatch lock = new CountDownLatch(1);
            try {
                lock.await(reconnectTime, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            kafkaProxy.setConnectionCut(false);
        }).start();
    }
}
