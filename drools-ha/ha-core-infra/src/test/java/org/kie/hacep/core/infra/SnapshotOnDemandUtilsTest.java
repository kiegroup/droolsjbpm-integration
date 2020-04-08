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
package org.kie.hacep.core.infra;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Before;
import org.junit.Test;
import org.kie.hacep.EnvConfig;
import org.kie.hacep.core.InfraFactory;
import org.kie.hacep.core.infra.utils.SnapshotOnDemandUtils;
import org.kie.hacep.core.infra.utils.SnapshotOnDemandUtilsImpl;
import org.kie.remote.impl.producer.Producer;

import static org.junit.Assert.*;

public class SnapshotOnDemandUtilsTest {

    private SnapshotOnDemandUtils snapshotOnDemandUtils;
    private EnvConfig config;

    @Before
    public void init(){
        snapshotOnDemandUtils = new SnapshotOnDemandUtilsImpl();
        config = EnvConfig.getDefaultEnvConfig();
        config.local(false);
        config.underTest(false);
    }

    @Test(expected = org.apache.kafka.common.KafkaException.class)
    public void askAKafkaConsumerWithoutServerUpTest(){
        KafkaConsumer consumer = snapshotOnDemandUtils.getConfiguredSnapshotConsumer(config);
        assertNull(consumer);
    }

    @Test(expected = org.apache.kafka.common.KafkaException.class)
    public void askASnapshotWithoutServerUpTest(){
        SessionSnapshooter sessionSnapshooter = new DefaultSessionSnapShooter(config, snapshotOnDemandUtils);
        Producer producer = InfraFactory.getProducer(config.isLocal());
        SnapshotInfos infos = snapshotOnDemandUtils.askASnapshotOnDemand(config, sessionSnapshooter, producer );
        assertNull(infos);
    }
}
