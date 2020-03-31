/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.hacep;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.hacep.core.Bootstrap;
import org.kie.hacep.core.infra.election.State;
import org.kie.hacep.core.infra.message.SnapshotMessage;
import org.kie.remote.CommonConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.kie.remote.util.SerializationUtil.deserialize;

public class SnapshotOnDemandTest {

    private KafkaUtils kafkaServerTest;
    private EnvConfig config;
    private Logger logger = LoggerFactory.getLogger(SnapshotOnDemandTest.class);

    public static EnvConfig getEnvConfig() {
        return EnvConfig.anEnvConfig().
                withNamespace(CommonConfig.DEFAULT_NAMESPACE).
                withControlTopicName(Config.DEFAULT_CONTROL_TOPIC).
                withEventsTopicName(CommonConfig.DEFAULT_EVENTS_TOPIC).
                withSnapshotTopicName(Config.DEFAULT_SNAPSHOT_TOPIC).
                withKieSessionInfosTopicName(CommonConfig.DEFAULT_KIE_SESSION_INFOS_TOPIC).
                withPrinterType(PrinterKafkaImpl.class.getName()).
                withPollTimeUnit("sec").
                withPollTimeout("10").
                withPollSnapshotTimeUnit("sec").
                withPollSnapshotTimeout("10").
                withMaxSnapshotAgeSeconds("60000").
                underTest(true);
    }

    @Before
    public void setUp() throws Exception {
        config = getEnvConfig();
        kafkaServerTest = new KafkaUtils();
        kafkaServerTest.startServer();
    }

    @After
    public void tearDown() {
        kafkaServerTest.tearDown();
    }

    @Test(timeout = 30000L)
    public void createSnapshotOnDemandTest() {
        Bootstrap.startEngine(config);
        Bootstrap.getConsumerController().getCallback().updateStatus(State.LEADER);

        KafkaConsumer eventsConsumer = kafkaServerTest.getConsumer(config.getEventsTopicName(),
                                                                   Config.getConsumerConfig("SnapshotOnDemandTest.createSnapshotOnDemandTest"));
        KafkaConsumer controlConsumer = kafkaServerTest.getConsumer(config.getControlTopicName(),
                                                                    Config.getConsumerConfig("SnapshotOnDemandTest.createSnapshotOnDemandTest"));

        KafkaConsumer snapshotConsumer = kafkaServerTest.getConsumer(config.getSnapshotTopicName(),
                                                                     Config.getConsumerConfig("SnapshotOnDemandTest.createSnapshotOnDemandTest"));

        try {
            ConsumerRecords eventsRecords = eventsConsumer.poll(Duration.ofSeconds(2));
            assertEquals(0, eventsRecords.count());

            ConsumerRecords controlRecords = controlConsumer.poll(Duration.ofSeconds(2));
            assertEquals(0, controlRecords.count());

            ConsumerRecords snapshotRecords = snapshotConsumer.poll(Duration.ofSeconds(2));
            assertEquals(0, snapshotRecords.count());

            KafkaUtils.insertSnapshotOnDemandCommand();

            List<SnapshotMessage> messages = new ArrayList<>();
            final AtomicInteger attempts = new AtomicInteger(0);
            while (messages.size() < 1) {
                snapshotRecords = snapshotConsumer.poll(Duration.ofSeconds(5));
                snapshotRecords.forEach(o -> {
                    ConsumerRecord<String, byte[]> controlRecord = (ConsumerRecord<String,byte[]>)o;
                    SnapshotMessage snapshotMessage = deserialize(controlRecord.value());
                    messages.add(snapshotMessage);
                });

                int attemptNumber = attempts.incrementAndGet();
                logger.warn("Attempt number:{}", attemptNumber);
                if(attempts.get() == 10){
                    throw new RuntimeException("No control message available after "+attempts + "attempts in waitForControlMessage");
                }
            }

            assertEquals(1, messages.size());
            Iterator<SnapshotMessage> messagesIter = messages.iterator();
            SnapshotMessage msg = messagesIter.next();
            assertNotNull(msg);
            assertTrue(msg.getFhManager().getFhMapKeys().isEmpty());
            assertEquals(0, msg.getLastInsertedEventOffset());
            assertNotNull(msg.getSerializedSession());

            eventsRecords = eventsConsumer.poll(Duration.ofSeconds(1));
            assertEquals(1, eventsRecords.count());

            controlRecords = controlConsumer.poll(Duration.ofSeconds(1));
            assertEquals(1, controlRecords.count());
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            eventsConsumer.close();
            controlConsumer.close();
            snapshotConsumer.close();
            Bootstrap.stopEngine();
        }
    }
}
