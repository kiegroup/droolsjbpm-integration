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
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Assert;
import org.junit.Test;
import org.kie.hacep.core.Bootstrap;
import org.kie.hacep.core.infra.election.State;
import org.kie.remote.RemoteKieSession;
import org.kie.remote.command.FireUntilHaltCommand;
import org.kie.remote.command.InsertCommand;
import org.kie.remote.command.RemoteCommand;
import org.kie.remote.message.ControlMessage;
import org.kie.remote.util.KafkaRemoteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.kie.remote.CommonConfig.SKIP_LISTENER_AUTOSTART;
import static org.kie.remote.util.SerializationUtil.deserialize;

public class PodAsLeaderTest extends KafkaFullTopicsTests {

    private Logger logger = LoggerFactory.getLogger("org.hacep");

    @Test(timeout = 30000)
    public void processOneSentMessageAsLeaderTest() {
        Bootstrap.startEngine(envConfig);
        Bootstrap.getConsumerController().getCallback().updateStatus(State.LEADER);
        KafkaConsumer eventsConsumer = kafkaServerTest.getConsumer(envConfig.getEventsTopicName(),
                                                                   Config.getConsumerConfig("eventsConsumerProcessOneSentMessageAsLeaderTest"));
        KafkaConsumer controlConsumer = kafkaServerTest.getConsumer(envConfig.getControlTopicName(),
                                                                    Config.getConsumerConfig("controlConsumerProcessOneSentMessageAsLeaderTest"));

        Properties props = (Properties) Config.getProducerConfig("InsertBatchStockTickets").clone();
        props.put(SKIP_LISTENER_AUTOSTART, true);

        logger.warn("Insert Stock Ticket event");
        kafkaServerTest.insertBatchStockTicketEvent(1, topicsConfig, RemoteKieSession.class, props, KafkaRemoteUtil.getListener(props, false));
        try {
            //EVENTS TOPIC
            logger.warn("Checks on Events topic");

            AtomicReference<ConsumerRecord<String, byte[]>> firstEvent = new AtomicReference<>();
            AtomicReference<ConsumerRecord<String, byte[]>> secondEvent = new AtomicReference<>();

            final AtomicInteger index = new AtomicInteger(0);
            final AtomicInteger attempts = new AtomicInteger(0);
            while (index.get() < 2) {
                ConsumerRecords eventsRecords = eventsConsumer.poll(Duration.ofSeconds(2));

                eventsRecords.forEach(o -> {
                    ConsumerRecord<String, byte[]> event = (ConsumerRecord<String, byte[]>) o;
                    assertNotNull(event);
                    Assert.assertEquals(event.topic(), envConfig.getEventsTopicName());
                    assertEquals(event.offset(), index.get());
                    RemoteCommand remoteCommand = deserialize(event.value());
                    logger.warn("Event {}:{} offset:{}", index.get(), remoteCommand, event.offset());
                    assertNotNull(remoteCommand.getId());
                    if (index.get() == 0) {
                        firstEvent.set(event);
                        assertTrue(remoteCommand instanceof FireUntilHaltCommand);
                    }
                    if (index.get() == 1) {
                        assertTrue(remoteCommand instanceof InsertCommand);
                        secondEvent.set(event);
                    }
                    index.incrementAndGet();
                });

                int attemptNumber = attempts.incrementAndGet();
                logger.warn("Attempt number:{}", attemptNumber);
                if(attempts.get() == 10){
                    throw new RuntimeException("No Events message available after "+attempts + "attempts.");
                }
            }


            //CONTROL TOPIC
            logger.warn("Checks on Control topic");

            List<ControlMessage> messages = new ArrayList<>();
            attempts.set(0);
            while (messages.size() < 2) {
                ConsumerRecords controlRecords = controlConsumer.poll(Duration.ofSeconds(2));
                controlRecords.forEach(o -> {
                    ConsumerRecord<String,byte[]> control = (ConsumerRecord<String,byte[]>)o;
                    assertNotNull(control);
                    ControlMessage controlMessage = deserialize(control.value());
                    controlMessage.setOffset(control.offset());
                    logger.warn("Control message found:{}", controlMessage);
                    messages.add(controlMessage);
                });

                int attemptNumber = attempts.incrementAndGet();
                logger.warn("Attempt number:{}", attemptNumber);
                if(attempts.get() == 10){
                    throw new RuntimeException("No control message available after "+attempts + "attempts.");
                }
            }

            assertEquals(2, messages.size());

            AtomicReference<ControlMessage> fireUntilHalt = new AtomicReference<>();
            AtomicReference<ControlMessage> insert = new AtomicReference<>();
            index.set(0);
            messages.forEach(controlMessage -> {
                if(index.get()==0){
                    assertNotNull(controlMessage);
                    fireUntilHalt.set(controlMessage);
                }
                if(index.get()==1){
                    assertNotNull(controlMessage);
                    insert.set(controlMessage);
                }
                index.incrementAndGet();
            });

            assertEquals(fireUntilHalt.get().getId(), firstEvent.get().key());
            assertTrue(fireUntilHalt.get().getSideEffects().isEmpty());
            assertEquals(insert.get().getId(), secondEvent.get().key());
            assertTrue(!insert.get().getSideEffects().isEmpty());
            logger.warn("Test ended, going to stop kafka");
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        } finally {
            eventsConsumer.close();
            logger.warn("Event consumer closed");
            controlConsumer.close();
            logger.warn("Control consumer closed");
        }
    }
}