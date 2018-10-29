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

package org.kie.server.services.jbpm.ui.form.render;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.casemgmt.api.model.CaseRole;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.task.model.Task;
import org.kie.server.services.jbpm.ui.form.render.model.FormField;
import org.kie.server.services.jbpm.ui.form.render.model.FormInstance;
import org.kie.server.services.jbpm.ui.form.render.model.FormLayout;
import org.kie.server.services.jbpm.ui.form.render.model.LayoutColumn;
import org.kie.server.services.jbpm.ui.form.render.model.LayoutItem;
import org.kie.server.services.jbpm.ui.form.render.model.LayoutRow;
import org.kie.server.services.jbpm.ui.form.render.model.TableInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

public abstract class AbstractFormRenderer implements FormRenderer {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractFormRenderer.class);
    
    public static final String MASTER_LAYOUT_TEMPLATE = "master";
    public static final String HEADER_LAYOUT_TEMPLATE = "header";
    public static final String FORM_GROUP_LAYOUT_TEMPLATE = "form-group";
    public static final String CASE_LAYOUT_TEMPLATE = "case-layout";
    public static final String PROCESS_LAYOUT_TEMPLATE = "process-layout";
    public static final String TASK_LAYOUT_TEMPLATE = "task-layout";
    public static final String TABLE_LAYOUT_TEMPLATE = "table";
    
    private Map<String, String> inputTypes;
    private StringTemplateLoader stringLoader = new StringTemplateLoader();
    private Configuration cfg;
    
    private FormReader reader = new FormReader();
    
    private String serverPath;
    private String resourcePath;
    
    
    public AbstractFormRenderer(String serverPath, String resources) {
        this.serverPath = serverPath;
        this.resourcePath = serverPath + resources;
        this.inputTypes = new HashMap<>();
        this.inputTypes.put("TextBox", "text");
        this.inputTypes.put("IntegerBox", "text");
        this.inputTypes.put("DecimalBox", "text");
        this.inputTypes.put("TextArea", "textarea");
        this.inputTypes.put("CheckBox", "checkbox");
        this.inputTypes.put("ListBox", "select");
        this.inputTypes.put("RadioGroup", "radio");
        this.inputTypes.put("Document", "file");
        this.inputTypes.put("DatePicker", "date");

        
        cfg = new Configuration(Configuration.VERSION_2_3_26);
        cfg.setTemplateLoader(stringLoader);
        cfg.setDefaultEncoding("UTF-8");
        
        loadTemplates();
    }
    
    public void configure(String serverPath, String resources) {
        this.serverPath = serverPath;
        this.resourcePath = serverPath + resources;
    }
    
    public String renderCase(String containerId, CaseDefinition caseDefinition, FormInstance form) {
        List<String> scriptDataList = new ArrayList<>();        
        
        
        StringBuilder jsonTemplate = new StringBuilder();
        // start json template
        jsonTemplate
            .append("{");
        
        appendRoleAssignment(caseDefinition, jsonTemplate);
        
        List<LayoutRow> rows = new ArrayList<>();
        // add data information
        jsonTemplate
            .append("'case-data' : {");

        if (form != null) { 
            FormLayout layout = form.getLayout();
            processFormLayout(form, form, Collections.emptyMap(), Collections.emptyMap(), CASE_LAYOUT_TEMPLATE, jsonTemplate, true, scriptDataList);
            
            // finish json template
            jsonTemplate
                .deleteCharAt(jsonTemplate.length() - 1)
                .append("}")
                .append("}");
            
            rows = layout.getRows();
        } else {
            jsonTemplate
                .append("}")
                .append("}");
        }
        StringBuilder caseEndpoint = new StringBuilder();
        caseEndpoint
            .append(serverPath)
            .append("/containers/")
            .append(containerId)
            .append("/cases/")
            .append(caseDefinition.getId())
            .append("/instances");
           
        scriptDataList.add(buildFunctionWithBody("getData", "return " + jsonTemplate.toString()));
        scriptDataList.add(buildFunctionWithBody("getCaseEndpoint", "return '" + caseEndpoint.toString() + "';"));
        scriptDataList.add(buildFunctionWithBody("initializeForm", ""));
        
        // render layout with data
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("roles", caseDefinition.getCaseRoles());
        parameters.put("rows", rows);
        parameters.put("renderButtons", true);
        
        String output = renderTemplate(CASE_LAYOUT_TEMPLATE, parameters);
                
        // render master template
        parameters = new HashMap<>();
        parameters.put(HEADER_LAYOUT_TEMPLATE, form != null ? form.getName() : "Case form");
        parameters.put("body", output);        
        parameters.put("scriptData", buildScriptData(scriptDataList));
        parameters.put("serverPath", resourcePath);
        String finalOutput = renderTemplate(MASTER_LAYOUT_TEMPLATE, parameters);
        
        return finalOutput;
    }


    public String renderProcess(String containerId, ProcessDefinition processDesc, FormInstance form) {
        
        List<String> scriptDataList = new ArrayList<>();        
        
        FormLayout layout = form.getLayout();
        StringBuilder jsonTemplate = new StringBuilder();
        // start json template
        jsonTemplate
            .append("{");

        processFormLayout(form, form, Collections.emptyMap(), Collections.emptyMap(), PROCESS_LAYOUT_TEMPLATE, jsonTemplate, true, scriptDataList);
        
        // finish json template
        jsonTemplate
            .deleteCharAt(jsonTemplate.length() - 1)
            .append("}");
        StringBuilder processEndpoint = new StringBuilder();
        processEndpoint
            .append(serverPath)
            .append("/containers/")
            .append(containerId)
            .append("/processes/")
            .append(processDesc.getId())
            .append("/instances");
           
        scriptDataList.add(buildFunctionWithBody("getData", "return " + jsonTemplate.toString()));
        scriptDataList.add(buildFunctionWithBody("getProcessEndpoint", "return '" + processEndpoint.toString() + "';"));
        scriptDataList.add(buildFunctionWithBody("initializeForm", ""));
        
        // render layout with data
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("rows", layout.getRows());
        parameters.put("renderButtons", true);
        String output = renderTemplate(PROCESS_LAYOUT_TEMPLATE, parameters);
                
        // render master template
        parameters = new HashMap<>();
        parameters.put(HEADER_LAYOUT_TEMPLATE, form.getName());
        parameters.put("body", output);        
        parameters.put("scriptData", buildScriptData(scriptDataList));
        parameters.put("serverPath", resourcePath);
        String finalOutput = renderTemplate(MASTER_LAYOUT_TEMPLATE, parameters);
        
        return finalOutput;
    }
    
    
    public String renderTask(String containerId, Task task, FormInstance form, Map<String, Object> inputs, Map<String, Object> outputs) {
        List<String> scriptDataList = new ArrayList<>();        
        
        FormLayout layout = form.getLayout();
        StringBuilder jsonTemplate = new StringBuilder();
        
        // start json template
        jsonTemplate
            .append("{");
        processFormLayout(form, form, inputs, outputs, TASK_LAYOUT_TEMPLATE, jsonTemplate, true, scriptDataList);

        // finish json template
        jsonTemplate
            .deleteCharAt(jsonTemplate.length() - 1)
            .append("}");
        
        StringBuilder taskEndpoint = new StringBuilder();
        taskEndpoint
            .append(serverPath)
            .append("/containers/")
            .append(containerId)
            .append("/tasks/")
            .append(task.getId());
        
        scriptDataList.add(buildFunctionWithBody("getData", "return " + jsonTemplate.toString()));
        scriptDataList.add(buildFunctionWithBody("getTaskEndpoint", "return '" + taskEndpoint.toString() + "';"));
        scriptDataList.add(buildFunctionWithBody("initializeForm", "taskStatus = '" + task.getTaskData().getStatus().name() + "';initTaskButtons();"));
        
        // render layout with data
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("rows", layout.getRows());
        parameters.put("renderButtons", true);
        String output = renderTemplate(TASK_LAYOUT_TEMPLATE, parameters);
                
        // render master template
        parameters = new HashMap<>();
        parameters.put(HEADER_LAYOUT_TEMPLATE, form.getName());
        parameters.put("body", output);
        parameters.put("scriptData", buildScriptData(scriptDataList));
        parameters.put("serverPath", resourcePath);
        String finalOutput = renderTemplate(MASTER_LAYOUT_TEMPLATE, parameters);
        
        return finalOutput;
    }
    
    /**
     * Renders the entire form (including any subforms if found as nested forms)
     * @param topLevelForm - the top level form to be rendered, if needed it should include all nested forms inside or lookup mechanism
     * @param form - form being currently processed - could be top level (on first iteration) or nested (on subsequent iterations)
     * @param inputs - data inputs or empty list
     * @param outputs - data outputs or empty list
     * @param layoutTemplate - layout template to be applied when processing form content
     * @param jsonTemplate - JSON template on how to retrieve data from rendered form - it's constantly updated by each form being processed
     * @param wrapJson - indicates if complex type information should be wrapped with type - usually it's set to true only multi subforms change it to false
     * @param scriptDataList - list of JS functions and code to be placed into the rendered html - at the end of the page
     */
    protected void processFormLayout(FormInstance topLevelForm, 
            FormInstance form, 
            Map<String, Object> inputs, 
            Map<String, Object> outputs, 
            String layoutTemplate, 
            StringBuilder jsonTemplate, 
            boolean wrapJson,
            List<String> scriptDataList) {
        FormLayout layout = form.getLayout();
        
        if (form.getModel().getClassName() != null && wrapJson) {
            jsonTemplate                
                .append("'")
                .append(form.getModel().getName())
                .append("' : ")
                .append("{")
                .append("'")
                .append(form.getModel().getClassName())                
                .append("' : ")
                .append("{");
        }
        
        
        for (LayoutRow row : layout.getRows()) {
            row.setHeader(form.getName());
            for (LayoutColumn column : row.getColumns()) {
                // append each item's content into the builder
                StringBuilder content = new StringBuilder();
                
                for (LayoutItem item : column.getItems()) {
                    if (item.getValue() != null) {
                        content.append(item.getValue());
                    } else {
                        FormField field = form.getField(item.getFieldId());
                        
                        if (field.getNestedForm() != null && !field.getNestedForm().isEmpty()) {
                            // handle subform
                            handleSubForm(topLevelForm, field, inputs, outputs, layoutTemplate, jsonTemplate, wrapJson, scriptDataList, content);
                            continue;
                        } else if (field.getCreationForm() != null) {
                            // handle multi subforms
                            handleMultiSubForm(topLevelForm, field, inputs, outputs, layoutTemplate, jsonTemplate, scriptDataList, content);
                            continue;
                        }
                        
                        // handle regular fields in the form 
                        String fieldType = inputTypes.get(field.getCode());
                        if (fieldType != null) {
                                                    
                            String jsType = getFieldType(field.getType());
                            
                            item.setId(field.getId());
                            item.setName(nonNull(field.getName()));
                            item.setLabel(nonNull(field.getLabel()));
                            item.setPlaceHolder(nonNull(field.getPlaceHolder()));
                            item.setType(fieldType);
                            item.setValue("");
                            item.setOptions(field.getOptions());
                            item.setPattern(getValidationPatternByType(field.getType()));
                            
                            if (inputs.get(field.getBinding()) != null) {
                                item.setValue(inputs.get(field.getBinding()).toString());
                            }
                            if (outputs.get(field.getBinding()) != null) {
                                item.setValue(outputs.get(field.getBinding()).toString());
                            }
                            
                            item.setReadOnly(field.isReadOnly());
                            item.setRequired(field.isRequired());
                            
                            // generate column content                    
                            Map<String, Object> parameters = new HashMap<>();
                            parameters.put("item", item);
                            parameters.put("serverPath", serverPath); // used to generate link for documents
                            String output = renderTemplate(FORM_GROUP_LAYOUT_TEMPLATE, parameters);
                            // append rendered content to the column content
                            content.append(output);
                            
                            // add the field to json template
                            appendFieldJSON(jsonTemplate, fieldType, field.getBinding(), field.getId(), jsType);
                        } else {
                            logger.warn("Field type {} is not supported, skipping it...", field.getCode());
                        }
                    }
                }
                
                column.setContent(content.toString());
            }
        }

        if (form.getModel().getClassName() != null && wrapJson) {
            jsonTemplate
                .deleteCharAt(jsonTemplate.length() - 1)
                .append("}")
                .append("}")
                .append(",");
        }        
    }
    
    protected void handleSubForm(FormInstance topLevelForm, 
            FormField field, 
            Map<String, Object> inputs, 
            Map<String, Object> outputs, 
            String layoutTemplate, 
            StringBuilder jsonTemplate, 
            boolean wrapJson,
            List<String> scriptDataList,
            StringBuilder content) {
        
        FormInstance nestedForm = topLevelForm.getNestedForm(field.getNestedForm());
        if (nestedForm == null) {
            throw new RuntimeException("Unable to find nested form with form id " + field.getNestedForm());
        }
        Map<String, Object> nestedInputs = new HashMap<>(inputs); 
        Map<String, Object> nestedOutputs = new HashMap<>(outputs);
        // extract properties of the binding
        Object binding = outputs.get(field.getBinding());
        if (binding == null) {
            binding = inputs.get(field.getBinding());
            Map<String, Object> nestedDataExtracted = reader.extractValues(binding);
            nestedInputs.putAll(nestedDataExtracted);
        } else {
            Map<String, Object> nestedDataExtracted = reader.extractValues(binding);
            nestedOutputs.putAll(nestedDataExtracted);
        }
                                    
        processFormLayout(topLevelForm, nestedForm, nestedInputs, nestedOutputs, layoutTemplate, jsonTemplate, wrapJson, scriptDataList);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("header", nestedForm.getName());
        String output = renderTemplate(HEADER_LAYOUT_TEMPLATE, parameters);
        content.append(output);                         
        FormLayout nestedLayout = nestedForm.getLayout();
        
        parameters = new HashMap<>();
        parameters.put("rows", nestedLayout.getRows());
        output = renderTemplate(layoutTemplate, parameters);
        // append rendered content to the column content
        content.append(output);
    }
    
    protected void handleMultiSubForm(FormInstance topLevelForm, 
            FormField field, 
            Map<String, Object> inputs, 
            Map<String, Object> outputs, 
            String layoutTemplate, 
            StringBuilder jsonTemplate, 
            List<String> scriptDataList,
            StringBuilder content) {
        
        FormInstance nestedForm = topLevelForm.getNestedForm(field.getCreationForm());                  
        if (nestedForm == null) {
            throw new RuntimeException("Unable to find creation form with form id " + field.getCreationForm());
        }
        // extract and set type of the nested fields
        for (TableInfo tableInfo : field.getTableInfo()) {
        
            FormField nestedField = nestedForm.getFieldByBinding(tableInfo.getProperty());
            String jsType = getFieldType(nestedField.getType());
            tableInfo.setType(jsType);
        }
                                    
        Object bindingData = outputs.get(field.getBinding());
        if (bindingData == null) {
            bindingData = inputs.get(field.getBinding());                                
        }
        
        // build initial JSON content for the data
        if (bindingData != null && bindingData instanceof Collection<?>) {
            Map<String, Object> mappedData = new LinkedHashMap<>();
            
            int index = 0;
            for (Object data : ((Collection<?>)bindingData)) {
                mappedData.put("table_" + field.getId() + "_" + index, data);
                index++;
            }
            
            String jsonData = reader.toJson(mappedData);                                
            String loadDataScript = "tableData.set('table_" + field.getId() +"', new Map(Object.entries(JSON.parse('" + jsonData + "'))));";
            scriptDataList.add(loadDataScript);
        }
        
        StringBuilder creationJsonTemplate = new  StringBuilder("{");
        processFormLayout(topLevelForm, nestedForm, inputs, outputs, layoutTemplate, creationJsonTemplate, false, scriptDataList);
        creationJsonTemplate
            .deleteCharAt(creationJsonTemplate.length() - 1)
            .append("}");    
        
        scriptDataList.add(buildFunctionWithBody("formData_" + field.getId(), " return " + creationJsonTemplate.toString() + ";"));
        
        FormLayout nestedLayout = nestedForm.getLayout();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("rows", nestedLayout.getRows());
        String output = renderTemplate(layoutTemplate, parameters);
        
        
        parameters = new HashMap<>();
        parameters.put("tableColumns", field.getTableInfo());
        parameters.put("tableData", bindingData);
        parameters.put("fieldId", field.getId());
        parameters.put("label", field.getLabel());
        parameters.put("type", field.getType());                            
        parameters.put("creationForm", output);
        output = renderTemplate(TABLE_LAYOUT_TEMPLATE, parameters);                                                    
        
        // append rendered content to the column content
        content.append(output);
        
        // add json template for the table
        jsonTemplate
            .append("'")
            .append(field.getBinding())
            .append("' : getTableData('table_")
            .append(field.getId())                            
            .append("')")
            .append(",");
    }

    protected abstract void loadTemplates();
    
    /*
     * Utility methods
     */
    
    
    protected void loadTemplate(String templateId, InputStream stream) {
        
        try {
            this.stringLoader.putTemplate(templateId, read(stream));
            
            logger.debug("Loaded template {} from input stream", templateId);
        } catch (Exception e) {
            logger.warn("Exception while loading template from input stream due to {}", e.getMessage(), e);
        }
    }
    
    protected String read(InputStream input) {
        String lineSeparator = System.getProperty("line.separator");

        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(input, Charset.forName("UTF-8")))) {
            return buffer.lines().collect(Collectors.joining(lineSeparator));
        } catch (Exception e) {
            return null;
        }
    }
    
    protected String renderTemplate(String templateName, Map<String, Object> parameters) {
        StringWriter out = new StringWriter();
        try {
            Template template = cfg.getTemplate(templateName);
            
            template.process(parameters, out);
        } catch (Exception e) {
            throw new IllegalArgumentException("Template " + templateName + " not found", e);
        }
        return out.toString();
    }
    
    /*
     * json processing utilities     
     */
    
    protected void appendFieldJSON(StringBuilder jsonTemplate, String type, String name, String id, String jsType) {
        jsonTemplate
            .append("'")
            .append(name)
            .append("' : ")
            .append(jsType)
            .append(appendExtractionExpression(type, name, id, jsType))
            .append(")")
            .append(",");
    
    }
    
    protected String appendExtractionExpression(String type, String name, String id, String jsType) {
        StringBuilder jsonTemplate = new StringBuilder();
        if (type.equals("radio")) {
            jsonTemplate            
                .append("$('input[name=")
                .append(name)
                .append("]:checked').val()");
        } else if (type.equals("select")) {
            jsonTemplate            
                .append("$('#")
                .append(id)
                .append("').val()");                
        } else if (type.equals("file")) {
            jsonTemplate            
                .append("getDocumentData('")
                .append(id)
                .append("')");                
        } else if (type.equals("date")) {
            jsonTemplate            
                .append("getDateFormated('")
                .append(id)
                .append("')");                
        }  else {
            jsonTemplate            
                .append("document.getElementById('")
                .append(id)
                .append("')")
                .append(getExtractionValue(jsType));
        }
        
        return jsonTemplate.toString();
    }
    
    protected void appendRoleAssignment(CaseDefinition caseDefinition, StringBuilder jsonTemplate) {
        // add case role assignment
        jsonTemplate
            .append("'case-user-assignments' : {");
        
        for (CaseRole role :  caseDefinition.getCaseRoles()) {
            appendFieldJSON(jsonTemplate, "text", role.getName(), "user_" + role.getName(), "String(");
        }
        
        jsonTemplate
            .deleteCharAt(jsonTemplate.length() - 1)
            .append("}, ");
        
        jsonTemplate
            .append("'case-group-assignments' : {");
        
        for (CaseRole role :  caseDefinition.getCaseRoles()) {
            appendFieldJSON(jsonTemplate, "text", role.getName(), "group_" + role.getName(), "String(");
        }
        
        jsonTemplate
            .deleteCharAt(jsonTemplate.length() - 1)
            .append("}, ");
        
    }
    
    protected String buildFunctionWithBody(String name, String body) {
 
        StringBuilder function = new StringBuilder();
        
        return function
            .append("function ")
            .append(name)
            .append("() { \n")
            .append(body)
            .append("\n };")
            .toString();        
    }
    
    protected String buildScriptData(List<String> scriptDataList) {
        StringBuilder scripts = new StringBuilder();
        
        for (String script : scriptDataList) {
            scripts.append(script).append("\n");
        }
        
        return scripts.toString();
    }
    
    protected String getFieldType(String type) {
        if (type.contains("Integer") || type.contains("Double") || type.contains("Float")) {
            return "Number(";
        } else if (type.contains("Boolean")) {
            return "Boolean(";
        } else if (type.contains("Document") || type.contains("Date")) {
            return "Object(";
        } else {
            return "String(";
        }
    }
    
    protected String getExtractionValue(String jsType) {
        if (jsType.equals("Boolean(")) {
            return ".checked";
        } else {
            return ".value";
        }
    }
    
    protected String getValidationPatternByType(String type) {
        if (type.contains("Integer")) {
            return "^\\d+$";
        } else if (type.contains("Double") || type.contains("Float")) {
            return "^\\d+(\\.\\d+)?$";
        } else if (type.contains("Date")) {
            return "(\\d+)(-|\\/)(\\d+)(?:-|\\/)(?:(\\d+)\\s+(\\d+):(\\d+)(?::(\\d+))?(?:\\.(\\d+))?)?";
        } else {
            return "";
        }
    }
    
    
    protected String nonNull(String value) {
        if (value == null) {
            return "";
        }
        
        return value;
    }
}

