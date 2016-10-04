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

package org.kie.server.services.jbpm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationKeyFactory;
import org.kie.server.api.model.instance.WorkItemInstance;
import org.kie.server.api.model.instance.WorkItemInstanceList;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.locator.LatestContainerLocator;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.kie.server.services.jbpm.locator.ByProcessInstanceIdContainerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessServiceBase {

    public static final Logger logger = LoggerFactory.getLogger(ProcessServiceBase.class);

    private ProcessService processService;
    private DefinitionService definitionService;
    private RuntimeDataService runtimeDataService;
    private MarshallerHelper marshallerHelper;
    private KieServerRegistry context;

    private CorrelationKeyFactory correlationKeyFactory = KieInternalServices.Factory.get().newCorrelationKeyFactory();

    public ProcessServiceBase(ProcessService processService, DefinitionService definitionService, RuntimeDataService runtimeDataService, KieServerRegistry context) {
        this.processService = processService;
        this.definitionService = definitionService;
        this.runtimeDataService = runtimeDataService;
        this.marshallerHelper = new MarshallerHelper(context);
        this.context = context;
    }

    public String startProcess(String containerId, String processId, String marshallingType) {
        containerId = context.getContainer(containerId, LatestContainerLocator.get()).getContainerId();
        // Check for presence of process id
        ProcessDefinition procDef = definitionService.getProcessDefinition(containerId, processId);
        if( procDef == null ) {
            throw new IllegalStateException("Unable to find process '" + processId + "' in container " + containerId);
        }
        logger.debug("Calling start process with id {} on container {} and parameters {}", processId, containerId, null);
        Long processInstanceId = processService.startProcess(containerId, processId);

        // return response
        String response = marshallerHelper.marshal(containerId, marshallingType, processInstanceId);
        return response;
    }


    public String startProcess(String containerId, String processId, String payload, String marshallingType) {
        containerId = context.getContainer(containerId, LatestContainerLocator.get()).getContainerId();
        // Check for presence of process id
        ProcessDefinition procDef = definitionService.getProcessDefinition(containerId, processId);
        if( procDef == null ) {
            throw new IllegalStateException("Unable to find process '" + processId + "' in container " + containerId);
        }

        logger.debug("About to unmarshal parameters from payload: '{}'", payload);
        Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class);

        logger.debug("Calling start process with id {} on container {} and parameters {}", processId, containerId, parameters);
        Long processInstanceId = processService.startProcess(containerId, processId, parameters);

        // return response
        String response = marshallerHelper.marshal(containerId, marshallingType, processInstanceId);
        return response;
    }

    public String startProcessWithCorrelation(String containerId, String processId, String correlationKey, String payload, String marshallingType) {
        containerId = context.getContainer(containerId, LatestContainerLocator.get()).getContainerId();
        // Check for presence of process id
        ProcessDefinition procDef = definitionService.getProcessDefinition(containerId, processId);
        if( procDef == null ) {
            throw new IllegalStateException("Unable to find process '" + processId + "' in container " + containerId);
        }
        logger.debug("About to unmarshal parameters from payload: '{}'", payload);
        Map<String, Object> parameters = marshallerHelper.unmarshal(containerId, payload, marshallingType, Map.class);

        String[] correlationProperties = correlationKey.split(":");

        CorrelationKey actualCorrelationKey = correlationKeyFactory.newCorrelationKey(Arrays.asList(correlationProperties));

        logger.debug("Calling start process with id {} on container {} and parameters {}", processId, containerId, parameters);
        Long processInstanceId = processService.startProcess(containerId, processId, actualCorrelationKey, parameters);

        // return response
        String response = marshallerHelper.marshal(containerId, marshallingType, processInstanceId);
        return response;

    }


    public Object abortProcessInstance(String containerId, Number processInstanceId) {

        processService.abortProcessInstance(processInstanceId.longValue());
        return null;
    }


    protected List<Long> convert(List<? extends Number> input) {
        List<Long> result = new ArrayList<Long>();

        for (Number n : input) {
            result.add(n.longValue());
        }

        return result;
    }

    public Object abortProcessInstances(String containerId, List<Long> processInstanceIds) {
        processService.abortProcessInstances(convert(processInstanceIds));
        return null;

    }

    public void signalProcessInstance(String containerId, Number processInstanceId, String signalName, String marshallingType) {

        logger.debug("Calling signal '{}' process instance with id {} on container {} and event {}", signalName, processInstanceId, containerId, null);
        processService.signalProcessInstance(processInstanceId.longValue(), signalName, null);

    }

    public void signalProcessInstance(String containerId, Number processInstanceId, String signalName, String eventPayload, String marshallingType) {


        logger.debug("About to unmarshal event from payload: '{}'", eventPayload);
        Object event = marshallerHelper.unmarshal(containerId, eventPayload, marshallingType, Object.class, new ByProcessInstanceIdContainerLocator(processInstanceId.longValue()));

        logger.debug("Calling signal '{}' process instance with id {} on container {} and event {}", signalName, processInstanceId, containerId, event);
        processService.signalProcessInstance(processInstanceId.longValue(), signalName, event);

    }

    public void signalProcessInstances(String containerId, List<Long> processInstanceIds, String signalName, String marshallingType) {

        logger.debug("Calling signal '{}' process instances with id {} on container {} and event {}", signalName, processInstanceIds, containerId, null);
        processService.signalProcessInstances(convert(processInstanceIds), signalName, null);

    }

    public void signalProcessInstances(String containerId, List<Long> processInstanceIds, String signalName, String eventPayload, String marshallingType) {

        List<Long> ids = convert(processInstanceIds);
        if (ids.isEmpty()) {
            return;
        }
        logger.debug("About to unmarshal event from payload: '{}'", eventPayload);
        Object event = marshallerHelper.unmarshal(containerId, eventPayload, marshallingType, Object.class, new ByProcessInstanceIdContainerLocator(ids.get(0)));

        logger.debug("Calling signal '{}' process instances with id {} on container {} and event {}", signalName, processInstanceIds, containerId, event);
        processService.signalProcessInstances(ids, signalName, event);

    }

    public void signal(String containerId, String signalName, String marshallingType) {

        logger.debug("Calling signal '{}' on container {} and event {}", signalName, containerId, null);
        processService.signalEvent(containerId, signalName, null);

    }

    public void signal(String containerId, String signalName, String eventPayload, String marshallingType) {

        logger.debug("About to unmarshal event from payload: '{}'", eventPayload);
        Object event = marshallerHelper.unmarshal(containerId, eventPayload, marshallingType, Object.class);

        logger.debug("Calling signal '{}' on container {} and event {}", signalName, containerId, event);
        processService.signalEvent(containerId, signalName, event);

    }

    public String getProcessInstance(String containerId,  Number processInstanceId, boolean withVars, String marshallingType) {

        ProcessInstanceDesc instanceDesc = runtimeDataService.getProcessInstanceById(processInstanceId.longValue());
        if (instanceDesc == null) {
            throw new IllegalStateException("Unable to find process instance with id " + processInstanceId);
        }

        org.kie.server.api.model.instance.ProcessInstance processInstance = org.kie.server.api.model.instance.ProcessInstance.builder()
                .id(instanceDesc.getId())
                .processId(instanceDesc.getProcessId())
                .processName(instanceDesc.getProcessName())
                .processVersion(instanceDesc.getProcessVersion())
                .state(instanceDesc.getState())
                .containerId(instanceDesc.getDeploymentId())
                .date(instanceDesc.getDataTimeStamp())
                .initiator(instanceDesc.getInitiator())
                .processInstanceDescription(instanceDesc.getProcessInstanceDescription())
                .parentInstanceId(instanceDesc.getParentId())
                .correlationKey(instanceDesc.getCorrelationKey())
                .build();

        if (Boolean.TRUE.equals(withVars) && processInstance.getState().equals(ProcessInstance.STATE_ACTIVE)) {
            Map<String, Object> variables = processService.getProcessInstanceVariables(processInstanceId.longValue());
            processInstance.setVariables(variables);
        }

        logger.debug("About to marshal process instance with id '{}' {}", processInstanceId, processInstance);
        String response = marshallerHelper.marshal(containerId, marshallingType, processInstance, new ByProcessInstanceIdContainerLocator(processInstanceId.longValue()));

        return response;

    }

    public void setProcessVariable(String containerId, Number processInstanceId, String varName, String variablePayload, String marshallingType) {

        logger.debug("About to unmarshal variable from payload: '{}'", variablePayload);
        Object variable = marshallerHelper.unmarshal(containerId, variablePayload, marshallingType, Object.class, new ByProcessInstanceIdContainerLocator(processInstanceId.longValue()));

        logger.debug("Setting variable '{}' on process instance with id {} with value {}", varName, processInstanceId, variable);
        processService.setProcessVariable(processInstanceId.longValue(), varName, variable);

    }

    public void setProcessVariables(String containerId, Number processInstanceId, String variablePayload, String marshallingType) {

        logger.debug("About to unmarshal variables from payload: '{}'", variablePayload);
        Map<String, Object> variables = marshallerHelper.unmarshal(containerId, variablePayload, marshallingType, Map.class, new ByProcessInstanceIdContainerLocator(processInstanceId.longValue()));

        logger.debug("Setting variables '{}' on process instance with id {} with value {}", variables.keySet(), processInstanceId, variables.values());
        processService.setProcessVariables(processInstanceId.longValue(), variables);
   }


    public String getProcessInstanceVariable(String containerId, Number processInstanceId, String varName, String marshallingType) {

        Object variable = processService.getProcessInstanceVariable(processInstanceId.longValue(), varName);

        if (variable == null) {
            throw new IllegalStateException("Unable to find variable '"+ varName + "' within process instance with id " + processInstanceId);
        }

        logger.debug("About to marshal process variable with name '{}' {}", varName, variable);
        String response = marshallerHelper.marshal(containerId, marshallingType, variable, new ByProcessInstanceIdContainerLocator(processInstanceId.longValue()));

        return response;

    }

    public String getProcessInstanceVariables(String containerId, Number processInstanceId, String marshallingType) {
        Map<String, Object> variables = processService.getProcessInstanceVariables(processInstanceId.longValue());

        logger.debug("About to marshal process variables {}", variables);
        String response = marshallerHelper.marshal(containerId, marshallingType, variables, new ByProcessInstanceIdContainerLocator(processInstanceId.longValue()));

        return response;

    }


    public String getAvailableSignals(String containerId, Number processInstanceId, String marshallingType) {
        Collection<String> signals = processService.getAvailableSignals(processInstanceId.longValue());

        logger.debug("About to marshal available signals {}", signals);
        String response = marshallerHelper.marshal(containerId, marshallingType, signals);

        return response;
    }


    public void completeWorkItem(String containerId, Number processInstanceId, Number workItemId, String resultPayload, String marshallingType) {

        logger.debug("About to unmarshal work item result from payload: '{}'", resultPayload);
        Map<String, Object> results = marshallerHelper.unmarshal(containerId, resultPayload, marshallingType, Map.class, new ByProcessInstanceIdContainerLocator(processInstanceId.longValue()));

        logger.debug("Completing work item '{}' on process instance id {} with value {}", workItemId, processInstanceId, results);
        processService.completeWorkItem(workItemId.longValue(), results);

    }


    public void abortWorkItem(String containerId, Number processInstanceId, Number workItemId) {

        logger.debug("Aborting work item '{}' on process instance id {}", workItemId, processInstanceId);
        processService.abortWorkItem(workItemId.longValue());
    }


    public String getWorkItem(String containerId, Number processInstanceId, Number workItemId, String marshallingType) {
        WorkItem workItem = processService.getWorkItem(workItemId.longValue());

        if (workItem == null) {
            throw new IllegalStateException("Unable to find work item with id " + workItemId);
        }

        WorkItemInstance workItemInstance = WorkItemInstance.builder()
                .id(workItem.getId())
                .nodeInstanceId(((org.drools.core.process.instance.WorkItem) workItem).getNodeInstanceId())
                .processInstanceId(workItem.getProcessInstanceId())
                .containerId(((org.drools.core.process.instance.WorkItem) workItem).getDeploymentId())
                .name(workItem.getName())
                .nodeId(((org.drools.core.process.instance.WorkItem) workItem).getNodeId())
                .parameters(workItem.getParameters())
                .state(workItem.getState())
                .build();

        logger.debug("About to marshal work item {}", workItemInstance);
        String response = marshallerHelper.marshal(containerId, marshallingType, workItemInstance, new ByProcessInstanceIdContainerLocator(processInstanceId.longValue()));

        return response;

    }

    public String getWorkItemByProcessInstance(String containerId, Number processInstanceId, String marshallingType) {


        List<WorkItem> workItems = processService.getWorkItemByProcessInstance(processInstanceId.longValue());

        WorkItemInstance[] instances = new WorkItemInstance[workItems.size()];
        int counter = 0;
        for (WorkItem workItem : workItems) {
            WorkItemInstance workItemInstance = WorkItemInstance.builder()
                    .id(workItem.getId())
                    .nodeInstanceId(((org.drools.core.process.instance.WorkItem) workItem).getNodeInstanceId())
                    .processInstanceId(workItem.getProcessInstanceId())
                    .containerId(((org.drools.core.process.instance.WorkItem) workItem).getDeploymentId())
                    .name(workItem.getName())
                    .nodeId(((org.drools.core.process.instance.WorkItem) workItem).getNodeId())
                    .parameters(workItem.getParameters())
                    .state(workItem.getState())
                    .build();
            instances[counter] = workItemInstance;
            counter++;
        }
        WorkItemInstanceList result = new WorkItemInstanceList(instances);
        logger.debug("About to marshal work items {}", result);
        String response = marshallerHelper.marshal(containerId, marshallingType, result, new ByProcessInstanceIdContainerLocator(processInstanceId.longValue()));

        return response;
    }


}
