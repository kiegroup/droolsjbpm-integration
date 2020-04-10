/*
 * Copyright 20120 Red Hat, Inc. and/or its affiliates.
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
package org.kie.hacep.core.infra.utils;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.kie.hacep.EnvConfig;
import org.kie.hacep.core.infra.SessionSnapshooter;
import org.kie.hacep.core.infra.SnapshotInfos;
import org.kie.remote.impl.producer.Producer;

public interface SnapshotOnDemandUtils {

  SnapshotInfos askASnapshotOnDemand(EnvConfig config, SessionSnapshooter snapshooter, Producer producer);

  KafkaConsumer getConfiguredSnapshotConsumer(EnvConfig envConfig);
}
