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

package org.kie.server.client.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.internal.process.CorrelationKey;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskSummaryList;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.api.model.instance.VariableInstanceList;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.QueryServicesClient;

import static org.kie.server.api.rest.RestURI.*;

public class QueryServicesClientImpl extends AbstractKieServicesClientImpl implements QueryServicesClient {

    public QueryServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public QueryServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }

    @Override
    public List<ProcessDefinition> findProcessesById(String processId) {
        ProcessDefinitionList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_ID, processId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_DEFINITIONS_BY_ID_GET_URI, valuesMap), ProcessDefinitionList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesById", new Object[]{processId}) ) );
            ServiceResponse<ProcessDefinitionList> response = (ServiceResponse<ProcessDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getProcesses() != null) {
            return Arrays.asList(result.getProcesses());
        }

        return Collections.emptyList();
    }

    @Override
    public ProcessDefinition findProcessByContainerIdProcessId(String containerId, String processId) {
        ProcessDefinition  result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI, valuesMap), ProcessDefinition.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesByDeploymentIdProcessId", new Object[]{containerId, processId}) ) );
            ServiceResponse<ProcessDefinition> response = (ServiceResponse<ProcessDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        return result;
    }

    @Override
    public List<ProcessDefinition> findProcesses(Integer page, Integer pageSize) {
        ProcessDefinitionList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("", page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_DEFINITIONS_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesByFilter", new Object[]{"", page, pageSize}) ) );
            ServiceResponse<ProcessDefinitionList> response = (ServiceResponse<ProcessDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getProcesses() != null) {
            return Arrays.asList(result.getProcesses());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessDefinition> findProcesses(String filter, Integer page, Integer pageSize) {
        ProcessDefinitionList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("?filter=" + filter, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_DEFINITIONS_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesByFilter", new Object[]{filter, page, pageSize}) ) );
            ServiceResponse<ProcessDefinitionList> response = (ServiceResponse<ProcessDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getProcesses() != null) {
            return Arrays.asList(result.getProcesses());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessDefinition> findProcessesByContainerId(String containerId, Integer page, Integer pageSize) {
        ProcessDefinitionList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String queryString = getPagingQueryString("", page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesByDeploymentId", new Object[]{containerId, page, pageSize}) ) );
            ServiceResponse<ProcessDefinitionList> response = (ServiceResponse<ProcessDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getProcesses() != null) {
            return Arrays.asList(result.getProcesses());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstances(Integer page, Integer pageSize) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("", page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstances", new Object[]{new ArrayList(), "", "", page, pageSize}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByCorrelationKey(CorrelationKey correlationKey, Integer page, Integer pageSize) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CORRELATION_KEY, correlationKey.toExternalForm());

            String queryString = getPagingQueryString("", page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCES_BY_CORRELATION_KEY_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstancesByCorrelationKey", new Object[]{correlationKey.toExternalForm(), page, pageSize}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessId(String processId, List<Integer> status, Integer page, Integer pageSize) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_ID, processId);

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstancesByProcessId", new Object[]{processId, safeList(status), "", page, pageSize}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessName(String processName, List<Integer> status, Integer page, Integer pageSize) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("?processName=" + processName, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstances", new Object[]{safeList(status), "", processName, page, pageSize}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByContainerId(String containerId, List<Integer> status, Integer page, Integer pageSize) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstancesByDeploymentId", new Object[]{containerId, safeList(status), page, pageSize}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByStatus(List<Integer> status, Integer page, Integer pageSize) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstances", new Object[]{safeList(status), "", "", page, pageSize}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByInitiator(String initiator, List<Integer> status, Integer page, Integer pageSize) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("?initiator=" + initiator, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstances", new Object[]{safeList(status), initiator, "", page, pageSize}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByVariable(String variableName, List<Integer> status, Integer page, Integer pageSize) {
        ProcessInstanceList result = null;
        if (config.isRest()) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(VAR_NAME, variableName);

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCE_BY_VAR_NAME_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceByVariables", new Object[]{variableName, "", safeList(status), page, pageSize}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByVariableAndValue(String variableName, String variableValue, List<Integer> status, Integer page, Integer pageSize) {
        ProcessInstanceList result = null;
        if (config.isRest()) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(VAR_NAME, variableName);

            String statusQueryString = getAdditionalParams("?varValue=" + variableValue, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCE_BY_VAR_NAME_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceByVariables", new Object[]{variableName, variableValue, safeList(status), page, pageSize}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public ProcessInstance findProcessInstanceById(Long processInstanceId) {
        ProcessInstance result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI, valuesMap), ProcessInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceById", new Object[]{processInstanceId}) ) );
            ServiceResponse<ProcessInstance> response = (ServiceResponse<ProcessInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        return result;
    }

    @Override
    public ProcessInstance findProcessInstanceByCorrelationKey(CorrelationKey correlationKey) {
        ProcessInstance result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CORRELATION_KEY, correlationKey.toExternalForm());

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI, valuesMap), ProcessInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceByCorrelationKey", new Object[]{correlationKey.toExternalForm()}) ) );
            ServiceResponse<ProcessInstance> response = (ServiceResponse<ProcessInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            return response.getResult();
        }

        return result;
    }

    @Override
    public NodeInstance findNodeInstanceByWorkItemId(Long processInstanceId, Long workItemId) {
        NodeInstance result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(WORK_ITEM_ID, workItemId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI, valuesMap), NodeInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getNodeInstanceForWorkItem", new Object[]{processInstanceId, workItemId}) ) );
            ServiceResponse<NodeInstance> response = (ServiceResponse<NodeInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        return result;
    }

    @Override
    public List<NodeInstance> findActiveNodeInstances(Long processInstanceId, Integer page, Integer pageSize) {
        NodeInstanceList result = null;

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String queryString = getPagingQueryString("?activeOnly=true", page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceHistory", new Object[]{processInstanceId, true, false, page, pageSize}) ) );
            ServiceResponse<NodeInstanceList> response = (ServiceResponse<NodeInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getNodeInstances() != null) {
            return Arrays.asList(result.getNodeInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<NodeInstance> findCompletedNodeInstances(Long processInstanceId, Integer page, Integer pageSize) {
        NodeInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String queryString = getPagingQueryString("?completedOnly=true", page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceHistory", new Object[]{processInstanceId, false, true, page, pageSize}) ) );
            ServiceResponse<NodeInstanceList> response = (ServiceResponse<NodeInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }


        if (result != null && result.getNodeInstances() != null) {
            return Arrays.asList(result.getNodeInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<NodeInstance> findNodeInstances(Long processInstanceId, Integer page, Integer pageSize) {
        NodeInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String queryString = getPagingQueryString("", page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceHistory", new Object[]{processInstanceId, true, true, page, pageSize}) ) );
            ServiceResponse<NodeInstanceList> response = (ServiceResponse<NodeInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getNodeInstances() != null) {
            return Arrays.asList(result.getNodeInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<VariableInstance> findVariablesCurrentState(Long processInstanceId) {
        VariableInstanceList result = null;

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + VAR_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap), VariableInstanceList.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getVariablesCurrentState", new Object[]{processInstanceId}) ) );
            ServiceResponse<VariableInstanceList> response = (ServiceResponse<VariableInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }

        if (result != null && result.getVariableInstances() != null) {
            return Arrays.asList(result.getVariableInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<VariableInstance> findVariableHistory(Long processInstanceId, String variableName, Integer page, Integer pageSize) {
        VariableInstanceList result = null;

        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(VAR_NAME, variableName);

            String queryString = getPagingQueryString("", page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, QUERY_URI + "/" + VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI, valuesMap) + queryString, VariableInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getVariableHistory", new Object[]{processInstanceId, variableName, page, pageSize}) ) );
            ServiceResponse<VariableInstanceList> response = (ServiceResponse<VariableInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);

            result = response.getResult();
        }


        if (result != null && result.getVariableInstances() != null) {
            return Arrays.asList(result.getVariableInstances());
        }

        return Collections.emptyList();
    }


}
