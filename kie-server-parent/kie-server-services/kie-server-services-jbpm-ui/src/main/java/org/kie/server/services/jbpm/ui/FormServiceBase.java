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

package org.kie.server.services.jbpm.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.drools.core.util.StringUtils;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.task.model.Task;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.kie.server.services.jbpm.ui.api.UIFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormServiceBase {

    public static final Logger logger = LoggerFactory.getLogger(FormServiceBase.class);

    private static final ServiceLoader<UIFormProvider> formProviders = ServiceLoader.load(UIFormProvider.class);

    private DefinitionService definitionService;
    private RuntimeDataService dataService;
    private UserTaskService userTaskService;

    private KieServerRegistry registry;

    private Set<UIFormProvider> providers = new LinkedHashSet<UIFormProvider>();

    public FormServiceBase(DefinitionService definitionService, RuntimeDataService dataService, UserTaskService userTaskService, FormManagerService formManagerService, KieServerRegistry registry) {
        this.definitionService = definitionService;
        this.dataService = dataService;
        this.userTaskService = userTaskService;
        this.registry = registry;

        providers.addAll(collectFormProviders(formManagerService));
    }


    public String getFormDisplayProcess(String containerId, String processId, String lang, boolean filterContent) {
        containerId = registry.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());

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
        renderContext.put("lang", lang);
        renderContext.put("filterForm", filterContent);

        for (UIFormProvider provider : providers) {
            String template = provider.render(processDesc.getName(), processDesc, renderContext);
            if (!StringUtils.isEmpty(template)) {
                return template;
            }
        }

        logger.warn("Unable to find form to render for process '{}'", processDesc.getName());
        throw new IllegalStateException("No form for process with id " + processDesc.getName() + " found");
    }

    public String getFormDisplayTask(long taskId, String lang, boolean filterContent) {
        Task task = userTaskService.getTask(taskId);
        if (task == null) {
            throw new IllegalStateException("No task with id " + taskId + " found");
        }
        String name = task.getName();
        ProcessDefinition processDesc = dataService.getProcessesByDeploymentIdProcessId(task.getTaskData()
                .getDeploymentId(), task.getTaskData().getProcessId());
        Map<String, Object> renderContext = new HashMap<String, Object>();
        renderContext.put("filterForm", filterContent);

        Map<String, Object> input = userTaskService.getTaskInputContentByTaskId(taskId);
        renderContext.put("inputs", input);
        for (Map.Entry<String, Object> inputVar : ((Map<String, Object>) input).entrySet()) {
            renderContext.put(inputVar.getKey(), inputVar.getValue());
        }
        renderContext.put("lang", lang);

        for (UIFormProvider provider : providers) {
            String template = provider.render(name, task, processDesc, renderContext);
            if (!StringUtils.isEmpty(template)) {
                return template;
            }
        }

        logger.warn("Unable to find form to render for task '{}' on process '{}'", name, processDesc.getName());
        throw new IllegalStateException("No form for task with id " + taskId + " found");
    }

    protected List<UIFormProvider> collectFormProviders(FormManagerService formManagerService) {
        List<UIFormProvider> uiFormProviders = new ArrayList<UIFormProvider>();

        for (UIFormProvider formProvider : formProviders) {
            formProvider.configure(formManagerService);
            uiFormProviders.add(formProvider);
        }

        Collections.sort(uiFormProviders, new Comparator<UIFormProvider>() {
            @Override
            public int compare(UIFormProvider e1, UIFormProvider e2) {
                return Integer.valueOf(e1.getPriority()).compareTo(Integer.valueOf(e2.getPriority()));
            }
        });

        return uiFormProviders;
    }
}