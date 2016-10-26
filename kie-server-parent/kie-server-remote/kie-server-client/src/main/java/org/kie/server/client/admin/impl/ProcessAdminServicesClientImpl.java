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
import org.kie.server.api.model.admin.ProcessNode;
import org.kie.server.api.model.admin.ProcessNodeList;
import org.kie.server.api.model.admin.TimerInstance;
import org.kie.server.api.model.admin.TimerInstanceList;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
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
                    build(loadBalancer.getUrl(), ADMIN_PROCESS_URI + "/" + MIGRATE_PROCESS_INST_PUT_URI, valuesMap) + queryString, nodeMapping, MigrationReportInstance.class, headers);
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
                    build(loadBalancer.getUrl(), ADMIN_PROCESS_URI + "/" + MIGRATE_PROCESS_INSTANCES_PUT_URI, valuesMap) + queryString, nodeMapping, MigrationReportInstanceList.class, headers);
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

    @Override
    public List<ProcessNode> getProcessNodes(String containerId, Long processInstanceId) {
        ProcessNodeList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_PROCESS_URI + "/" + NODES_PROCESS_INST_GET_URI, valuesMap), ProcessNodeList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "ProcessAdminService", "getProcessNodes", new Object[]{containerId, processInstanceId}) ) );
            ServiceResponse<ProcessNodeList> response = (ServiceResponse<ProcessNodeList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getItems() != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public void cancelNodeInstance(String containerId, Long processInstanceId, Long nodeInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(NODE_INSTANCE_ID, nodeInstanceId);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_PROCESS_URI + "/" + CANCEL_NODE_INST_PROCESS_INST_DELETE_URI, valuesMap), null);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessAdminService", "cancelNodeInstance", new Object[]{containerId, processInstanceId, nodeInstanceId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void retriggerNodeInstance(String containerId, Long processInstanceId, Long nodeInstanceId) {

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(NODE_INSTANCE_ID, nodeInstanceId);

            Map<String, String> headers = new HashMap<String, String>();


            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_PROCESS_URI + "/" + RETRIGGER_NODE_INST_PROCESS_INST_PUT_URI, valuesMap), null, null, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessAdminService", "retriggerNodeInstance", new Object[]{containerId, processInstanceId, nodeInstanceId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public List<NodeInstance> getActiveNodeInstances(String containerId, Long processInstanceId) {
        NodeInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_PROCESS_URI + "/" + NODE_INSTANCES_PROCESS_INST_GET_URI, valuesMap), NodeInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "ProcessAdminService", "getActiveNodeInstances", new Object[]{containerId, processInstanceId}) ) );
            ServiceResponse<NodeInstanceList> response = (ServiceResponse<NodeInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getItems() != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public void updateTimer(String containerId, Long processInstanceId, long timerId, long delay, long period, int repeatLimit) {
        updateTimer(containerId, processInstanceId, timerId, delay, period, repeatLimit, false);
    }

    @Override
    public void updateTimerRelative(String containerId, Long processInstanceId, long timerId, long delay, long period, int repeatLimit) {
        updateTimer(containerId, processInstanceId, timerId, delay, period, repeatLimit, true);
    }

    @Override
    public List<TimerInstance> getTimerInstances(String containerId, Long processInstanceId) {
        TimerInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_PROCESS_URI + "/" + TIMERS_PROCESS_INST_GET_URI, valuesMap), TimerInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "ProcessAdminService", "getTimerInstances", new Object[]{containerId, processInstanceId}) ) );
            ServiceResponse<TimerInstanceList> response = (ServiceResponse<TimerInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getItems() != null) {
            return result.getItems();
        }

        return Collections.emptyList();
    }

    @Override
    public void triggerNode(String containerId, Long processInstanceId, Long nodeId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(NODE_ID, nodeId);

            Map<String, String> headers = new HashMap<String, String>();


            makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_PROCESS_URI + "/" + TRIGGER_NODE_PROCESS_INST_POST_URI, valuesMap), null, null, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessAdminService", "triggerNode", new Object[]{containerId, processInstanceId, nodeId})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    /*
     * helper methods
     */
    protected void updateTimer(String containerId, Long processInstanceId, long timerId, long delay, long period, int repeatLimit, boolean relative) {
        Map<String, Number> timerUpdate = new HashMap<>();
        timerUpdate.put("delay", delay);
        timerUpdate.put("period", period);
        timerUpdate.put("repeatLimit", repeatLimit);

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(TIMER_INSTANCE_ID, timerId);

            Map<String, String> headers = new HashMap<String, String>();


            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), ADMIN_PROCESS_URI + "/" + UPDATE_TIMER_PROCESS_INST_PUT_URI, valuesMap)+ "?relative=" + relative, timerUpdate, null, headers);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "ProcessAdminService", "updateTimer", serialize(timerUpdate), marshaller.getFormat().getType(), new Object[]{containerId, processInstanceId, timerId, relative})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM", containerId ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }
}
