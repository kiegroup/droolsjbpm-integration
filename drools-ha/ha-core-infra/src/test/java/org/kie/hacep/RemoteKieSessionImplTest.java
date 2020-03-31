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

import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.kie.hacep.core.Bootstrap;
import org.kie.hacep.core.InfraFactory;
import org.kie.hacep.core.infra.election.State;
import org.kie.remote.RemoteKieSession;
import org.kie.remote.impl.RemoteKieSessionImpl;
import org.kie.remote.util.KafkaRemoteUtil;

import static org.junit.Assert.*;
import static org.kie.remote.CommonConfig.getTestProperties;

public class RemoteKieSessionImplTest extends KafkaFullTopicsTests{


    @Test
    public void getFactCountTest() {
        Properties props = getTestProperties();
        Bootstrap.startEngine(envConfig);
        Bootstrap.getConsumerController().getCallback().updateStatus(State.LEADER);
        kafkaServerTest.insertBatchStockTicketEvent(7, topicsConfig, RemoteKieSession.class, KafkaRemoteUtil.getListener(props, false));
        RemoteKieSessionImpl client = new RemoteKieSessionImpl(Config.getProducerConfig("getFactCountTest"), KafkaRemoteUtil.getListener(props, false) , InfraFactory.getProducer(false));
        try {
            client.fireUntilHalt();
            CompletableFuture<Long> factCountFuture = client.getFactCount();
            Long factCount = factCountFuture.get(20, TimeUnit.SECONDS);
            assertEquals( new Long(7), factCount);
        }catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            client.close();
        }
    }
}
