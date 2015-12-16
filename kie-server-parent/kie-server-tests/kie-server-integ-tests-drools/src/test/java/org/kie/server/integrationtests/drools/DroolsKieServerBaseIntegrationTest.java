/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.integrationtests.drools;

import java.lang.reflect.Field;

import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

public abstract class DroolsKieServerBaseIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    protected static KieCommands commandsFactory;

    protected RuleServicesClient ruleClient;

    @BeforeClass
    public static void setupFactory() throws Exception {
        commandsFactory = KieServices.Factory.get().getCommands();
    }

    @Override
    protected void setupClients(KieServicesClient kieServicesClient) {
        this.ruleClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
    }

    protected Object valueOf(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            return null;
        }
    }

    protected void setValue(Object object, String fieldName, Object newValue) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, newValue);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Unable to set value to field %s in object %s due " + e.getMessage(), fieldName, object), e);
        }
    }
}
