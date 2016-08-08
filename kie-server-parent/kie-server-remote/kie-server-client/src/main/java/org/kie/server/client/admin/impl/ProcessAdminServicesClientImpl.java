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

package org.kie.server.client.admin.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.admin.MigrationReportInstance;
import org.kie.server.api.model.admin.MigrationReportInstanceList;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.admin.ProcessAdminServicesClient;
import org.kie.server.client.impl.AbstractKieServicesClientImpl;

import static org.kie.server.api.rest.RestURI.*;

public class ProcessAdminServicesClientImpl extends AbstractKieServicesClientImpl implements ProcessAdminServicesClient {

    public ProcessAdminServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public ProcessAdminServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public MigrationReportInstance migrateProcessInstance(String containerId, Long processInstanceId, String targetContainerId, String targetProcessId) {
        return migrateProcessInstance(containerId, processInstanceId, targetContainerId, targetProcessId, new HashMap<String, String>());
    }

    @Override
    public MigrationReportInstance migrateProcessInstance(String containerId, Long processInstanceId, String targetContainerId, String targetProcessId, Map<String, String> nodeMapping) {
        MigrationReportInstance reportInstance = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            Map<String, String> headers = new HashMap<String, String>();

            String queryString = "?targetContainerId=" + targetContainerId + "&targetProcessId=" + targetProcessId;

            reportInstance = makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, ADMIN_PROCESS_URI + "/" + MIGRATE_PROCESS_INST_PUT_URI, valuesMap) + queryString, nodeMapping, MigrationReportInstance.class, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessAdminService", "migrateProcessInstance", serialize(safeMap(nodeMapping)), marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId, targetContainerId, targetProcessId})));
            ServiceResponse<MigrationReportInstance> response = (ServiceResponse<MigrationReportInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            reportInstance = response.getResult();
        }

        return reportInstance;
    }

    @Override
    public List<MigrationReportInstance> migrateProcessInstances(String containerId, List<Long> processInstancesId, String targetContainerId, String targetProcessId) {
        return migrateProcessInstances(containerId, processInstancesId, targetContainerId, targetProcessId, new HashMap<String, String>());
    }

    @Override
    public List<MigrationReportInstance> migrateProcessInstances(String containerId, List<Long> processInstancesId, String targetContainerId, String targetProcessId, Map<String, String> nodeMapping) {
        MigrationReportInstanceList reportInstanceList = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            Map<String, String> headers = new HashMap<String, String>();
            String queryStringBase = buildQueryString("pInstanceId", processInstancesId);
            String queryString = queryStringBase + "&targetContainerId=" + targetContainerId + "&targetProcessId=" + targetProcessId;

            reportInstanceList = makeHttpPutRequestAndCreateCustomResponse(
                    build(baseURI, ADMIN_PROCESS_URI + "/" + MIGRATE_PROCESS_INSTANCES_PUT_URI, valuesMap) + queryString, nodeMapping, MigrationReportInstanceList.class, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessAdminService", "migrateProcessInstances", serialize(safeMap(nodeMapping)), marshaller.getFormat().getType(), new Object[]{containerId, processInstancesId, targetContainerId, targetProcessId})));
            ServiceResponse<MigrationReportInstanceList> response = (ServiceResponse<MigrationReportInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            reportInstanceList = response.getResult();
        }
        if (reportInstanceList != null) {
            return reportInstanceList.getItems();
        }
        return Collections.emptyList();
    }
}
