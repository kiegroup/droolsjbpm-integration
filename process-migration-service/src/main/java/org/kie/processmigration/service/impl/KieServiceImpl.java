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

package org.kie.processmigration.service.impl;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.processmigration.model.KieServerConfig;
import org.kie.processmigration.service.KieService;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.CredentialsProvider;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.kie.server.client.admin.impl.ProcessAdminServicesClientImpl;
import org.kie.server.client.helper.JBPMServicesClientBuilder;
import org.kie.server.client.impl.KieServicesClientImpl;

@ApplicationScoped
public class KieServiceImpl implements KieService {

    @Inject
    private KieServerConfig config;

    public ServiceResponse<KieServerStateInfo> getServerState(CredentialsProvider credentialsProvider) {
        try {
            return createKieServicesClient(credentialsProvider).getServerState();
        } catch (Exception e) {
            ServiceResponse<KieServerStateInfo> response = new ServiceResponse<>();
            response.setType(ResponseType.FAILURE);
            response.setMsg(e.getMessage());
            return response;
        }
    }

    public ProcessAdminServicesClient createProcessAdminServicesClient(CredentialsProvider credentialsProvider) {
        Map<Class<?>, Object> services = createServices(credentialsProvider);
        ProcessAdminServicesClientImpl processAdminServicesClient = (ProcessAdminServicesClientImpl) services.get(ProcessAdminServicesClient.class);
        processAdminServicesClient.setOwner((KieServicesClientImpl) createKieServicesClient(credentialsProvider));
        return processAdminServicesClient;
    }

    public KieServicesClient createKieServicesClient(CredentialsProvider credentialsProvider) {
        return KieServicesFactory.newKieServicesRestClient(config.getUrl(), credentialsProvider);
    }

    private Map<Class<?>, Object> createServices(CredentialsProvider credentialsProvider) {
        return new JBPMServicesClientBuilder().build(KieServicesFactory.newRestConfiguration(config.getUrl(), credentialsProvider), this.getClass().getClassLoader());
    }

}
