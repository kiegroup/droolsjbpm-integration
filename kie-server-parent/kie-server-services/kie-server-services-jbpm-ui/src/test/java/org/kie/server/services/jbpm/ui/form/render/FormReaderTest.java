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

import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.kie.server.services.jbpm.ui.form.render.model.FormField;
import org.kie.server.services.jbpm.ui.form.render.model.FormInstance;
import org.kie.server.services.jbpm.ui.form.render.model.FormLayout;
import org.kie.server.services.jbpm.ui.form.render.model.LayoutColumn;
import org.kie.server.services.jbpm.ui.form.render.model.LayoutItem;
import org.kie.server.services.jbpm.ui.form.render.model.LayoutRow;

public class FormReaderTest {

    @Test
    public void testReadBasicForm() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/hiring-taskform.json"));
        assertThat(form).isNotNull();
        assertThat(form.getId()).isEqualTo("011ebd63-f9b5-4057-b5ff-999c1c7e5080");
        assertThat(form.getName()).isEqualTo("hiring-taskform.frm");
        
        assertThat(form.getFields()).hasSize(1);
        
        FormField field = form.getFields().get(0);
        assertField(field, 
                "field_2225717094101704E12",
                "name",
                "TextBox",
                "Candidate Name",
                100,
                "name",
                "Enter your name",
                "java.lang.String",
                false,
                false);        
        
        FormLayout layout = form.getLayout();
        assertThat(layout).isNotNull();
        assertThat(layout.getRows()).hasSize(1);
        
        LayoutRow row = layout.getRows().get(0);
        assertThat(row.getColumns()).hasSize(1);
        
        LayoutColumn column = row.getColumns().get(0);
        assertColumn(column, "12", 1, "011ebd63-f9b5-4057-b5ff-999c1c7e5080", "field_2225717094101704E12");    
        
    }
    
    @Test
    public void testReadBasicFormWithModel() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/property-form.json"));
        assertThat(form).isNotNull();
        assertThat(form.getId()).isEqualTo("2aeaf281-71e1-45a5-9ab3-0abd855d924e");
        assertThat(form.getName()).isEqualTo("Property");
        
        assertThat(form.getModel()).isNotNull();
        assertThat(form.getModel().getClassName()).isEqualTo("com.myspace.mortgage_app.Property");
        assertThat(form.getModel().getName()).isEqualTo("property");
        
        assertThat(form.getFields()).hasSize(4);  
        
        FormField field = form.getFields().get(0);
        assertField(field, 
                "field_815717729253767E11",
                "age",
                "IntegerBox",
                "Age of property",
                100,
                "age",
                "Age of property",
                "java.lang.Integer",
                false,
                false); 
        
        field = form.getFields().get(1);
        assertField(field, 
                "field_236289653097941E11",
                "address",
                "TextBox",
                "Address of property",
                100,
                "address",
                "Address of property",
                "java.lang.String",
                false,
                false);
        
        field = form.getFields().get(2);
        assertField(field, 
                "field_9471909295199063E11",
                "locale",
                "TextBox",
                "Locale",
                100,
                "locale",
                "Locale",
                "java.lang.String",
                false,
                false);
        
        field = form.getFields().get(3);
        assertField(field, 
                "field_4113393327260706E12",
                "saleprice",
                "IntegerBox",
                "Sale Price",
                100,
                "saleprice",
                "Sale Price",
                "java.lang.Integer",
                false,
                false);
        
        FormLayout layout = form.getLayout();
        assertThat(layout).isNotNull();
        assertThat(layout.getRows()).hasSize(4);
        
        LayoutRow row = layout.getRows().get(0);
        assertThat(row.getColumns()).hasSize(1);
        
        LayoutColumn column = row.getColumns().get(0);
        assertColumn(column, "12", 1, "2aeaf281-71e1-45a5-9ab3-0abd855d924e", "field_815717729253767E11");
        
        row = layout.getRows().get(1);
        assertThat(row.getColumns()).hasSize(1);
        column = row.getColumns().get(0);
        assertColumn(column, "12", 1, "2aeaf281-71e1-45a5-9ab3-0abd855d924e", "field_236289653097941E11");
        
        row = layout.getRows().get(2);
        assertThat(row.getColumns()).hasSize(1);
        column = row.getColumns().get(0);
        assertColumn(column, "12", 1, "2aeaf281-71e1-45a5-9ab3-0abd855d924e", "field_9471909295199063E11");
        
        row = layout.getRows().get(3);
        assertThat(row.getColumns()).hasSize(1);
        column = row.getColumns().get(0);
        assertColumn(column, "12", 1, "2aeaf281-71e1-45a5-9ab3-0abd855d924e", "field_4113393327260706E12");       
        
    }
    
    @Test
    public void testReadMultiSubForm() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/order-taskform.json"));
        assertThat(form).isNotNull();
        assertThat(form.getId()).isEqualTo("d8d455cf-f40e-4b89-93d4-f26b2d7e1aff");
        assertThat(form.getName()).isEqualTo("Order");
        
        assertThat(form.getFields()).hasSize(4);
        
        FormField field = form.getFields().get(3);
        assertField(field, 
                "field_180962688550559E11",
                "items",
                "MultipleSubForm",
                "Items",
                0,
                "items",
                null,
                "com.myspace.form_rendering.Item",
                false,
                false);      
        
        assertThat(field.getCreationForm()).isEqualTo("40e31ab4-7eb8-404c-96f1-ce131f00d49f");
        assertThat(field.getEditionForm()).isEqualTo("40e31ab4-7eb8-404c-96f1-ce131f00d49f");
        assertThat(field.getTableInfo()).hasSize(3);
        
        assertThat(field.getTableInfo().get(0).getLabel()).isEqualTo("Name");
        assertThat(field.getTableInfo().get(0).getProperty()).isEqualTo("name");
        
        assertThat(field.getTableInfo().get(1).getLabel()).isEqualTo("Quantity");
        assertThat(field.getTableInfo().get(1).getProperty()).isEqualTo("quantity");
        
        assertThat(field.getTableInfo().get(2).getLabel()).isEqualTo("Price");
        assertThat(field.getTableInfo().get(2).getProperty()).isEqualTo("price");
        
        FormLayout layout = form.getLayout();
        assertThat(layout).isNotNull();
        assertThat(layout.getRows()).hasSize(4);
        
    }
    
    @Test
    public void testReadMultiLayerFormForm() {
        
        FormReader reader = new FormReader();
        
        FormInstance form = reader.readFromStream(this.getClass().getResourceAsStream("/many-layers-layout-form.json"));
        assertThat(form).isNotNull();
        assertThat(form.getId()).isEqualTo("8548ee2c-5f81-4502-81ba-f649c6dbbd21");
        assertThat(form.getName()).isEqualTo("usertaskprocess-taskform.frm");
        
        assertThat(form.getFields()).hasSize(2);               
        
        FormLayout layout = form.getLayout();
        assertThat(layout).isNotNull();
        assertThat(layout.getRows()).hasSize(1);
        
        LayoutRow row = layout.getRows().get(0);
        assertThat(row.getColumns()).hasSize(1);
        
        LayoutColumn column = row.getColumns().get(0);        
        assertThat(column.getItems()).hasSize(2);

    }
    
    protected void assertField(FormField field, String id, String binding, String code, String label, int maxLength, String name, String placeHolder, String type, boolean readOnly, boolean required) {
        assertThat(field.getId()).isEqualTo(id);
        assertThat(field.getBinding()).isEqualTo(binding);
        assertThat(field.getCode()).isEqualTo(code);
        assertThat(field.getLabel()).isEqualTo(label);
        assertThat(field.getMaxLength()).isEqualTo(maxLength);
        assertThat(field.getName()).isEqualTo(name);
        assertThat(field.getPlaceHolder()).isEqualTo(placeHolder);
        assertThat(field.getType()).isEqualTo(type);
        assertThat(field.isReadOnly()).isEqualTo(readOnly);
        assertThat(field.isRequired()).isEqualTo(required);
    }
    
    protected void assertColumn(LayoutColumn column, String span, int itemSize, String formId, String fieldId) {
        assertThat(column.getSpan()).isEqualTo(span);
        assertThat(column.getItems()).hasSize(itemSize);
        
        LayoutItem item = column.getItems().get(0);
        assertThat(item.getFormId()).isEqualTo(formId);
        assertThat(item.getFieldId()).isEqualTo(fieldId);
    }
}
