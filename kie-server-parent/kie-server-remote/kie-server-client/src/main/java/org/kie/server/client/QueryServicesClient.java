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

package org.kie.server.client;

import java.util.List;

import org.kie.internal.process.CorrelationKey;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.VariableInstance;

public interface QueryServicesClient {

    // runtime data searches
    ProcessDefinition findProcessByContainerIdProcessId(String containerId, String processId);

    List<ProcessDefinition> findProcessesById(String processId);

    List<ProcessDefinition> findProcesses(Integer page, Integer pageSize);

    List<ProcessDefinition> findProcesses(String filter, Integer page, Integer pageSize);

    List<ProcessDefinition> findProcessesByContainerId(String containerId, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstances(Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByCorrelationKey(CorrelationKey correlationKey, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByProcessId(String processId, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByProcessName(String processName, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByContainerId(String containerId, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByStatus(List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByInitiator(String initiator, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByVariable(String variableName, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByVariableAndValue(String variableName, String variableValue, List<Integer> status, Integer page, Integer pageSize);

    ProcessInstance findProcessInstanceById(Long processInstanceId);

    ProcessInstance findProcessInstanceByCorrelationKey(CorrelationKey correlationKey);

    NodeInstance findNodeInstanceByWorkItemId(Long processInstanceId, Long workItemId);

    List<NodeInstance> findActiveNodeInstances(Long processInstanceId, Integer page, Integer pageSize);

    List<NodeInstance> findCompletedNodeInstances(Long processInstanceId, Integer page, Integer pageSize);

    List<NodeInstance> findNodeInstances(Long processInstanceId, Integer page, Integer pageSize);

    List<VariableInstance> findVariablesCurrentState(Long processInstanceId);

    List<VariableInstance> findVariableHistory(Long processInstanceId, String variableName, Integer page, Integer pageSize);
}
