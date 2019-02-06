/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.jbpm.ui.docs;

public class ParameterSamples {

    public static final String JSON = "application/json";
    
    /*
     * 
     * JSON sample payloads
     * 
     */
    public static final String PROCESS_FORM_DEF_JSON = "{\n" + 
            "  \"id\": \"d1e6dd47-b24c-4f93-ba25-337832926113\",\n" + 
            "  \"name\": \"evaluation-taskform.frm\",\n" + 
            "  \"model\": {\n" + 
            "    \"processName\": \"Evaluation\",\n" + 
            "    \"processId\": \"evaluation\",\n" + 
            "    \"name\": \"process\",\n" + 
            "    \"properties\": [\n" + 
            "      {\n" + 
            "        \"name\": \"employee\",\n" + 
            "        \"typeInfo\": {\n" + 
            "          \"type\": \"BASE\",\n" + 
            "          \"className\": \"java.lang.String\",\n" + 
            "          \"multiple\": false\n" + 
            "        },\n" + 
            "        \"metaData\": {\n" + 
            "          \"entries\": [\n" + 
            "            {\n" + 
            "              \"name\": \"field-readOnly\",\n" + 
            "              \"value\": false\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      },\n" + 
            "      {\n" + 
            "        \"name\": \"initiator\",\n" + 
            "        \"typeInfo\": {\n" + 
            "          \"type\": \"BASE\",\n" + 
            "          \"className\": \"java.lang.String\",\n" + 
            "          \"multiple\": false\n" + 
            "        },\n" + 
            "        \"metaData\": {\n" + 
            "          \"entries\": [\n" + 
            "            {\n" + 
            "              \"name\": \"field-readOnly\",\n" + 
            "              \"value\": false\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      },\n" + 
            "      {\n" + 
            "        \"name\": \"performance\",\n" + 
            "        \"typeInfo\": {\n" + 
            "          \"type\": \"BASE\",\n" + 
            "          \"className\": \"java.lang.Integer\",\n" + 
            "          \"multiple\": false\n" + 
            "        },\n" + 
            "        \"metaData\": {\n" + 
            "          \"entries\": [\n" + 
            "            {\n" + 
            "              \"name\": \"field-readOnly\",\n" + 
            "              \"value\": false\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      },\n" + 
            "      {\n" + 
            "        \"name\": \"reason\",\n" + 
            "        \"typeInfo\": {\n" + 
            "          \"type\": \"BASE\",\n" + 
            "          \"className\": \"java.lang.String\",\n" + 
            "          \"multiple\": false\n" + 
            "        },\n" + 
            "        \"metaData\": {\n" + 
            "          \"entries\": [\n" + 
            "            {\n" + 
            "              \"name\": \"field-readOnly\",\n" + 
            "              \"value\": false\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      }\n" + 
            "    ],\n" + 
            "    \"formModelType\": \"org.kie.workbench.common.forms.jbpm.model.authoring.process.BusinessProcessFormModel\"\n" + 
            "  },\n" + 
            "  \"fields\": [\n" + 
            "    {\n" + 
            "      \"maxLength\": 100,\n" + 
            "      \"placeHolder\": \"Employee\",\n" + 
            "      \"id\": \"field_740177746345817E11\",\n" + 
            "      \"name\": \"employee\",\n" + 
            "      \"label\": \"Employee\",\n" + 
            "      \"required\": true,\n" + 
            "      \"readOnly\": false,\n" + 
            "      \"validateOnChange\": true,\n" + 
            "      \"binding\": \"employee\",\n" + 
            "      \"standaloneClassName\": \"java.lang.String\",\n" + 
            "      \"code\": \"TextBox\",\n" + 
            "      \"serializedFieldClassName\": \"org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textBox.definition.TextBoxFieldDefinition\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"placeHolder\": \"Reason\",\n" + 
            "      \"rows\": 4,\n" + 
            "      \"id\": \"field_282038126127015E11\",\n" + 
            "      \"name\": \"reason\",\n" + 
            "      \"label\": \"Reason\",\n" + 
            "      \"required\": true,\n" + 
            "      \"readOnly\": false,\n" + 
            "      \"validateOnChange\": true,\n" + 
            "      \"binding\": \"reason\",\n" + 
            "      \"standaloneClassName\": \"java.lang.String\",\n" + 
            "      \"code\": \"TextArea\",\n" + 
            "      \"serializedFieldClassName\": \"org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.definition.TextAreaFieldDefinition\"\n" + 
            "    }\n" + 
            "  ],\n" + 
            "  \"layoutTemplate\": {\n" + 
            "    \"version\": 2,\n" + 
            "    \"name\": \"evaluation-taskform.frm\",\n" + 
            "    \"style\": \"FLUID\",\n" + 
            "    \"layoutProperties\": {},\n" + 
            "    \"rows\": [\n" + 
            "      {\n" + 
            "        \"height\": \"12\",\n" + 
            "        \"layoutColumns\": [\n" + 
            "          {\n" + 
            "            \"span\": \"12\",\n" + 
            "            \"height\": \"12\",\n" + 
            "            \"rows\": [],\n" + 
            "            \"layoutComponents\": [\n" + 
            "              {\n" + 
            "                \"dragTypeName\": \"org.kie.workbench.common.forms.editor.client.editor.rendering.EditorFieldLayoutComponent\",\n" + 
            "                \"properties\": {\n" + 
            "                  \"field_id\": \"field_740177746345817E11\",\n" + 
            "                  \"form_id\": \"d1e6dd47-b24c-4f93-ba25-337832926113\"\n" + 
            "                }\n" + 
            "              }\n" + 
            "            ]\n" + 
            "          }\n" + 
            "        ]\n" + 
            "      },\n" + 
            "      {\n" + 
            "        \"height\": \"12\",\n" + 
            "        \"layoutColumns\": [\n" + 
            "          {\n" + 
            "            \"span\": \"12\",\n" + 
            "            \"height\": \"12\",\n" + 
            "            \"rows\": [],\n" + 
            "            \"layoutComponents\": [\n" + 
            "              {\n" + 
            "                \"dragTypeName\": \"org.kie.workbench.common.forms.editor.client.editor.rendering.EditorFieldLayoutComponent\",\n" + 
            "                \"properties\": {\n" + 
            "                  \"field_id\": \"field_282038126127015E11\",\n" + 
            "                  \"form_id\": \"d1e6dd47-b24c-4f93-ba25-337832926113\"\n" + 
            "                }\n" + 
            "              }\n" + 
            "            ]\n" + 
            "          }\n" + 
            "        ]\n" + 
            "      }\n" + 
            "    ]\n" + 
            "  }\n" + 
            "}";
    public static final String TASK_FORM_DEF_JSON = "{\n" + 
            "  \"id\": \"47078d21-7da5-4d3f-8355-0fcd78b09f39\",\n" + 
            "  \"name\": \"PerformanceEvaluation-taskform.frm\",\n" + 
            "  \"model\": {\n" + 
            "    \"taskName\": \"PerformanceEvaluation\",\n" + 
            "    \"processId\": \"evaluation\",\n" + 
            "    \"name\": \"task\",\n" + 
            "    \"properties\": [\n" + 
            "      {\n" + 
            "        \"name\": \"BusinessAdministratorId\",\n" + 
            "        \"typeInfo\": {\n" + 
            "          \"type\": \"BASE\",\n" + 
            "          \"className\": \"java.lang.String\",\n" + 
            "          \"multiple\": false\n" + 
            "        },\n" + 
            "        \"metaData\": {\n" + 
            "          \"entries\": [\n" + 
            "            {\n" + 
            "              \"name\": \"field-readOnly\",\n" + 
            "              \"value\": true\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      },\n" + 
            "      {\n" + 
            "        \"name\": \"reason\",\n" + 
            "        \"typeInfo\": {\n" + 
            "          \"type\": \"BASE\",\n" + 
            "          \"className\": \"java.lang.String\",\n" + 
            "          \"multiple\": false\n" + 
            "        },\n" + 
            "        \"metaData\": {\n" + 
            "          \"entries\": [\n" + 
            "            {\n" + 
            "              \"name\": \"field-readOnly\",\n" + 
            "              \"value\": true\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      },\n" + 
            "      {\n" + 
            "        \"name\": \"performance\",\n" + 
            "        \"typeInfo\": {\n" + 
            "          \"type\": \"BASE\",\n" + 
            "          \"className\": \"java.lang.Integer\",\n" + 
            "          \"multiple\": false\n" + 
            "        },\n" + 
            "        \"metaData\": {\n" + 
            "          \"entries\": [\n" + 
            "            {\n" + 
            "              \"name\": \"field-readOnly\",\n" + 
            "              \"value\": false\n" + 
            "            }\n" + 
            "          ]\n" + 
            "        }\n" + 
            "      }\n" + 
            "    ],\n" + 
            "    \"formModelType\": \"org.kie.workbench.common.forms.jbpm.model.authoring.task.TaskFormModel\"\n" + 
            "  },\n" + 
            "  \"fields\": [\n" + 
            "    {\n" + 
            "      \"placeHolder\": \"Reason\",\n" + 
            "      \"rows\": 4,\n" + 
            "      \"id\": \"field_332058348325587E12\",\n" + 
            "      \"name\": \"reason\",\n" + 
            "      \"label\": \"Reason\",\n" + 
            "      \"required\": false,\n" + 
            "      \"readOnly\": true,\n" + 
            "      \"validateOnChange\": true,\n" + 
            "      \"binding\": \"reason\",\n" + 
            "      \"standaloneClassName\": \"java.lang.String\",\n" + 
            "      \"code\": \"TextArea\",\n" + 
            "      \"serializedFieldClassName\": \"org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.textArea.definition.TextAreaFieldDefinition\"\n" + 
            "    },\n" + 
            "    {\n" + 
            "      \"placeHolder\": \"Performance\",\n" + 
            "      \"maxLength\": 100,\n" + 
            "      \"id\": \"field_336003622256354E12\",\n" + 
            "      \"name\": \"performance\",\n" + 
            "      \"label\": \"Performance\",\n" + 
            "      \"required\": true,\n" + 
            "      \"readOnly\": false,\n" + 
            "      \"validateOnChange\": true,\n" + 
            "      \"binding\": \"performance\",\n" + 
            "      \"standaloneClassName\": \"java.lang.Integer\",\n" + 
            "      \"code\": \"IntegerBox\",\n" + 
            "      \"serializedFieldClassName\": \"org.kie.workbench.common.forms.fields.shared.fieldTypes.basic.integerBox.definition.IntegerBoxFieldDefinition\"\n" + 
            "    }\n" + 
            "  ],\n" + 
            "  \"layoutTemplate\": {\n" + 
            "    \"version\": 2,\n" + 
            "    \"name\": \"PerformanceEvaluation-taskform.frm\",\n" + 
            "    \"style\": \"FLUID\",\n" + 
            "    \"layoutProperties\": {},\n" + 
            "    \"rows\": [\n" + 
            "      {\n" + 
            "        \"height\": \"12\",\n" + 
            "        \"layoutColumns\": [\n" + 
            "          {\n" + 
            "            \"span\": \"12\",\n" + 
            "            \"height\": \"12\",\n" + 
            "            \"rows\": [],\n" + 
            "            \"layoutComponents\": [\n" + 
            "              {\n" + 
            "                \"dragTypeName\": \"org.kie.workbench.common.forms.editor.client.editor.rendering.EditorFieldLayoutComponent\",\n" + 
            "                \"properties\": {\n" + 
            "                  \"field_id\": \"field_332058348325587E12\",\n" + 
            "                  \"form_id\": \"47078d21-7da5-4d3f-8355-0fcd78b09f39\"\n" + 
            "                }\n" + 
            "              }\n" + 
            "            ]\n" + 
            "          }\n" + 
            "        ]\n" + 
            "      },\n" + 
            "      {\n" + 
            "        \"height\": \"12\",\n" + 
            "        \"layoutColumns\": [\n" + 
            "          {\n" + 
            "            \"span\": \"12\",\n" + 
            "            \"height\": \"12\",\n" + 
            "            \"rows\": [],\n" + 
            "            \"layoutComponents\": [\n" + 
            "              {\n" + 
            "                \"dragTypeName\": \"org.kie.workbench.common.forms.editor.client.editor.rendering.EditorFieldLayoutComponent\",\n" + 
            "                \"properties\": {\n" + 
            "                  \"field_id\": \"field_336003622256354E12\",\n" + 
            "                  \"form_id\": \"47078d21-7da5-4d3f-8355-0fcd78b09f39\"\n" + 
            "                }\n" + 
            "              }\n" + 
            "            ]\n" + 
            "          }\n" + 
            "        ]\n" + 
            "      }\n" + 
            "    ]\n" + 
            "  }\n" + 
            "}";
}
