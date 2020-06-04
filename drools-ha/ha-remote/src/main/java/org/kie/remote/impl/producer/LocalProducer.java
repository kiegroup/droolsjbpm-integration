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

package org.kie.remote.impl.producer;

import java.util.Properties;

import org.kie.remote.message.Message;
import org.kie.remote.message.ResultMessage;
import org.kie.remote.util.LocalMessageSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalProducer implements Producer {

    private Logger logger = LoggerFactory.getLogger(LocalProducer.class);
    private final LocalMessageSystem queue = LocalMessageSystem.get();

    @Override
    public void start(Properties properties) { /*do nothing*/}

    @Override
    public void stop() { /*do nothing*/}

    @Override
    public <T> void produceSync(String topicName, String key, ResultMessage<T> object) {
        if(logger.isDebugEnabled()){
            logger.debug("LocalProducer.produceSync topic:{} key:{} ResultMessage:{}", topicName, key,  object);
        }
        queue.put(topicName, object);
    }

    @Override
    public void produceSync(String topicName, String key, Message object) {
        if(logger.isDebugEnabled()){
            logger.debug("LocalProducer.produceSync topic:{} Message:{}", topicName, object);
        }
        queue.put(topicName, object);
    }
}
