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

import org.junit.After;
import org.junit.Before;
import org.kie.remote.TopicsConfig;

public class KafkaFullTopicsTests {

    protected final String TEST_KAFKA_LOGGER_TOPIC = "testlogs";
    protected KafkaUtils kafkaServerTest;
    protected EnvConfig envConfig;
    protected TopicsConfig topicsConfig;

    @Before
    public void setUp() throws Exception {
        envConfig = KafkaUtils.getEnvConfig();
        topicsConfig = TopicsConfig.getDefaultTopicsConfig();
        kafkaServerTest = new KafkaUtils();
        kafkaServerTest.startServer();
    }

    @After
    public void tearDown() {
        kafkaServerTest.tearDown();
    }
}
