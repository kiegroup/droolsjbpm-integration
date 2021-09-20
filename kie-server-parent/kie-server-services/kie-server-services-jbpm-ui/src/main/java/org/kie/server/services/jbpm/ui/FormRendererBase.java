/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.jbpm.ui;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jbpm.casemgmt.api.CaseDefinitionNotFoundException;
import org.jbpm.casemgmt.api.CaseRuntimeDataService;
import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.ProcessDefinitionNotFoundException;
import org.jbpm.services.api.TaskNotFoundException;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.task.commands.GetUserTaskCommand;
import org.kie.api.task.model.Task;
import org.kie.internal.task.api.model.InternalTask;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.kie.server.services.jbpm.locator.ByTaskIdContainerLocator;
import org.kie.server.services.jbpm.ui.form.render.FormReader;
import org.kie.server.services.jbpm.ui.form.render.FormRenderer;
import org.kie.server.services.jbpm.ui.form.render.model.FormField;
import org.kie.server.services.jbpm.ui.form.render.model.FormInstance;
import org.kie.server.services.jbpm.ui.form.render.model.FormLayout;
import org.kie.server.services.jbpm.ui.form.render.model.FormModel;
import org.kie.server.services.jbpm.ui.form.render.model.LayoutColumn;
import org.kie.server.services.jbpm.ui.form.render.model.LayoutItem;
import org.kie.server.services.jbpm.ui.form.render.model.LayoutRow;

public class FormRendererBase {
    
    private static final String SUPPORTED_FORM_SUFFIX = "-taskform.frm";
    private static final String ENDPOINT = "/files";
    
    private static final ServiceLoader<FormRenderer> formRenderers = ServiceLoader.load(FormRenderer.class);

    private DefinitionService definitionService;
    private FormManagerService formManagerService;
    private UserTaskService userTaskService;
    private CaseRuntimeDataService caseRuntimeDataService;

    private KieServerRegistry registry;
    
    private Map<String, FormRenderer> renderers = new HashMap<>() ;
    private FormReader formReader = new FormReader();
    
    private Map<String, Map<String, String>> indexedForms = new ConcurrentHashMap<>();
            
    
    public FormRendererBase(DefinitionService definitionService, UserTaskService userTaskService, FormManagerService formManagerService, CaseRuntimeDataService caseRuntimeDataService, KieServerRegistry registry) {
        super();
        this.definitionService = definitionService;
        this.userTaskService = userTaskService;
        this.formManagerService = formManagerService;
        this.caseRuntimeDataService = caseRuntimeDataService;
        this.registry = registry;
        
        for (FormRenderer renderer : formRenderers) {
            
            renderer.configure(System.getProperty("org.kie.server.location"), ENDPOINT);
            renderers.put(renderer.getName(), renderer);
        }
                
    }
    
    public void indexDeploymentForms(String containerId) {
        
        Map<String, String> forms = formManagerService.getAllFormsByDeployment(containerId);
        if (forms != null) {
            List<String> supportedFormsOnly = forms.entrySet()
                                            .stream()
                                            .filter( entry -> entry.getKey().endsWith( ".frm" ) )
                                            .map( entry -> entry.getValue() )
                                            .collect(Collectors.toList());
            if (!supportedFormsOnly.isEmpty()) {
                Map<String, String> formsByDeployment = new HashMap<>();
                indexedForms.put(containerId, formsByDeployment);
                
                for (String formContent : supportedFormsOnly) {
                    String formId = formReader.readFromString(formContent).getId();
                    formsByDeployment.put(formId, formContent);
                }
            }
        }
    }
    
    public void dropDeploymentForms(String containerId) {
        indexedForms.remove(containerId);
    }

    public String getProcessRenderedForm(String renderer, String containerId, String processId) {
        String resolvedContainerId = registry.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());

        ProcessDefinition processDesc = definitionService.getProcessDefinition(resolvedContainerId, processId);
        if (processDesc == null) {
            throw new ProcessDefinitionNotFoundException("Process definition " + resolvedContainerId + " : " + processId + " not found");
        }
        
        String formStructure = formManagerService.getFormByKey(resolvedContainerId, processId + SUPPORTED_FORM_SUFFIX);   
        
        FormInstance form = formReader.readFromString(formStructure);
        
        if (form == null) {
            // generate default form as there is none existing
            
            form = generateDefaultProcessForm(processDesc);
        }
        
        form.setNestedFormsLookup(formId -> {
            Map<String, String> forms = indexedForms.get(resolvedContainerId);
            String formStructureNested = forms.get(formId);
            return formReader.readFromString(formStructureNested);
        });
        FormRenderer formRenderer = renderers.get(renderer);
        if (formRenderer == null) {
            throw new IllegalArgumentException("Form renderer with name " + renderer + " not found");
        }
        String output = formRenderer.renderProcess(resolvedContainerId, processDesc, form);
        
        return output;
    }
    
    public String getTaskRenderedForm(String renderer, String containerId, long taskId) {
        String resolvedContainerId = registry.getContainerId(containerId, new ByTaskIdContainerLocator(taskId));

        Task task = userTaskService.execute(resolvedContainerId, new GetUserTaskCommand(registry.getIdentityProvider().getName(), taskId));
        if (task == null) {
            throw new TaskNotFoundException("No task with id " + taskId + " found");
        }
        String name = getTaskFormName(task);

        String formStructure = formManagerService.getFormByKey(resolvedContainerId, name);

        FormInstance form = formReader.readFromString(formStructure);
        if (form == null) {
            // generate default form as there is none existing
            Map<String, String> inputs = definitionService.getTaskInputMappings(resolvedContainerId, task.getTaskData().getProcessId(), task.getName());
            Map<String, String> outputs = definitionService.getTaskOutputMappings(resolvedContainerId, task.getTaskData().getProcessId(), task.getName());
            form = generateDefaultTaskForm(task, inputs, outputs);
        }
        form.setNestedFormsLookup(formId -> {
            Map<String, String> forms = indexedForms.get(resolvedContainerId);
            String formStructureNested = forms.get(formId);
            return formReader.readFromString(formStructureNested);
        });
        
        Map<String, Object> inputData = userTaskService.getTaskInputContentByTaskId(resolvedContainerId, taskId);        
        Map<String, Object> outputData = userTaskService.getTaskOutputContentByTaskId(resolvedContainerId, taskId);        
        
        FormRenderer formRenderer = renderers.get(renderer);
        if (formRenderer == null) {
            throw new IllegalArgumentException("Form renderer with name " + renderer + " not found");
        }
        String output = formRenderer.renderTask(resolvedContainerId, task, form, inputData, outputData);
        
        return output;
    }
    
    public String getCaseRenderedForm(String renderer, String containerId, String caseDefinitionId) {
        if (caseRuntimeDataService == null) {
            throw new RuntimeException("Case capability in jBPM UI are not enabled");
        }
        String resolvedContainerId = registry.getContainerId(containerId, ContainerLocatorProvider.get().getLocator());

        CaseDefinition caseDefinition = caseRuntimeDataService.getCase(resolvedContainerId, caseDefinitionId);
        if (caseDefinition == null) {
            throw new CaseDefinitionNotFoundException("Case definition " + resolvedContainerId + " : " + caseDefinitionId + " not found");
        }
        
        String formStructure = formManagerService.getFormByKey(resolvedContainerId, caseDefinitionId + SUPPORTED_FORM_SUFFIX);
        
        
        FormInstance form = formReader.readFromString(formStructure);
        if (form != null) {
            form.setNestedFormsLookup(formId -> {
                Map<String, String> forms = indexedForms.get(resolvedContainerId);
                String formStructureNested = forms.get(formId);
                return formReader.readFromString(formStructureNested);
            });
        }
        FormRenderer formRenderer = renderers.get(renderer);
        if (formRenderer == null) {
            throw new IllegalArgumentException("Form renderer with name " + renderer + " not found");
        }
        String output = formRenderer.renderCase(resolvedContainerId, caseDefinition, form);
        
        return output;
    }
    
    public InputStream readResources(String resourcePath) {
        return this.getClass().getResourceAsStream("/form-templates-providers" + resourcePath);
    }
    
    protected String getTaskFormName(Task task) {
        String formName = ((InternalTask ) task).getFormName();
        if (formName != null && !formName.equals("")) {
            // if the form name has extension it
            if ( formName.endsWith( ".frm" ) ) {
                return formName;
            }
            return formName + SUPPORTED_FORM_SUFFIX;
        } else {
            if (task.getNames() != null && !task.getNames().isEmpty()) {
                formName = task.getNames().get(0).getText();
                if (formName != null) return formName.replace(" ", "") + SUPPORTED_FORM_SUFFIX;
            }
        }
        return null;
    }
    
    protected FormInstance generateDefaultProcessForm(ProcessDefinition processDesc) {
        FormInstance form = new FormInstance();
        form.setId(UUID.randomUUID().toString());
        form.setName("Default form - " + processDesc.getName());
        
        form.setModel(new FormModel());
        
        form.setFields(new ArrayList<>());
        
        FormLayout layout = new FormLayout();
        layout.setRows(new ArrayList<>());
        form.setLayout(layout);
        
        Map<String, String> variables = processDesc.getProcessVariables();
        
        if (variables != null) {
            
            for (Entry<String, String> entry : variables.entrySet()) {
            
                FormField field = new FormField();
                field.setId(UUID.randomUUID().toString());
                field.setBinding(entry.getKey());
                field.setCode(entry.getValue().contains("Boolean")?"CheckBox" :"TextBox");
                field.setLabel(entry.getKey());
                field.setName(entry.getKey());
                field.setType(entry.getValue());
                field.setTags(processDesc.getTagsForVariable(entry.getKey()));
                Set<String> tags = processDesc.getTagsForVariable(entry.getKey());
                field.setRequired(tags.contains("required"));
                field.setReadOnly(tags.contains("readonly"));
                
                form.getFields().add(field);
                
                LayoutRow row = new LayoutRow();
                layout.getRows().add(row);
                row.setColumns(new ArrayList<>());
                
                LayoutColumn column = new LayoutColumn();
                column.setSpan("12");
                column.setItems(new ArrayList<>());                    
                row.getColumns().add(column);
                
                LayoutItem item = new LayoutItem();
                item.setFieldId(field.getId());
                item.setFormId(form.getId());
                
                column.getItems().add(item);
            }
        }
        
        return form;
    }
    
    protected FormInstance generateDefaultTaskForm(Task task, Map<String, String> inputs, Map<String, String> outputs) {
        FormInstance form = new FormInstance();
        form.setId(UUID.randomUUID().toString());
        form.setName("Default form - " + task.getName());
        
        form.setModel(new FormModel());
        
        form.setFields(new ArrayList<>());
        
        FormLayout layout = new FormLayout();
        layout.setRows(new ArrayList<>());
        form.setLayout(layout);
        
        if (inputs != null) {
            
            for (Entry<String, String> entry : inputs.entrySet()) {
            
                FormField field = new FormField();
                field.setId(UUID.randomUUID().toString());
                field.setBinding(entry.getKey());
                field.setCode(entry.getValue().contains("Boolean")?"CheckBox" :"TextBox");
                field.setLabel(entry.getKey());
                field.setName(entry.getKey());
                field.setType(entry.getValue());
                field.setReadOnly(true);
                
                form.getFields().add(field);
                
                LayoutRow row = new LayoutRow();
                layout.getRows().add(row);
                row.setColumns(new ArrayList<>());
                
                LayoutColumn column = new LayoutColumn();
                column.setSpan("12");
                column.setItems(new ArrayList<>());                    
                row.getColumns().add(column);
                
                LayoutItem item = new LayoutItem();
                item.setFieldId(field.getId());
                item.setFormId(form.getId());
                
                column.getItems().add(item);
            }
        }
        
        if (outputs != null) {
            
            for (Entry<String, String> entry : outputs.entrySet()) {
            
                FormField field = new FormField();
                field.setId(UUID.randomUUID().toString());
                field.setBinding(entry.getKey());
                field.setCode(entry.getValue().contains("Boolean")?"CheckBox" :"TextBox");
                field.setLabel(entry.getKey());
                field.setName(entry.getKey());
                field.setType(entry.getValue());
                
                form.getFields().add(field);
                
                LayoutRow row = new LayoutRow();
                layout.getRows().add(row);
                row.setColumns(new ArrayList<>());
                
                LayoutColumn column = new LayoutColumn();
                column.setSpan("12");
                column.setItems(new ArrayList<>());                    
                row.getColumns().add(column);
                
                LayoutItem item = new LayoutItem();
                item.setFieldId(field.getId());
                item.setFormId(form.getId());
                
                column.getItems().add(item);
            }
        }
        
        return form;
    }
}
