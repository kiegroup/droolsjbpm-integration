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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Test;
import org.kie.hacep.core.Bootstrap;
import org.kie.hacep.core.infra.election.State;
import org.kie.hacep.core.infra.message.SnapshotMessage;
import org.kie.remote.RemoteKieSession;
import org.kie.remote.util.KafkaRemoteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.kie.remote.CommonConfig.getTestProperties;
import static org.kie.remote.util.SerializationUtil.deserialize;

public class PodAsLeaderSnapshotTest extends KafkaFullTopicsTests{

    private Logger logger = LoggerFactory.getLogger("org.hacep");

    @Test(timeout = 30000)
    public void processMessagesAsLeaderAndCreateSnapshotTest() {
        Bootstrap.startEngine(envConfig);
        Bootstrap.getConsumerController().getCallback().updateStatus(State.LEADER);
        KafkaConsumer eventsConsumer = kafkaServerTest.getConsumer(envConfig.getEventsTopicName(),
                                                                   Config.getConsumerConfig("eventsProcessMessagesAsLeaderAndCreateSnapshotTest"));
        KafkaConsumer snapshotConsumer = kafkaServerTest.getConsumer(envConfig.getSnapshotTopicName(),
                                                                     Config.getSnapshotConsumerConfig());
        KafkaConsumer controlConsumer = kafkaServerTest.getConsumer(envConfig.getControlTopicName(),
                                                                    Config.getConsumerConfig("controlProcessMessagesAsLeaderAndCreateSnapshotTest"));

        kafkaServerTest.insertBatchStockTicketEvent(10,
                                                    topicsConfig,
                                                    RemoteKieSession.class, KafkaRemoteUtil.getListener(getTestProperties(), false));
        try {

            final AtomicInteger attempts = new AtomicInteger(0);

            //EVENTS TOPIC
            logger.warn("Checks on Events Topic");
            int events = 0;
            while(events < 11) {
                ConsumerRecords eventsRecords = eventsConsumer.poll(Duration.ofSeconds(3));
                events = events + eventsRecords.count();
                int attemptNumber = attempts.incrementAndGet();
                logger.warn("Attempt number on events topic:{}", attemptNumber);
                if(attemptNumber == 30){
                    throw new RuntimeException("No enough Events message available "+ events +" after "+attempts + "attempts.");
                }
            }
            assertEquals(11, events); //1 fireUntilHalt + 11 stock ticket

            //CONTROL TOPIC
            logger.warn("Checks on Control Topic");
            attempts.set(0);
            events = 0;
            while (events < 11) {
                ConsumerRecords records = controlConsumer.poll(Duration.ofSeconds(3));
                events = events + records.count();
                int attemptNumber = attempts.incrementAndGet();
                logger.warn("Attempt number on control topic:{}", attemptNumber);
                if(attemptNumber == 30){
                    throw new RuntimeException("No enough Control message available "+ events +" after "+attempts + "attempts.");
                }
            }

            assertEquals(11, events); //1 fireUntilHalt + 11 stock ticket

            //SNAPSHOT TOPIC
            logger.warn("Checks on Snapshot Topic");
            attempts.set(0);
            events = 0;
            while(events < 1) {
                ConsumerRecords snapshotRecords = snapshotConsumer.poll(Duration.ofSeconds(3));
                events = events + snapshotRecords.count();
                snapshotRecords.forEach(o -> {
                    ConsumerRecord record = (ConsumerRecord)o;
                    SnapshotMessage snapshot = deserialize((byte[]) record.value());
                    assertNotNull(snapshot);
                    assertTrue(snapshot.getLastInsertedEventOffset() > 0);
                    assertFalse(snapshot.getFhMapKeys().isEmpty());
                    assertNotNull(snapshot.getLastInsertedEventkey());
                    assertEquals(9, snapshot.getFhMapKeys().size());
                    assertNotNull(snapshot.getLastInsertedEventkey());
                });
                int attemptNumber = attempts.incrementAndGet();
                logger.warn("Attempt number on snapshot topic:{}", attemptNumber);
                if(attemptNumber == 30){
                    throw new RuntimeException("No enough Snapshot message available "+ events +" after "+attempts + "attempts.");
                }
            }
            assertEquals(1, events);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            eventsConsumer.close();
            snapshotConsumer.close();
        }
    }
}
