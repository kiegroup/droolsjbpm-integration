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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.api.model.UserTaskDefinition;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.NodeDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.TimerDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;

import static java.util.stream.Collectors.toSet;

public class DefinitionServiceBase {

    private DefinitionService definitionService;
    private KieServerRegistry context;

    public DefinitionServiceBase(DefinitionService definitionService, KieServerRegistry context) {
        this.definitionService = definitionService;
        this.context = context;
    }


    public org.kie.server.api.model.definition.ProcessDefinition getProcessDefinition(String containerId, String processId) {

        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());
        ProcessDefinition procDef = findProcessDefinition(containerId, processId);

        org.kie.server.api.model.definition.ProcessDefinition responseObject = org.kie.server.api.model.definition.ProcessDefinition.builder()
                .id(procDef.getId())
                .name(procDef.getName())
                .version(procDef.getVersion())
                .packageName(procDef.getPackageName())
                .containerId(procDef.getDeploymentId())
                .entitiesAsCollection(procDef.getAssociatedEntities())
                .serviceTasks(procDef.getServiceTasks())
                .subprocesses(procDef.getReusableSubProcesses())
                .variables(procDef.getProcessVariables())
                .tagsByVariables(procDef.getTagsInfo())
                .dynamic(procDef.isDynamic())
                .nodes(procDef.getNodes().stream().map(node -> NodeDefinition.builder().id(node.getId()).name(node.getName()).type(node.getNodeType()).uniqueId(node.getUniqueId()).build()).collect(toSet()))
                .timers(procDef.getTimers().stream().map(timer -> TimerDefinition.builder().id(timer.getId()).nodeId(timer.getNodeId()).nodeName(timer.getNodeName()).uniqueId(timer.getUniqueId()).build()).collect(toSet()))
                .build();
        return responseObject;
    }

    public SubProcessesDefinition getReusableSubProcesses(String containerId, String processId) {
        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());
        findProcessDefinition(containerId, processId);

        Collection<String> reusableSubProcesses = definitionService.getReusableSubProcesses(containerId, processId);

        return new SubProcessesDefinition(reusableSubProcesses);
    }


    public VariablesDefinition getProcessVariables(String containerId, String processId) {
        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());
        findProcessDefinition(containerId, processId);

        Map<String, String> processVariables = definitionService.getProcessVariables(containerId, processId);

        return new VariablesDefinition(processVariables);
    }


    public ServiceTasksDefinition getServiceTasks(String containerId, String processId) {
        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());
        findProcessDefinition(containerId, processId);

        Map<String, String> serviceTasks = definitionService.getServiceTasks(containerId, processId);

        return new ServiceTasksDefinition(serviceTasks);
    }


    public AssociatedEntitiesDefinition getAssociatedEntities(String containerId, String processId) {
        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());
        findProcessDefinition(containerId, processId);

        Map<String, Collection<String>> entities = definitionService.getAssociatedEntities(containerId, processId);

        return AssociatedEntitiesDefinition.from(entities);
    }


    public UserTaskDefinitionList getTasksDefinitions(String containerId, String processId) {
        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());
        findProcessDefinition(containerId, processId);

        Collection<UserTaskDefinition> userTaskDefinitions = definitionService.getTasksDefinitions(containerId, processId);

        return convert(userTaskDefinitions);
    }


    public TaskInputsDefinition getTaskInputMappings(String containerId, String processId, String taskName) {
        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());
        findProcessDefinition(containerId, processId);

        Map<String, String> taskInputs = definitionService.getTaskInputMappings(containerId, processId, taskName);

        return new TaskInputsDefinition(taskInputs);

    }


    public TaskOutputsDefinition getTaskOutputMappings(String containerId, String processId, String taskName) {
        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());
        findProcessDefinition(containerId, processId);

        Map<String, String> taskOutputs = definitionService.getTaskOutputMappings(containerId, processId, taskName);

        return new TaskOutputsDefinition(taskOutputs);
    }

    protected ProcessDefinition findProcessDefinition(String containerId, String processId) {
        containerId = context.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());
        ProcessDefinition procDef = definitionService.getProcessDefinition(containerId, processId);
        if (procDef == null) {
            throw new IllegalStateException("Process definition " + containerId + " : " + processId + " not found");
        }

        return procDef;

    }

    protected UserTaskDefinitionList convert(Collection<UserTaskDefinition> taskDefinitions) {
        org.kie.server.api.model.definition.UserTaskDefinition[] userTaskDefinitions = new org.kie.server.api.model.definition.UserTaskDefinition[taskDefinitions.size()];

        int i = 0;
        for (UserTaskDefinition orig : taskDefinitions) {

            Collection<String> entities = orig.getAssociatedEntities();
            if (entities == null) {
                entities = Collections.emptyList();
            }
            org.kie.server.api.model.definition.UserTaskDefinition definition = org.kie.server.api.model.definition.UserTaskDefinition.builder()
                    .id(orig.getId())
                    .name(orig.getName())
                    .comment(orig.getComment())
                    .createdBy(orig.getCreatedBy())
                    .priority(orig.getPriority())
                    .skippable(orig.isSkippable())
                    .formName(orig.getFormName())
                    .entities(entities.toArray(new String[entities.size()]))
                    .taskInputs(orig.getTaskInputMappings())
                    .taskOutputs(orig.getTaskOutputMappings())
                    .build();
            userTaskDefinitions[i] = definition;
            i++;
        }

        return new UserTaskDefinitionList(userTaskDefinitions);
    }
}
