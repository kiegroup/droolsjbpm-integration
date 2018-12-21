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

public class PatternflyFormRenderer extends AbstractFormRenderer {
    
    private static final Logger logger = LoggerFactory.getLogger(PatternflyFormRenderer.class);
    
    private static final String NAME = "patternfly";
    
    public PatternflyFormRenderer() {
        super(null,  null);
    }

    public PatternflyFormRenderer(String serverPath, String resources) {
        super(serverPath, resources);
    }
    
    protected void loadTemplates() {
        loadTemplate(MASTER_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/patternfly/master-template.html"));
        loadTemplate(PROCESS_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/patternfly/process-layout-template.html"));
        loadTemplate(TASK_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/patternfly/task-layout-template.html"));
        loadTemplate(FORM_GROUP_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/patternfly/input-form-group-template.html"));
        loadTemplate(HEADER_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/patternfly/header-template.html"));
        loadTemplate(CASE_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/patternfly/case-layout-template.html"));
        loadTemplate(TABLE_LAYOUT_TEMPLATE, this.getClass().getResourceAsStream("/form-templates-providers/patternfly/table-template.html"));
        
        logger.info(getName() + " Form renderer templates loaded successfully.");
    }

    @Override
    public String getName() {
        return NAME;
    }
    
}

