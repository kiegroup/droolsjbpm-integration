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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BootstrapFormRenderer extends AbstractFormRenderer {
    
    private static final Logger logger = LoggerFactory.getLogger(BootstrapFormRenderer.class);
    
    private static final String NAME = "bootstrap";
    
    public BootstrapFormRenderer() {
        super(null,  null);
    }

    public BootstrapFormRenderer(String serverPath, String resources) {
        super(serverPath, resources);
    }
    
    protected void loadTemplates() {
        loadTemplate(MASTER_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/bootstrap/master-template.html"));
        loadTemplate(PROCESS_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/bootstrap/process-layout-template.html"));
        loadTemplate(TASK_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/bootstrap/task-layout-template.html"));
        loadTemplate(FORM_GROUP_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/bootstrap/input-form-group-template.html"));
        loadTemplate(HEADER_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/bootstrap/header-template.html"));
        loadTemplate(CASE_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/bootstrap/case-layout-template.html"));
        
        logger.info("Boostrap Form renderer templates loaded successfully.");
    }
    
    @Override
    public String getName() {
        return NAME;
    }
}

