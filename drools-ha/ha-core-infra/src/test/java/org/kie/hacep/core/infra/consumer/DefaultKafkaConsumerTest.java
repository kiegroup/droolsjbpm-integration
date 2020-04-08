/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.hacep.core.infra.consumer;

import java.time.LocalDateTime;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.hacep.EnvConfig;
import org.kie.hacep.consumer.DroolsConsumerHandler;
import org.kie.hacep.core.infra.DefaultSessionSnapShooter;
import org.kie.hacep.core.infra.SnapshotInfos;
import org.kie.hacep.core.infra.election.State;
import org.kie.hacep.core.infra.utils.SnapshotOnDemandUtils;
import org.kie.hacep.util.ConsumerUtilsCore;
import org.kie.remote.impl.producer.Producer;
import org.kie.remote.message.ControlMessage;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DefaultKafkaConsumerTest {

  @Mock
  protected Producer producer;
  @Mock
  protected DroolsConsumerHandler handler;
  @Mock
  protected KafkaConsumer primaryConsumer;
  @Mock
  protected KafkaConsumer secondaryConsumer;
  @Mock
  protected ConsumerUtilsCore consumerUtilsCore;
  @Mock
  protected DefaultSessionSnapShooter defaultSessionSnapShooter;
  @Mock
  protected SnapshotOnDemandUtils snapshotOnDemandUtils;

  private DefaultKafkaConsumer consumer;

  @Before
  public void initTest() {
    EnvConfig envConfig = EnvConfig.getDefaultEnvConfig();
    ControlMessage lastControlMessage = new ControlMessage();
    lastControlMessage.setId("1");
    lastControlMessage.setOffset(1l);
    when(consumerUtilsCore.getLastEvent(envConfig.getControlTopicName(), envConfig.getPollTimeout())).thenReturn(lastControlMessage);
    when(defaultSessionSnapShooter.getLastSnapshotTime()).thenReturn(LocalDateTime.now());
    when(handler.initializeKieSessionFromSnapshotOnDemand(any(EnvConfig.class), any(SnapshotInfos.class))).thenReturn(Boolean.TRUE);
    consumer = new DefaultKafkaConsumer(EnvConfig.getDefaultEnvConfig(), producer, primaryConsumer, secondaryConsumer, consumerUtilsCore, defaultSessionSnapShooter, snapshotOnDemandUtils, handler);
  }

  @Test
  public void UpdateStatusBecomingLeaderAtStartupTest(){
    InfraCallbackStatus status = consumer.updateStatus(State.BECOMING_LEADER);
    assertFalse(status.isEnableConsumerAndStartLoop());// nothing happens
    assertFalse(status.isUpdateOnRunningConsumer());// isn't on running consumer
    assertFalse(status.isAskAndProcessSnapshotOnDemandResult()); // no snapshot enabled
    assertEquals(status.getPreviousState(), State.REPLICA); // every instance starts as a replica
  }

  @Test
  public void UpdateStatusLeaderAtStartupTest(){
    InfraCallbackStatus status = consumer.updateStatus(State.LEADER);
    assertTrue(status.isEnableConsumerAndStartLoop()); // it ask to start
    assertFalse(status.isUpdateOnRunningConsumer()); // isn't on running consumer
    assertFalse(status.isAskAndProcessSnapshotOnDemandResult()); // no snapshot enabled
    assertEquals(status.getPreviousState(), State.REPLICA);
  }

  @Test
  public void UpdateStatusReplicaAtStartupTest(){
    InfraCallbackStatus status = consumer.updateStatus(State.REPLICA);
    assertTrue(status.isEnableConsumerAndStartLoop());
    assertFalse(status.isUpdateOnRunningConsumer());
    assertTrue(status.isAskAndProcessSnapshotOnDemandResult());
    assertEquals(status.getPreviousState(), State.REPLICA);
  }

}
