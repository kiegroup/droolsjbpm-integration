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

package org.kie.server.services.jbpm;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.drools.core.util.StringUtils;
import org.jbpm.kie.services.impl.form.FormProvider;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.task.impl.TaskContentRegistry;
import org.kie.api.task.model.Task;
import org.kie.internal.runtime.manager.InternalRuntimeManager;
import org.kie.internal.task.api.ContentMarshallerContext;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.marshal.MarshallerHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormServiceBase {

    public static final Logger logger = LoggerFactory.getLogger(FormServiceBase.class);

    private DefinitionService definitionService;
    private DeploymentService deploymentService;
    private RuntimeDataService dataService;
    private UserTaskService userTaskService;

    private MarshallerHelper marshallerHelper;

    private Set<FormProvider> providers;

    public FormServiceBase(DefinitionService definitionService, DeploymentService deploymentService,
            RuntimeDataService dataService, UserTaskService userTaskService,
            KieServerRegistry context) {
        this.definitionService = definitionService;
        this.deploymentService = deploymentService;
        this.dataService = dataService;
        this.userTaskService = userTaskService;
        this.marshallerHelper = new MarshallerHelper(context);
    }

    public void setProviders(Set<FormProvider> providers) {
        this.providers = providers;
    }

    public String getFormDisplayProcess(String containerId, String processId, String lang) {
        ProcessDefinition processDesc = definitionService.getProcessDefinition(containerId, processId);
        if (processDesc == null) {
            throw new IllegalStateException("Process definition " + containerId + " : " + processId + " not found");
        }
        Map<String, String> processData = definitionService.getProcessVariables(containerId, processId);

        if (processData == null) {
            processData = new HashMap<String, String>();
        }

        Map<String, Object> renderContext = new HashMap<String, Object>();
        renderContext.put("process", processDesc);
        renderContext.put("outputs", processData);
        renderContext.put("marshallerContext", marshallerHelper);
        renderContext.put("lang", lang);

        for (FormProvider provider : providers) {
            String template = provider.render(processDesc.getName(), processDesc, renderContext);
            if (!StringUtils.isEmpty(template)) {
                return template;
            }
        }

        logger.warn("Unable to find form to render for process '{}'", processDesc.getName());
        return "";
    }

    public String getFormDisplayTask(long taskId, String lang) {
        Task task = userTaskService.getTask(taskId);
        if (task == null) {
            return "";
        }
        String name = task.getName();
        ProcessDefinition processDesc = dataService.getProcessesByDeploymentIdProcessId(task.getTaskData()
                .getDeploymentId(), task.getTaskData().getProcessId());
        Map<String, Object> renderContext = new HashMap<String, Object>();

        Map<String, Object> input = userTaskService.getTaskInputContentByTaskId(taskId);
        renderContext.put("inputs", input);
        for (Map.Entry<String, Object> inputVar : ((Map<String, Object>) input).entrySet()) {
            renderContext.put(inputVar.getKey(), inputVar.getValue());
        }
        renderContext.put("lang", lang);

        for (FormProvider provider : providers) {
            String template = provider.render(name, task, processDesc, renderContext);
            if (!StringUtils.isEmpty(template)) {
                return template;
            }
        }

        logger.warn("Unable to find form to render for task '{}' on process '{}'", name, processDesc.getName());
        return "";
    }

    protected ContentMarshallerContext getMarshallerContext(String deploymentId, String processId) {
        DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
        if (deployedUnit == null) {
            return new ContentMarshallerContext();
        }
        InternalRuntimeManager manager = (InternalRuntimeManager) deployedUnit.getRuntimeManager();
        return new ContentMarshallerContext(manager.getEnvironment().getEnvironment(), manager.getEnvironment()
                .getClassLoader());
    }

    protected ContentMarshallerContext getMarshallerContext(Task task) {
        if (task == null) {
            return new ContentMarshallerContext();
        }
        return TaskContentRegistry.get().getMarshallerContext(task);
    }

}
