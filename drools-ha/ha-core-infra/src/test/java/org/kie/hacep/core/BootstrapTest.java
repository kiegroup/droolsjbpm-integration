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
package org.kie.hacep.core;

import org.junit.Test;
import org.kie.hacep.EnvConfig;
import org.kie.hacep.core.infra.election.State;

import static org.junit.Assert.*;

public class BootstrapTest {

    @Test
    public void startUnderTestTest(){
        EnvConfig envConfig = EnvConfig.getDefaultEnvConfig();
        envConfig.underTest(true);
        envConfig.local(true);
        Bootstrap.startEngine(envConfig);
        Bootstrap.getConsumerController().getCallback().updateStatus(State.LEADER);
        assertNotNull(Bootstrap.getConsumerController());
    }

    @Test
    public void startTest(){
        EnvConfig envConfig = EnvConfig.getDefaultEnvConfig();
        envConfig.underTest(false);
        envConfig.local(true);
        Bootstrap.startEngine(envConfig);
        Bootstrap.getConsumerController().getCallback().updateStatus(State.LEADER);
        assertNotNull(Bootstrap.getConsumerController());
    }

    @Test
    public void stopUnderTestTest(){
        EnvConfig envConfig = EnvConfig.getDefaultEnvConfig();
        envConfig.underTest(true);
        envConfig.local(true);
        Bootstrap.startEngine(envConfig);
        Bootstrap.getConsumerController().getCallback().updateStatus(State.LEADER);
        assertNotNull(Bootstrap.getConsumerController());
        Bootstrap.stopEngine();
        assertNull(Bootstrap.getConsumerController());
    }

    @Test
    public void stopTest(){
        EnvConfig envConfig = EnvConfig.getDefaultEnvConfig();
        envConfig.underTest(false);
        envConfig.local(true);
        Bootstrap.startEngine(envConfig);
        Bootstrap.getConsumerController().getCallback().updateStatus(State.LEADER);
        assertNotNull(Bootstrap.getConsumerController());
        Bootstrap.stopEngine();
        assertNull(Bootstrap.getConsumerController());
    }

}
