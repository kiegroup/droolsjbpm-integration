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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.testcontainers.containers.KafkaContainer;

import static org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.jbpm.springboot.samples.KafkaFixture.KAFKA_FLOW_ID;
import static org.jbpm.springboot.samples.KafkaFixture.KAFKA_RESULT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

@RunWith(Parameterized.class)
@SpringBootTest(classes = {JBPMApplication.class, TestAutoConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:application-test.properties")
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaSerializationTest extends KafkaBaseTest {

    @ClassRule
    public static final SpringClassRule scr = new SpringClassRule();
 
    @Rule
    public final SpringMethodRule smr = new SpringMethodRule();
    
    @Autowired
    private DeploymentService deploymentService;
    
    @Autowired
    private ProcessService processService;
    
    @Autowired
    CountDownLatchEventListener countDownLatchEventListener;
    
    @Rule
    public KafkaContainer kafka = new KafkaContainer();
    
    protected static KafkaFixture kafkaFixture = new KafkaFixture();
    
    protected String deploymentId;
    
    @BeforeClass
    public static void generalSetup() {
        kafkaFixture.generalSetup();
    }

    @Before
    public void setup() throws IOException, InterruptedException {
        System.setProperty(BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        System.setProperty(CLIENT_ID_CONFIG, "test_jbpm");
        System.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        System.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, "com.myspace.kafka_process.BoxSerializer");
        
        kafkaFixture.createTopic(kafka);
        deploymentId = kafkaFixture.setup(deploymentService, strategy);
        
        countDownLatchEventListener.configure(KAFKA_FLOW_ID, 1);
        countDownLatchEventListener.setVariable(KAFKA_RESULT);
    }

    @After
    public void cleanup() {
        kafkaFixture.cleanup(deploymentService);
    }

    @Test(timeout = 60000)
    public void testKafkaWIHSendMessage() throws Exception {
        
        Long processInstanceId = processService.startProcess(deploymentId, 
                                                             KAFKA_FLOW_ID, 
                                                             kafkaFixture.getFlowVariables());

        assertTrue(processInstanceId > 0);
        
        kafkaFixture.assertConsumerMessagesBox(kafka.getBootstrapServers());
        
        //Countdown decrements the count of the latch before process ends
        countDownLatchEventListener.getCountDown().await();
        
        assertEquals("success", (String)countDownLatchEventListener.getResult());
    }
}
