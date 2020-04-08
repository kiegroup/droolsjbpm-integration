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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class DefaultKafkaConsumerTest {

  @Mock
  protected Producer mockProducer;
  @Mock
  protected DroolsConsumerHandler handlerMock;
  @Mock
  protected KafkaConsumer primaryConsumerMock;
  @Mock
  protected KafkaConsumer secondaryConsumerMock;
  @Mock
  protected ConsumerUtilsCore consumerUtilsCoreMock;
  @Mock
  protected DefaultSessionSnapShooter defaultSessionSnapShooterMock;
  @Mock
  protected SnapshotOnDemandUtils snapshotOnDemandUtilsMock;

  private DefaultKafkaConsumer spy;

  @Before
  public void initTest() {
    EnvConfig envConfigTest = EnvConfig.getDefaultEnvConfig();
    ControlMessage lastControlMessage = new ControlMessage();
    lastControlMessage.setId("1");
    lastControlMessage.setOffset(1l);
    when(consumerUtilsCoreMock.getLastEvent(envConfigTest.getControlTopicName(), envConfigTest.getPollTimeout())).thenReturn(lastControlMessage);
    when(defaultSessionSnapShooterMock.getLastSnapshotTime()).thenReturn(LocalDateTime.now());
    when(handlerMock.initializeKieSessionFromSnapshotOnDemand(any(EnvConfig.class), any(SnapshotInfos.class))).thenReturn(Boolean.TRUE);

    spy = Mockito.spy(new DefaultKafkaConsumer(){
      {
        this.envConfig = envConfigTest;
        this.producer = mockProducer;
        this.consumerUtilsCore = consumerUtilsCoreMock;
        this.snapShooter = defaultSessionSnapShooterMock;
        this.snapshotOnDemandUtils = snapshotOnDemandUtilsMock;
        this.consumerHandler = handlerMock;
        createKafkaConsumer();
        updateKafkaSecondaryConsumer();
      }

      @Override
      public void createKafkaConsumer() {
        this.kafkaConsumer = primaryConsumerMock;
      }

      @Override
      public void updateKafkaSecondaryConsumer() {
        this.kafkaSecondaryConsumer = secondaryConsumerMock;
      }
    });
  }

  @Test
  public void updateStatusBecomingLeaderAtStartupTest(){
    spy.updateStatus(State.BECOMING_LEADER);
    verify(spy).updateStatus(State.BECOMING_LEADER);
    verify(spy, never()).updateOnRunningConsumer(any(State.class));
    verify(spy, never()).askAndProcessSnapshotOnDemand(any(SnapshotInfos.class));
    verify(spy, never()).enableConsumeAndStartLoop(any(State.class));
   }

  @Test
  public void updateStatusLeaderAtStartupTest(){
    spy.updateStatus(State.LEADER);
    verify(spy).updateStatus(State.LEADER);
    verify(spy, never()).updateOnRunningConsumer(any(State.class));
    verify(spy, never()).askAndProcessSnapshotOnDemand(any(SnapshotInfos.class));
    verify(spy, times(1)).enableConsumeAndStartLoop(eq(State.LEADER));
    verify(spy, times(1)).setLastProcessedKey();
    verify(spy, times(1)).assignAndStartConsume();
  }

  @Test
  public void updateStatusReplicaAtStartupTest(){
    spy.updateStatus(State.REPLICA);
    verify(spy).updateStatus(State.REPLICA);
    verify(spy, never()).updateOnRunningConsumer(any(State.class));
    verify(spy, times(1)).askAndProcessSnapshotOnDemand(any(SnapshotInfos.class));
    verify(spy, times(1)).enableConsumeAndStartLoop(eq(State.REPLICA));
    verify(spy, times(1)).setLastProcessedKey();
    verify(spy, times(1)).assignAndStartConsume();
  }

}
