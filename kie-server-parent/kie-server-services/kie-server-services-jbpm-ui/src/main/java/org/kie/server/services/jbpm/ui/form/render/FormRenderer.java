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

import java.util.Map;

import org.jbpm.casemgmt.api.model.CaseDefinition;
import org.jbpm.services.api.model.ProcessDefinition;
import org.kie.api.task.model.Task;
import org.kie.server.services.jbpm.ui.form.render.model.FormInstance;

public interface FormRenderer {

    String getName();
    
    void configure(String serverPath, String resources);
    
    String renderCase(String containerId, CaseDefinition caseDefinition, FormInstance form);
    
    String renderProcess(String containerId, ProcessDefinition processDesc, FormInstance form);
    
    String renderTask(String containerId, Task task, FormInstance form, Map<String, Object> inputs, Map<String, Object> outputs);
}
