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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.casemgmt.api.model.CaseRole;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
import org.jbpm.services.api.model.ProcessDefinition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;
import org.kie.api.task.model.TaskData;
import org.kie.server.services.jbpm.ui.form.render.data.Item;
import org.kie.server.services.jbpm.ui.form.render.model.FormInstance;
import org.mockito.Mockito;

@RunWith(Parameterized.class)
public class FormRendererTest {
    
    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        FormRenderer patternfly = new PatternflyFormRenderer("file://" + new File("src/main/resources").getAbsolutePath(), "/form-templates-providers");

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {patternfly}
                        }
        ));


        
        FormRenderer bootstrap  = new BootstrapFormRenderer("file://" + new File("src/main/resources").getAbsolutePath(), "/form-templates-providers");
        parameterData.addAll(Arrays.asList(new Object[][]
                        {
                                {bootstrap}
                        })
        );
        

        return parameterData;
    }
    @Parameterized.Parameter(0)
    public FormRenderer renderer;

    @Test
    public void testRenderOfBasicFormNoData() {
                        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/hiring-taskform.json"));
        assertThat(form).isNotNull();
        
        ProcessDefinition processDefinition = newProcessDefinition("");
        
        String renderedForm = renderer.renderProcess("", processDefinition, form);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfBasicFormNoData.html", renderedForm);
    }
       
    @Test
    public void testRenderOfBasicFormMultipleRowsNoData() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/createProposal-taskform.json"));
        assertThat(form).isNotNull();
        
        ProcessDefinition processDefinition = newProcessDefinition("");
        
        String renderedForm = renderer.renderProcess("", processDefinition, form);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfBasicFormMultipleRowsNoData.html", renderedForm);
    }
    
    @Test
    public void testRenderOfBasicTaskFormNoData() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/createProposal-taskform.json"));
        assertThat(form).isNotNull();
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("offering", 1000);
        inputs.put("tech_score", 123);
        Map<String, Object> outputs = new HashMap<>();
        
        Task task = newTask(0L, "Ready");
        
        String renderedForm = renderer.renderTask("", task, form, inputs, outputs);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfBasicTaskFormNoData.html", renderedForm);
    }
    
    @Test
    public void testRenderOfBasicFormCustomData() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/property-form.json"));
        assertThat(form).isNotNull();        
    
        ProcessDefinition processDefinition = newProcessDefinition("");
        
        String renderedForm = renderer.renderProcess("", processDefinition, form);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfBasicFormCustomData.html", renderedForm);
    }
    
    @Test
    public void testRenderOfBasicFormCustomDataApplicant() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/applicant-form.json"));
        assertThat(form).isNotNull();
            
        ProcessDefinition processDefinition = newProcessDefinition("");
        
        String renderedForm = renderer.renderProcess("", processDefinition, form);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfBasicFormCustomDataApplicant.html", renderedForm);
    }
    
    @Test
    public void testRenderOfNestedFormCustomData() {
        
        FormReader reader = new FormReader();
        
        FormInstance applicant = reader.readFromStream(this.getClass().getResourceAsStream("/applicant-form.json"));
        assertThat(applicant).isNotNull();
        FormInstance property = reader.readFromStream(this.getClass().getResourceAsStream("/property-form.json"));
        assertThat(property).isNotNull();
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/application-form.json"));
        assertThat(form).isNotNull();
        
        form.addNestedForm(applicant);
        form.addNestedForm(property);
            
        ProcessDefinition processDefinition = newProcessDefinition("");
        
        String renderedForm = renderer.renderProcess("", processDefinition, form);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfNestedFormCustomData.html", renderedForm);
    }
    
    @Test
    public void testRenderOfAllInOneFormCustomDataApplicant() {
        
        FormReader reader = new FormReader();
        
        FormInstance applicant = reader.readFromStream(this.getClass().getResourceAsStream("/applicant-form.json"));
        assertThat(applicant).isNotNull();
        FormInstance property = reader.readFromStream(this.getClass().getResourceAsStream("/property-form.json"));
        assertThat(property).isNotNull();
        FormInstance application = reader.readFromStream(this.getClass().getResourceAsStream("/complete-application-form.json"));
        assertThat(application).isNotNull();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/composite-form.json"));
        assertThat(form).isNotNull();
        
        form.addNestedForm(application);
        form.addNestedForm(applicant);
        form.addNestedForm(property);
            
        ProcessDefinition processDefinition = newProcessDefinition("");
        
        String renderedForm = renderer.renderProcess("", processDefinition, form);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfAllInOneFormCustomDataApplicant.html", renderedForm);
    }
    
    @Test
    public void testRenderOfBasicCaseFormNoData() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/hiring-taskform.json"));
        assertThat(form).isNotNull();
        
        CaseDefinition caseDefinition = newCaseDefinition("test", "admin", "owner");
        
        String renderedForm = renderer.renderCase("", caseDefinition, form);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfBasicCaseFormNoData.html", renderedForm);
    }
    
    @Test
    public void testRenderOfBasicFormWithSelectRadioGroup() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/various-fields-taskform.json"));
        assertThat(form).isNotNull();
        
        ProcessDefinition processDefinition = newProcessDefinition("");
        
        String renderedForm = renderer.renderProcess("", processDefinition, form);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfBasicFormWithSelectRadioGroup.html", renderedForm);
    }
    
    @Test
    public void testRenderOfBasicTaskFormWithSelectRadioGroupAndData() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/various-fields-taskform.json"));
        assertThat(form).isNotNull();
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("selection", "another");
        inputs.put("radio", "radio2");
        inputs.put("decimal", 123.5);
        inputs.put("hwSpec_", "name####123####111####id");
        Map<String, Object> outputs = new HashMap<>();
        
        Task task = newTask(0L, "Ready");
        
        String renderedForm = renderer.renderTask("", task, form, inputs, outputs);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfBasicTaskFormWithSelectRadioGroupAndData.html", renderedForm);
    }
    
    @Test
    public void testRenderOfMultiSubFormNoData() {
                        
        FormReader reader = new FormReader();
        
        FormInstance itemForm = reader.readFromStream(this.getClass().getResourceAsStream("/item-taskform.json"));
        assertThat(itemForm).isNotNull();
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/order-taskform.json"));
        assertThat(form).isNotNull();
        form.addNestedForm(itemForm);
        
        ProcessDefinition processDefinition = newProcessDefinition("");
        
        String renderedForm = renderer.renderProcess("", processDefinition, form);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfMultiSubFormNoData.html", renderedForm);
    }
    
    @Test
    public void testRenderOfMultiSubFormWithData() {
                        
        FormReader reader = new FormReader();
        FormInstance itemForm = reader.readFromStream(this.getClass().getResourceAsStream("/item-taskform.json"));
        assertThat(itemForm).isNotNull();
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/order-taskform.json"));
        assertThat(form).isNotNull();
        form.addNestedForm(itemForm);
        
        List<Item> items = new ArrayList<>();
        items.add(new Item("test", 10, 125.50));
        items.add(new Item("another", 4, 25.80));
        
        Map<String, Object> inputs = new HashMap<>();
        inputs.put("items", items);
        inputs.put("orderNumber", "XXX-ZZZ-YYY");
        inputs.put("customer", "John");
        inputs.put("address", "Main Street");
        Map<String, Object> outputs = new HashMap<>();
        
        Task task = newTask(0L, "Ready");
        
        String renderedForm = renderer.renderTask("", task, form, inputs, outputs);
        assertThat(renderedForm).isNotNull();
        writeToFile("testRenderOfMultiSubFormWithData.html", renderedForm);
    }
    
    protected void writeToFile(String fileName, String content) {
        try {
            Files.write(Paths.get("target/" + renderer.getName() + "_" + fileName), content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected ProcessDefinition newProcessDefinition(String processId) {
        ProcessAssetDesc definition = new ProcessAssetDesc();
        definition.setId(processId);
        return definition;
    }
    
    protected Task newTask(Long taskId, String taskStatus) {
        Task task = Mockito.mock(Task.class);
        TaskData taskData = Mockito.mock(TaskData.class);
        
        Mockito.when(task.getId()).thenReturn(taskId);
        Mockito.when(task.getTaskData()).thenReturn(taskData);
        Mockito.when(task.getTaskData().getStatus()).thenReturn(Status.valueOf(taskStatus));
        return task;
    }
    
    protected CaseDefinition newCaseDefinition(String definitionId, String ...roles) {
        CaseDefinition definition = Mockito.mock(CaseDefinition.class);
        Mockito.when(definition.getId()).thenReturn(definitionId);
        
        List<CaseRole> caseRoles = new ArrayList<>();
        for (String role : roles) {
            CaseRole cRole = Mockito.mock(CaseRole.class);
            Mockito.when(cRole.getName()).thenReturn(role);
            caseRoles.add(cRole);
        }
        
        Mockito.when(definition.getCaseRoles()).thenReturn(caseRoles);
        return definition;
    }
}
