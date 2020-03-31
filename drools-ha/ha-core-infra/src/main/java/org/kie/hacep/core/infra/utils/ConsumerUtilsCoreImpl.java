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
package org.kie.hacep.core.infra.utils;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.kie.hacep.Config;
import org.kie.hacep.core.InfraFactory;
import org.kie.hacep.util.ConsumerUtilsCore;
import org.kie.remote.message.ControlMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.remote.util.SerializationUtil.deserialize;

public class ConsumerUtilsCoreImpl implements ConsumerUtilsCore {

  private Logger logger = LoggerFactory.getLogger(ConsumerUtilsCoreImpl.class);

  public ControlMessage getLastEvent(String topic, Integer pollTimeout) {
    return getLastEvent(topic, Config.getConsumerConfig("LastEventConsumer"), pollTimeout);
  }

  public ControlMessage getLastEvent(String topic, Properties properties, Integer pollTimeout) {
    ControlMessage lastMessage = new ControlMessage();
    try(KafkaConsumer consumer = InfraFactory.getConsumer(topic, properties)) {
      ConsumerRecords records = consumer.poll(Duration.of(pollTimeout, ChronoUnit.MILLIS));
      Iterator<ConsumerRecord<String, byte[]>> iterator = records.iterator();
      while(iterator.hasNext()){
        ConsumerRecord<String, byte[]> record = iterator.next();
        lastMessage = deserialize(record.value());
      }
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    return lastMessage;
  }
}
