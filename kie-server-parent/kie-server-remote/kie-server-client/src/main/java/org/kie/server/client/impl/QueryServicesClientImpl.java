/*
 * Copyright 2015 JBoss Inc
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.internal.process.CorrelationKey;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ProcessDefinitionList;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.NodeInstanceList;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.ProcessInstanceList;
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
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_ID, processId);

            ProcessDefinitionList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_BY_ID_GET_URI, valuesMap), ProcessDefinitionList.class);

            if (result != null && result.getProcesses() != null) {
                return Arrays.asList(result.getProcesses());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ProcessDefinition findProcessByContainerIdProcessId(String containerId, String processId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);
            valuesMap.put(PROCESS_ID, processId);

            ProcessDefinition result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_BY_CONTAINER_ID_DEF_ID_GET_URI, valuesMap), ProcessDefinition.class);

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessDefinition> findProcesses(Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("", page, pageSize);

            ProcessDefinitionList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

            if (result != null && result.getProcesses() != null) {
                return Arrays.asList(result.getProcesses());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessDefinition> findProcesses(String filter, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("?filter=" + filter, page, pageSize);

            ProcessDefinitionList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

            if (result != null && result.getProcesses() != null) {
                return Arrays.asList(result.getProcesses());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessDefinition> findProcessesByContainerId(String containerId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String queryString = getPagingQueryString("", page, pageSize);

            ProcessDefinitionList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_DEFINITIONS_BY_CONTAINER_ID_GET_URI, valuesMap) + queryString, ProcessDefinitionList.class);

            if (result != null && result.getProcesses() != null) {
                return Arrays.asList(result.getProcesses());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstances(Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String queryString = getPagingQueryString("", page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessId(String processId, List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_ID, processId);

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_BY_PROCESS_ID_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByProcessName(String processName, List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("?processName=" + processName, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByContainerId(String containerId, List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CONTAINER_ID, containerId);

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_BY_CONTAINER_ID_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByStatus(List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("", "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<ProcessInstance> findProcessInstancesByInitiator(String initiator, List<Integer> status, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();

            String statusQueryString = getAdditionalParams("?initiator=" + initiator, "status", status);
            String queryString = getPagingQueryString(statusQueryString, page, pageSize);

            ProcessInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCES_GET_URI, valuesMap) + queryString, ProcessInstanceList.class);

            if (result != null && result.getProcessInstances() != null) {
                return Arrays.asList(result.getProcessInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ProcessInstance findProcessInstanceById(Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            ProcessInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_BY_INSTANCE_ID_GET_URI, valuesMap), ProcessInstance.class);

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ProcessInstance findProcessInstanceByCorrelationKey(CorrelationKey correlationKey) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(CORRELATION_KEY, correlationKey.toExternalForm());

            ProcessInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, PROCESS_INSTANCE_BY_CORRELATION_KEY_GET_URI, valuesMap), ProcessInstance.class);

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public NodeInstance findNodeInstanceByWorkItemId(Long processInstanceId, Long workItemId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(WORK_ITEM_ID, workItemId);

            NodeInstance result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, NODE_INSTANCES_BY_WORK_ITEM_ID_GET_URI, valuesMap), NodeInstance.class);

            return result;

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<NodeInstance> findActiveNodeInstances(Long processInstanceId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String queryString = getPagingQueryString("?activeOnly=true", page, pageSize);

            NodeInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);

            if (result != null && result.getNodeInstances() != null) {
                return Arrays.asList(result.getNodeInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<NodeInstance> findCompletedNodeInstances(Long processInstanceId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String queryString = getPagingQueryString("?completedOnly=true", page, pageSize);

            NodeInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);

            if (result != null && result.getNodeInstances() != null) {
                return Arrays.asList(result.getNodeInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<NodeInstance> findNodeInstances(Long processInstanceId, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            String queryString = getPagingQueryString("", page, pageSize);

            NodeInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, NODE_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap) + queryString, NodeInstanceList.class);

            if (result != null && result.getNodeInstances() != null) {
                return Arrays.asList(result.getNodeInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<VariableInstance> findVariablesCurrentState(Long processInstanceId) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);

            VariableInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, VAR_INSTANCES_BY_INSTANCE_ID_GET_URI, valuesMap), VariableInstanceList.class);

            if (result != null && result.getVariableInstances() != null) {
                return Arrays.asList(result.getVariableInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public List<VariableInstance> findVariableHistory(Long processInstanceId, String variableName, Integer page, Integer pageSize) {
        if( config.isRest() ) {
            Map<String, Object> valuesMap = new HashMap<String, Object>();
            valuesMap.put(PROCESS_INST_ID, processInstanceId);
            valuesMap.put(VAR_NAME, variableName);

            String queryString = getPagingQueryString("", page, pageSize);

            VariableInstanceList result = makeHttpGetRequestAndCreateCustomResponse(
                    build(baseURI, VAR_INSTANCES_BY_VAR_INSTANCE_ID_GET_URI, valuesMap) + queryString, VariableInstanceList.class);

            if (result != null && result.getVariableInstances() != null) {
                return Arrays.asList(result.getVariableInstances());
            }

            return Collections.emptyList();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }


}
