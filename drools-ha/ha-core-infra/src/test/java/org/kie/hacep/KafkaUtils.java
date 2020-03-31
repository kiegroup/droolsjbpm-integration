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

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.server.NotRunning;
import kafka.utils.TestUtils;
import kafka.zk.EmbeddedZookeeper;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.utils.SystemTime;
import org.apache.kafka.common.utils.Time;
import org.kie.hacep.core.Bootstrap;
import org.kie.hacep.core.InfraFactory;
import org.kie.hacep.sample.kjar.StockTickEvent;
import org.kie.remote.CommonConfig;
import org.kie.remote.RemoteKieSession;
import org.kie.remote.RemoteStreamingKieSession;
import org.kie.remote.TopicsConfig;
import org.kie.remote.command.SnapshotOnDemandCommand;
import org.kie.remote.impl.RemoteKieSessionImpl;
import org.kie.remote.impl.RemoteStreamingKieSessionImpl;
import org.kie.remote.impl.consumer.Listener;
import org.kie.remote.impl.producer.Sender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaUtils implements AutoCloseable {

    private static final String ZOOKEEPER_HOST = "127.0.0.1";
    private static final String BROKER_HOST = "127.0.0.1";
    private static final String BROKER_PORT = "9092";
    private final static Logger logger = LoggerFactory.getLogger(KafkaUtils.class);
    private KafkaServer kafkaServer;
    private EmbeddedZookeeper zkServer;
    private String tmpDir;
    private KafkaAdminClient adminClient;
    private Logger kafkaLogger = LoggerFactory.getLogger("org.hacep");


    public Map<String, Object> getKafkaProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.RETRIES_CONFIG, 0);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return props;
    }


    public KafkaServer startServer() throws IOException {
        tmpDir = Files.createTempDirectory(Paths.get(System.getProperty("user.dir"), File.separator, "target"),
                                           "kafkatest-").toAbsolutePath().toString();
        zkServer = new EmbeddedZookeeper();
        String zkConnect = ZOOKEEPER_HOST + ":" + zkServer.port();
        Properties brokerProps = new Properties();
        brokerProps.setProperty("zookeeper.connect", zkConnect);
        brokerProps.setProperty("broker.id", "0");
        brokerProps.setProperty("log.dirs", tmpDir);
        brokerProps.setProperty("listeners", "PLAINTEXT://" + BROKER_HOST + ":" + BROKER_PORT);
        brokerProps.setProperty("offsets.topic.replication.factor", "1");
        brokerProps.setProperty("auto.create.topics.enable","true");
        KafkaConfig config = new KafkaConfig(brokerProps);
        Time mock = new SystemTime();
        kafkaServer = TestUtils.createServer(config, mock);
        Map<String, Object>  props = getKafkaProps();
        adminClient = (KafkaAdminClient) AdminClient.create(props);
        return kafkaServer;
    }

    public void shutdownServer() {
        if(adminClient != null) {
            adminClient.close();
        }
        logger.warn("Shutdown kafka server");
        Path tmp = Paths.get(tmpDir);
        try {
            if (kafkaServer.brokerState().currentState() != (NotRunning.state())) {
                kafkaServer.shutdown();
                kafkaServer.awaitShutdown();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        kafkaServer = null;

        try {
            zkServer.shutdown();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        zkServer = null;

        try {
            logger.warn("Deleting kafka temp dir:{}", tmp.toString());
            Files.walk(tmp).
                    sorted(Comparator.reverseOrder()).
                    map(Path::toFile).
                    forEach(File::delete);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(tmp.getParent())) {
            for (Path path : directoryStream) {
                if (path.toString().startsWith("kafkatest-")) {
                    logger.warn("Deleting kafkatest folder:{}", path.toString());
                    Files.walk(path).
                            sorted(Comparator.reverseOrder()).
                            map(Path::toFile).
                            forEach(File::delete);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(),
                         e);
        }
    }

    @Override
    public void close() {
        shutdownServer();
    }

    private Properties getConsumerConfig() {
        Properties consumerProps = new Properties();
        consumerProps.setProperty("bootstrap.servers", BROKER_HOST + ":" + BROKER_PORT);
        consumerProps.setProperty("group.id", "group0");
        consumerProps.setProperty("client.id", "consumer0");
        consumerProps.put("auto.offset.reset", "earliest");
        consumerProps.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return consumerProps;
    }

    private Properties getProducerConfig() {
        Properties producerProps = new Properties();
        producerProps.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.setProperty("bootstrap.servers", BROKER_HOST + ":" + BROKER_PORT);
        return producerProps;
    }


    public <K, V> KafkaConsumer<K, V> getStringConsumer(String topic) {
        Properties consumerProps = getConsumerConfig();
        consumerProps.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<K, V> consumer = new KafkaConsumer<>(consumerProps);
        consumer.subscribe(Arrays.asList(topic));
        return consumer;
    }


    public KafkaConsumer getConsumer(String topic, Properties props) {
        KafkaConsumer consumer = new KafkaConsumer(props);
        List<PartitionInfo> infos = consumer.partitionsFor(topic);
        List<TopicPartition> partitions = new ArrayList();
        if (infos != null) {
            for (PartitionInfo partition : infos) {
                partitions.add(new TopicPartition(partition.topic(), partition.partition()));
            }
        }
        consumer.assign(partitions);
        Set<TopicPartition> assignments = consumer.assignment();
        assignments.forEach(topicPartition -> consumer.seekToBeginning(assignments));
        return consumer;
    }

    public void insertBatchStockTicketEvent(int items, TopicsConfig topicsConfig, Class sessionType, Listener listener) {
        insertBatchStockTicketEvent(items, topicsConfig, sessionType, Config.getProducerConfig("InsertBatchStockTicketEvent" ), listener);
    }

    public void insertBatchStockTicketEvent(int items, TopicsConfig topicsConfig, Class sessionType, Properties props, Listener listener) {
        if (sessionType.equals(RemoteKieSession.class)) {
            RemoteKieSessionImpl producer = new RemoteKieSessionImpl(props, topicsConfig, listener, InfraFactory.getProducer(false));
            producer.fireUntilHalt();
            try{
                for (int i = 0; i < items; i++) {
                    StockTickEvent ticket = new StockTickEvent("RHT", ThreadLocalRandom.current().nextLong(80, 100));
                    producer.insert(ticket);
                }
            }finally {
                producer.close();
            }

        }
        if (sessionType.equals( RemoteStreamingKieSession.class)) {
            RemoteStreamingKieSessionImpl producer = new RemoteStreamingKieSessionImpl(props, topicsConfig, listener, InfraFactory.getProducer(false));
            producer.fireUntilHalt();
            try {
                for (int i = 0; i < items; i++) {
                    StockTickEvent ticket = new StockTickEvent("RHT", ThreadLocalRandom.current().nextLong(80, 100));
                    producer.insert(ticket);
                }
            }finally {
                producer.close();
            }
        }
    }

    public static void insertSnapshotOnDemandCommand() {
        Properties props = Config.getProducerConfig("insertSnapshotOnDemandCommand");
        Sender sender = new Sender(props, InfraFactory.getProducer(false));
        sender.start();
        SnapshotOnDemandCommand command = new SnapshotOnDemandCommand();
        sender.sendCommand(command, TopicsConfig.getDefaultTopicsConfig().getEventsTopicName());
        sender.stop();
    }

    public static EnvConfig getEnvConfig() {
        return EnvConfig.anEnvConfig().
                withNamespace(CommonConfig.DEFAULT_NAMESPACE).
                withControlTopicName(Config.DEFAULT_CONTROL_TOPIC).
                withEventsTopicName(CommonConfig.DEFAULT_EVENTS_TOPIC).
                withSnapshotTopicName(Config.DEFAULT_SNAPSHOT_TOPIC).
                withKieSessionInfosTopicName(CommonConfig.DEFAULT_KIE_SESSION_INFOS_TOPIC).
                withPrinterType(PrinterKafkaImpl.class.getName()).
                withPollTimeUnit("millisec").
                withPollTimeout("1000").
                withIterationBetweenSnapshot("10").
                skipOnDemandSnapshot("true").
                withMaxSnapshotAgeSeconds("60000").
                withPollSnapshotTimeUnit("sec").
                withPollSnapshotTimeout("10").
                withUpdatableKJar("false").
                underTest(true);
    }

    public void tearDown() {
        kafkaLogger.warn("tearDown");
        try {
            Bootstrap.stopEngine();
        } catch (ConcurrentModificationException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
        kafkaLogger.warn("shutdownServer");
        shutdownServer();
    }
}
