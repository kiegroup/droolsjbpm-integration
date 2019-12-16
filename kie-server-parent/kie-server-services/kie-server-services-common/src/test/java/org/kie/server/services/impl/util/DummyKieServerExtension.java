/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.Message;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;


public class DummyKieServerExtension implements KieServerExtension {

    private List<Message> messages;

    public DummyKieServerExtension() {
        this.messages = new ArrayList<>();
    }

    public void clear() {
        this.messages.clear();
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    @Override
    public List<Message> healthCheck(boolean report) {
        return KieServerExtension.super.healthCheck(report);
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return false;
    }

    @Override
    public boolean isInitialized() {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
    }

    @Override
    public Integer getStartOrder() {
        return 10;
    }

    @Override
    public List<Object> getServices() {
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return "TEST";
    }

    @Override
    public String getExtensionName() {
        return "TEST";
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        return null;
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        return null;
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        ((List) parameters.get(KieServerConstants.KIE_SERVER_PARAM_MESSAGES)).addAll(messages);
    }
}
