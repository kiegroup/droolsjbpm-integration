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

package org.kie.processmigration.service;

import java.util.Map;

import org.kie.processmigration.model.KieServerConfig;
import org.kie.processmigration.model.exceptions.InvalidKieServerException;
import org.kie.server.client.QueryServicesClient;
import org.kie.server.client.admin.ProcessAdminServicesClient;

public interface KieService {

    ProcessAdminServicesClient getProcessAdminServicesClient(String kieServerId) throws InvalidKieServerException;

    QueryServicesClient getQueryServicesClient(String kieserverId) throws InvalidKieServerException;

    Map<String, KieServerConfig> getConfigs();

    boolean existsProcessDefinition(String containerId, String processId, String kieServerId) throws InvalidKieServerException;

}
