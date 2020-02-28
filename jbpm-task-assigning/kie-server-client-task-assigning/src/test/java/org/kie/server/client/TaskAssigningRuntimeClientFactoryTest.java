/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.client;

import org.junit.Test;
import org.kie.server.client.impl.TaskAssigningRuntimeClientImpl;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.kie.server.api.KieServerConstants.CFG_BYPASS_AUTH_USER;

public class TaskAssigningRuntimeClientFactoryTest {

    private static final String ENDPOINT = "ENDPOINT";
    private static final String USER = "USER";
    private static final String PWD = "PWD";
    private static final long TIMEOUT = 1234L;

    @Test
    public void newRuntimeClient() {
        TaskAssigningRuntimeClient client = TaskAssigningRuntimeClientFactory.newRuntimeClient(ENDPOINT, USER, PWD, TIMEOUT);
        assertTrue(client instanceof TaskAssigningRuntimeClientImpl);
        TaskAssigningRuntimeClientImpl clientImpl = (TaskAssigningRuntimeClientImpl) client;
        KieServicesConfiguration config = clientImpl.getConfig();
        assertEquals(ENDPOINT, config.getServerUrl());
        assertEquals(USER, config.getUserName());
        assertEquals(PWD, config.getPassword());
        assertEquals(TIMEOUT, config.getTimeout(), 0);
        assertEquals("true", System.getProperty(CFG_BYPASS_AUTH_USER));
    }
}
