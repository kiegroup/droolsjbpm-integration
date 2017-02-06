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
import java.util.Map;

import org.kie.internal.process.CorrelationKey;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.api.model.instance.NodeInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.api.model.instance.VariableInstance;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.client.jms.ResponseHandler;

public interface ProcessServicesClient {

    // process definition
    ProcessDefinition getProcessDefinition(String containerId, String processId);

    SubProcessesDefinition getReusableSubProcessDefinitions(String containerId, String processId);

    VariablesDefinition getProcessVariableDefinitions(String containerId, String processId);

    ServiceTasksDefinition getServiceTaskDefinitions(String containerId, String processId);

    AssociatedEntitiesDefinition getAssociatedEntityDefinitions(String containerId, String processId);

    UserTaskDefinitionList getUserTaskDefinitions(String containerId, String processId);

    TaskInputsDefinition getUserTaskInputDefinitions(String containerId, String processId, String taskName);

    TaskOutputsDefinition getUserTaskOutputDefinitions(String containerId, String processId, String taskName);

    // process operations
    Long startProcess(String containerId, String processId);

    Long startProcess(String containerId, String processId, Map<String, Object> variables);

    Long startProcess(String containerId, String processId, CorrelationKey correlationKey);

    Long startProcess(String containerId, String processId, CorrelationKey correlationKey, Map<String, Object> variables);

    void abortProcessInstance(String containerId, Long processInstanceId);

    void abortProcessInstances(String containerId, List<Long> processInstanceIds);

    Object getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName);

    <T> T getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName, Class<T> type);

    Map<String, Object> getProcessInstanceVariables(String containerId, Long processInstanceId);

    void signalProcessInstance(String containerId, Long processInstanceId, String signalName, Object event);

    void signalProcessInstances(String containerId, List<Long> processInstanceId, String signalName, Object event);

    void signal(String containerId, String signalName, Object event);

    List<String> getAvailableSignals(String containerId, Long processInstanceId);

    void setProcessVariable(String containerId, Long processInstanceId, String variableId, Object value);

    void setProcessVariables(String containerId, Long processInstanceId, Map<String, Object> variables);

    ProcessInstance getProcessInstance(String containerId, Long processInstanceId);

    ProcessInstance getProcessInstance(String containerId, Long processInstanceId, boolean withVars);

    void completeWorkItem(String containerId, Long processInstanceId, Long id, Map<String, Object> results);

    void abortWorkItem(String containerId, Long processInstanceId, Long id);

    WorkItemInstance getWorkItem(String containerId, Long processInstanceId, Long id);

    List<WorkItemInstance> getWorkItemByProcessInstance(String containerId, Long processInstanceId);

    List<NodeInstance> findActiveNodeInstances(String containerId, Long processInstanceId, Integer page, Integer pageSize);

    List<NodeInstance> findCompletedNodeInstances(String containerId, Long processInstanceId, Integer page, Integer pageSize);

    List<NodeInstance> findNodeInstances(String containerId, Long processInstanceId, Integer page, Integer pageSize);

    List<VariableInstance> findVariablesCurrentState(String containerId, Long processInstanceId);

    List<VariableInstance> findVariableHistory(String containerId, Long processInstanceId, String variableName, Integer page, Integer pageSize);

    void setResponseHandler(ResponseHandler responseHandler);

    List<ProcessInstance> findProcessInstancesByParent(String containerId, Long parentProcessInstanceId, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByParent(String containerId, Long parentProcessInstanceId, List<Integer> status, Integer page, Integer pageSize);

    List<ProcessInstance> findProcessInstancesByParent(String containerId, Long parentProcessInstanceId, List<Integer> status, Integer page, Integer pageSize, String sort, boolean sortOrder);
}
