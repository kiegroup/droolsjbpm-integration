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

import org.junit.Test;
import org.kie.hacep.Config;
import org.kie.hacep.util.ConsumerUtilsCore;
import org.kie.remote.message.ControlMessage;

import static org.junit.Assert.*;

public class ConsumerUtilsCoreTest {

    @Test()
    public void lastEventWithoutKafkaUpTest(){
        ConsumerUtilsCore consumerUtilsCore = new ConsumerUtilsCoreImpl();
        ControlMessage msg = consumerUtilsCore.getLastEvent(Config.DEFAULT_CONTROL_TOPIC, 1000);
        assertNotNull(msg);
        assertTrue(msg.getOffset() == 0);
        assertNull(msg.getSideEffects());
        assertNull(msg.getId());
        assertTrue(msg.getTimestamp() == 0);
    }
}
