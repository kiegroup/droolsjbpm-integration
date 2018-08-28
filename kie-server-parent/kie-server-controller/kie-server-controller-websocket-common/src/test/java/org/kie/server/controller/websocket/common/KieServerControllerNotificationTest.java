/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.controller.websocket.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.controller.api.model.events.*;
import org.kie.server.controller.api.model.notification.KieServerControllerNotification;
import org.kie.server.controller.api.model.runtime.ServerInstance;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.websocket.common.decoder.KieServerControllerNotificationDecoder;
import org.kie.server.controller.websocket.common.encoder.KieServerControllerNotificationEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class KieServerControllerNotificationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieServerControllerNotificationTest.class);

    @Parameterized.Parameter
    public KieServerControllerEvent event;

    private KieServerControllerNotificationDecoder decoder = new KieServerControllerNotificationDecoder();
    private KieServerControllerNotificationEncoder encoder = new KieServerControllerNotificationEncoder();

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]{
                                                                                           {new ServerInstanceConnected(new ServerInstance())},
                                                                                           {new ServerInstanceDisconnected("serverId")},
                                                                                           {new ServerInstanceDeleted("serverId")},
                                                                                           {new ServerInstanceUpdated(new ServerInstance())},
                                                                                           {new ServerTemplateUpdated(new ServerTemplate())},
                                                                                           {new ServerTemplateDeleted("serverTemplateId")},
                                                                                           {new ContainerSpecUpdated(new ServerTemplate(),
                                                                                                                     new ContainerSpec(),
                                                                                                                     new ArrayList<>())}
                                                                                   }
        ));

        return parameterData;
    }

    @Test
    public void testEventsEncodeDecode() throws Exception {
        final KieServerControllerNotification notification = new KieServerControllerNotification(event);

        final String json = encoder.encode(notification);
        LOGGER.info("JSON content\n{}",
                    json);

        assertEquals(notification,
                     decoder.decode(json));
    }
}
