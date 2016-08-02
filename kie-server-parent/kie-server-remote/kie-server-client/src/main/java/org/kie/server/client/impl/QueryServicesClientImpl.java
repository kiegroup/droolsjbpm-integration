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
import org.kie.server.api.model.ItemList;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.definition.QueryDefinitionList;
import org.kie.server.api.model.definition.QueryFilterSpec;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
import org.kie.server.api.model.instance.TaskInstance;
import org.kie.server.api.model.instance.TaskInstanceList;
import org.kie.server.api.model.instance.TaskSummary;
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
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_DEFINITIONS_BY_ID_GET_URI, valuesMap), ProcessDefinitionList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesById", new Object[]{processId}) ) );
            ServiceResponse<ProcessDefinitionList> response = (ServiceResponse<ProcessDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getProcesses() != null) {
            return Arrays.asList(result.getProcesses());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessDefinition> findProcesses(Integer page, Integer pageSize) {
        return findProcesses(page, pageSize, "", true);
    }

    @Override
    public List<ProcessDefinition> findProcesses(String filter, Integer page, Integer pageSize) {
        return findProcesses(filter, page, pageSize, "", true);
    }

    @Override
    public List<ProcessDefinition> findProcessesByContainerId(String containerId, Integer page, Integer pageSize) {
        return findProcessesByContainerId(containerId, page, pageSize, "", true);
    }

    @Override
    public ProcessDefinition findProcessByContainerIdProcessId(String containerId, String processId) {
        ProcessDefinition  result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI, valuesMap), ProcessDefinition.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesByDeploymentIdProcessId", new Object[]{containerId, processId}) ) );
            ServiceResponse<ProcessDefinition> response = (ServiceResponse<ProcessDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        return result;
    }

    @Override
    public List<ProcessDefinition> findProcesses(Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessDefinitionList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("?sort="+sort+"&sortOrder="+sortOrder, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_DEFINITIONS_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesByFilter", new Object[]{"", page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessDefinitionList> response = (ServiceResponse<ProcessDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getProcesses() != null) {
            return Arrays.asList(result.getProcesses());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessDefinition> findProcesses(String filter, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessDefinitionList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("?filter=" + filter+"&sort="+sort+"&sortOrder="+sortOrder, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_DEFINITIONS_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesByFilter", new Object[]{filter, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessDefinitionList> response = (ServiceResponse<ProcessDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getProcesses() != null) {
            return Arrays.asList(result.getProcesses());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessDefinition> findProcessesByContainerId(String containerId, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessDefinitionList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String queryString = getPagingQueryString("?sort="+sort+"&sortOrder="+sortOrder, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessesByDeploymentId", new Object[]{containerId, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessDefinitionList> response = (ServiceResponse<ProcessDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getProcesses() != null) {
            return Arrays.asList(result.getProcesses());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstances(Integer page, Integer pageSize) {
        return findProcessInstances(page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByCorrelationKey(CorrelationKey correlationKey, Integer page, Integer pageSize) {
        return findProcessInstancesByCorrelationKey(correlationKey, page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessId(String processId, List<Integer> status, Integer page, Integer pageSize) {
        return findProcessInstancesByProcessId(processId, status, page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessName(String processName, List<Integer> status, Integer page, Integer pageSize) {
        return findProcessInstancesByProcessName(processName, status, page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByContainerId(String containerId, List<Integer> status, Integer page, Integer pageSize) {
        return findProcessInstancesByContainerId(containerId, status, page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByStatus(List<Integer> status, Integer page, Integer pageSize) {
        return findProcessInstancesByStatus(status, page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByInitiator(String initiator, List<Integer> status, Integer page, Integer pageSize) {
        return findProcessInstancesByInitiator(initiator, status, page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByVariable(String variableName, List<Integer> status, Integer page, Integer pageSize) {
        return findProcessInstancesByVariable(variableName, status, page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByVariableAndValue(String variableName, String variableValue, List<Integer> status, Integer page, Integer pageSize) {
        return findProcessInstancesByVariableAndValue(variableName, variableValue, status, page, pageSize, "", true);
    }

    @Override
    public List<ProcessInstance> findProcessInstances(Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("?sort="+sort+"&sortOrder="+sortOrder, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstances", new Object[]{new ArrayList(), "", "", page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByCorrelationKey(CorrelationKey correlationKey, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CORRELATION_KEY, correlationKey.toExternalForm());

            String queryString = getPagingQueryString("?sort="+sort+"&sortOrder="+sortOrder, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCES_BY_CORRELATION_KEY_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstancesByCorrelationKey", new Object[]{correlationKey.toExternalForm(), page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessId(String processId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_ID, processId);

            String statusQueryString = getAdditionalParams("?sort="+sort+"&sortOrder="+sortOrder, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstancesByProcessId", new Object[]{processId, safeList(status), "", page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessName(String processName, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("?processName=" + processName+"&sort="+sort+"&sortOrder="+sortOrder, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstances", new Object[]{safeList(status), "", processName, page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByContainerId(String containerId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String statusQueryString = getAdditionalParams("?sort="+sort+"&sortOrder="+sortOrder, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstancesByDeploymentId", new Object[]{containerId, safeList(status), page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByStatus(List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("?sort="+sort+"&sortOrder="+sortOrder, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstances", new Object[]{safeList(status), "", "", page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByInitiator(String initiator, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("?initiator=" + initiator+"&sort="+sort+"&sortOrder="+sortOrder, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstances", new Object[]{safeList(status), initiator, "", page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByVariable(String variableName, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList result = null;
        if (config.isRest()) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(VAR_NAME, variableName);

            String statusQueryString = getAdditionalParams("?sort="+sort+"&sortOrder="+sortOrder, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCE_BY_VAR_NAME_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceByVariables", new Object[]{variableName, "", safeList(status), page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        if (result != null && result.getProcessInstances() != null) {
            return Arrays.asList(result.getProcessInstances());
        }

        return Collections.emptyList();
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByVariableAndValue(String variableName, String variableValue, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder) {
        ProcessInstanceList result = null;
        if (config.isRest()) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(VAR_NAME, variableName);

            String statusQueryString = getAdditionalParams("?varValue=" + variableValue+"&sort="+sort+"&sortOrder="+sortOrder, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCE_BY_VAR_NAME_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceByVariables", new Object[]{variableName, variableValue, safeList(status), page, pageSize, sort, sortOrder}) ) );
            ServiceResponse<ProcessInstanceList> response = (ServiceResponse<ProcessInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
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
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI, valuesMap), ProcessInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceById", new Object[]{processInstanceId}) ) );
            ServiceResponse<ProcessInstance> response = (ServiceResponse<ProcessInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        return result;
    }

    @Override
    public ProcessInstance findProcessInstanceById(Long processInstanceId, boolean withVars) {
        ProcessInstance result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI, valuesMap) + "?withVars=" + withVars, ProcessInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceById", new Object[]{processInstanceId, withVars}) ) );
            ServiceResponse<ProcessInstance> response = (ServiceResponse<ProcessInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
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
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI, valuesMap), ProcessInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceByCorrelationKey", new Object[]{correlationKey.toExternalForm()}) ) );
            ServiceResponse<ProcessInstance> response = (ServiceResponse<ProcessInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
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
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI, valuesMap), NodeInstance.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getNodeInstanceForWorkItem", new Object[]{processInstanceId, workItemId}) ) );
            ServiceResponse<NodeInstance> response = (ServiceResponse<NodeInstance>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
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
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceHistory", new Object[]{processInstanceId, true, false, page, pageSize}) ) );
            ServiceResponse<NodeInstanceList> response = (ServiceResponse<NodeInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
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
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceHistory", new Object[]{processInstanceId, false, true, page, pageSize}) ) );
            ServiceResponse<NodeInstanceList> response = (ServiceResponse<NodeInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
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
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getProcessInstanceHistory", new Object[]{processInstanceId, true, true, page, pageSize}) ) );
            ServiceResponse<NodeInstanceList> response = (ServiceResponse<NodeInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
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
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + VAR_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap), VariableInstanceList.class);



        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getVariablesCurrentState", new Object[]{processInstanceId}) ) );
            ServiceResponse<VariableInstanceList> response = (ServiceResponse<VariableInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
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
                    build(loadBalancer.getUrl(), QUERY_URI + "/" + VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI, valuesMap) + queryString, VariableInstanceList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryService", "getVariableHistory", new Object[]{processInstanceId, variableName, page, pageSize}) ) );
            ServiceResponse<VariableInstanceList> response = (ServiceResponse<VariableInstanceList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null && result.getVariableInstances() != null) {
            return Arrays.asList(result.getVariableInstances());
        }

        return Collections.emptyList();
    }

    // QueryDataService related
    @Override
    public void registerQuery(QueryDefinition queryDefinition) {
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(QUERY_NAME, queryDefinition.getName());

            makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_DEF_URI + "/" + CREATE_QUERY_DEF_POST_URI, valuesMap), queryDefinition,
                    Object.class);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "QueryDataService", "registerQuery", serialize(queryDefinition), marshaller.getFormat().getType(), new Object[]{queryDefinition.getName()}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void replaceQuery(QueryDefinition queryDefinition) {
        if( config.isRest() ) {

            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(QUERY_NAME, queryDefinition.getName());

            makeHttpPutRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_DEF_URI + "/" + REPLACE_QUERY_DEF_PUT_URI, valuesMap), queryDefinition,
                    Object.class, new HashMap<String, String>());

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "QueryDataService", "replaceQuery", serialize(queryDefinition), marshaller.getFormat().getType(), new Object[]{queryDefinition.getName()}) ) );
            ServiceResponse<String> response = (ServiceResponse<String>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
        }
    }

    @Override
    public void unregisterQuery(String queryName) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(QUERY_NAME, queryName);

            makeHttpDeleteRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_DEF_URI + "/" + DROP_QUERY_DEF_DELETE_URI, valuesMap),
                    null);

        } else {
            CommandScript script = new CommandScript( Collections.singletonList(
                    (KieServerCommand) new DescriptorCommand( "QueryDataService", "unregisterQuery", new Object[]{queryName})));
            ServiceResponse<?> response = (ServiceResponse<?>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);
            throwExceptionOnFailure(response);
        }
    }

    @Override
    public QueryDefinition getQuery(String queryName) {
        QueryDefinition result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(QUERY_NAME, queryName);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_DEF_URI + "/" + QUERY_DEF_GET_URI, valuesMap), QueryDefinition.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryDataService", "getQuery", new Object[]{queryName}) ) );
            ServiceResponse<QueryDefinition> response = (ServiceResponse<QueryDefinition>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }

        return result;
    }

    @Override
    public List<QueryDefinition> getQueries(Integer page, Integer pageSize) {
        QueryDefinitionList result = null;
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("", page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_DEF_URI, valuesMap) + queryString, QueryDefinitionList.class);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryDataService", "getQueries", new Object[]{page, pageSize}) ) );
            ServiceResponse<QueryDefinitionList> response = (ServiceResponse<QueryDefinitionList>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null && result.getQueries() != null) {
            return Arrays.asList(result.getQueries());
        }

        return Collections.emptyList();
    }

    @Override
    public <T> List<T> query(String queryName, String mapper, String orderBy, Integer page, Integer pageSize, Class<T> resultType) {
        Object result = null;
        Class<?> resultTypeList = getResultTypeList(resultType);
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(QUERY_NAME, queryName);

            String queryString = getPagingQueryString("?mapper="+mapper+"&orderBy="+orderBy, page, pageSize);

            result = makeHttpGetRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_DEF_URI + "/" + RUN_QUERY_DEF_GET_URI, valuesMap) + queryString, resultTypeList);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryDataService", "query", new Object[]{queryName, mapper, orderBy, page, pageSize}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null) {

            if (result instanceof ItemList) {
                return ((ItemList<T>) result).getItems();
            } else if (result instanceof List) {
                return (List) result;
            } else if (result instanceof Wrapped) {
                return (List)((Wrapped) result).unwrap();
            }
        }

        return null;
    }

    @Override
    public <T> List<T> query(String queryName, String mapper, Integer page, Integer pageSize, Class<T> resultType) {

        return query(queryName, mapper, "", page, pageSize, resultType);
    }

    @Override
    public <T> List<T> query(String queryName, String mapper, QueryFilterSpec filterSpec, Integer page, Integer pageSize, Class<T> resultType) {
        Object result = null;
        Class<?> resultTypeList = getResultTypeList(resultType);
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(QUERY_NAME, queryName);

            String queryString = getPagingQueryString("?mapper="+mapper, page, pageSize);

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_DEF_URI + "/" + RUN_FILTERED_QUERY_DEF_POST_URI, valuesMap) + queryString, filterSpec, resultTypeList);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryDataService", "queryFiltered", serialize(filterSpec), marshaller.getFormat().getType(), new Object[]{queryName, mapper, page, pageSize}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null) {

            if (result instanceof ItemList) {
                return ((ItemList<T>) result).getItems();
            } else if (result instanceof List) {
                return (List) result;
            } else if (result instanceof Wrapped) {
                return (List)((Wrapped) result).unwrap();
            }
        }

        return Collections.emptyList();
    }

    @Override
    public <T> List<T> query(String queryName, String mapper, String builder, Map<String, Object> parameters, Integer page, Integer pageSize, Class<T> resultType) {
        Object result = null;
        Class<?> resultTypeList = getResultTypeList(resultType);
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(QUERY_NAME, queryName);

            String queryString = getPagingQueryString("?mapper="+mapper+"&builder="+builder, page, pageSize);

            result = makeHttpPostRequestAndCreateCustomResponse(
                    build(loadBalancer.getUrl(), QUERY_DEF_URI + "/" + RUN_FILTERED_QUERY_DEF_POST_URI, valuesMap) + queryString, parameters, resultTypeList);


        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand)
                    new DescriptorCommand( "QueryDataService", "queryFilteredWithBuilder", serialize(safeMap(parameters)),
                            marshaller.getFormat().getType(), new Object[]{queryName, mapper, builder, page, pageSize}) ) );
            ServiceResponse<Object> response = (ServiceResponse<Object>) executeJmsCommand( script, DescriptorCommand.class.getName(), "BPM" ).getResponses().get(0);

            throwExceptionOnFailure(response);
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            result = response.getResult();
        }


        if (result != null) {

            if (result instanceof ItemList) {
                return ((ItemList<T>) result).getItems();
            } else if (result instanceof List) {
                return (List) result;
            } else if (result instanceof Wrapped) {
                return (List)((Wrapped) result).unwrap();
            }
        }

        return Collections.emptyList();
    }

    protected Class<?> getResultTypeList(Class<?> resultType) {
        if (TaskSummary.class.isAssignableFrom(resultType)) {
            return TaskSummaryList.class;
        } else if (ProcessInstance.class.isAssignableFrom(resultType)) {
            return ProcessInstanceList.class;
        } else if (TaskInstance.class.isAssignableFrom(resultType)) {
            return TaskInstanceList.class;
        } else {
            return Object.class;
        }
    }

}
