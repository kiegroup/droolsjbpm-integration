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

import org.jbpm.casemgmt.api.CaseRuntimeDataService;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.ProcessDefinition;
import org.junit.Test;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormRendererBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(FormRendererBaseTest.class);
    
    @Test
    public void testGenerateDefaultForm() {
        DefinitionService definitionService = mock(DefinitionService.class);
        UserTaskService userTaskService = mock(UserTaskService.class);
        FormManagerService formManagerService = mock(FormManagerService.class);
        CaseRuntimeDataService caseRuntimeDataService = mock(CaseRuntimeDataService.class);
        KieServerRegistry registry = mock(KieServerRegistry.class);
        
        ProcessDefinition processDefinition = mock(ProcessDefinition.class);
        when(processDefinition.getId()).thenReturn("testprocess");
        when(processDefinition.getName()).thenReturn("Test Process");
        
        FormRendererBase rendererBase = new FormRendererBase(definitionService, userTaskService, formManagerService, caseRuntimeDataService, registry);
        
        when(registry.getContainerId(any(), any())).thenReturn("test");
        when(definitionService.getProcessDefinition(eq("test"), eq("test-process"))).thenReturn(processDefinition);
        
        String result = rendererBase.getProcessRenderedForm("patternfly", "test", "test-process");
        logger.debug("Form content is '{}'", result);
        assertThat(result).isNotNull().isNotEmpty();
        
        // it has the patternfly (default renderer) css
        assertThat(result).contains("/files/patternfly/css/patternfly.min.css\" rel=\"stylesheet\">");
        assertThat(result).contains("/files/patternfly/css/patternfly-additions.min.css\" rel=\"stylesheet\">");
        
        // it has required js files
        assertThat(result).contains("/files/patternfly/js/jquery.min.js\"></script>");
        assertThat(result).contains("/files/patternfly/js/patternfly.min.js\"></script>");
        assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                
        // it has the form header
        assertThat(result).contains("<h3 class=\"panel-title\">Default form - Test Process</h3>");
        
    }
    
    @Test
    public void testGenerateDefaultFormWithVariables() {
        DefinitionService definitionService = mock(DefinitionService.class);
        UserTaskService userTaskService = mock(UserTaskService.class);
        FormManagerService formManagerService = mock(FormManagerService.class);
        CaseRuntimeDataService caseRuntimeDataService = mock(CaseRuntimeDataService.class);
        KieServerRegistry registry = mock(KieServerRegistry.class);
        
        ProcessDefinition processDefinition = mock(ProcessDefinition.class);
        when(processDefinition.getId()).thenReturn("testprocess");
        when(processDefinition.getName()).thenReturn("Test Process");

        Map<String, String> variables = new HashMap<>();
        variables.put("name", "java.lang.String");
        variables.put("age", "java.lang.Integer");
        variables.put("maried", "java.lang.Boolean");
        when(processDefinition.getProcessVariables()).thenReturn(variables);
        Map<String, Set<String>> tags = new HashMap<>();
        Set<String> tags1 = new HashSet<>();
        tags1.add("required");
        Set<String> tags2 = new HashSet<>();
        tags2.add("readonly");
        tags.put("name", tags1);
        tags.put("maried", tags2);
        when(processDefinition.getTagsInfo()).thenReturn(tags);
        when(processDefinition.getTagsForVariable(eq("name"))).thenReturn(tags1);
        when(processDefinition.getTagsForVariable(eq("maried"))).thenReturn(tags2);
        FormRendererBase rendererBase = new FormRendererBase(definitionService, userTaskService, formManagerService, caseRuntimeDataService, registry);
        
        when(registry.getContainerId(any(), any())).thenReturn("test");
        when(definitionService.getProcessDefinition(eq("test"), eq("test-process"))).thenReturn(processDefinition);
        
        String result = rendererBase.getProcessRenderedForm("patternfly", "test", "test-process");
        // it has the patternfly (default renderer) css
        assertThat(result).contains("/files/patternfly/css/patternfly.min.css\" rel=\"stylesheet\">");
        assertThat(result).contains("/files/patternfly/css/patternfly-additions.min.css\" rel=\"stylesheet\">");
        
        // it has required js files
        assertThat(result).contains("/files/patternfly/js/jquery.min.js\"></script>");
        assertThat(result).contains("/files/patternfly/js/patternfly.min.js\"></script>");
        assertThat(result).contains("/files/js/kieserver-ui.js\"></script>");
                
        // it has the form header
        assertThat(result).contains("<h3 class=\"panel-title\">Default form - Test Process</h3>");
        
        // it has all three variables rendered
        Pattern namePattern = Pattern.compile("<input name=\"name\" type=\"text\" class=\"form-control\".*required.*>");
        Matcher nameMatcher = namePattern.matcher(result);
        assertTrue(nameMatcher.find());
        Pattern mariedPattern = Pattern.compile("<input name=\"maried\" type=\"checkbox\" class=\"form-control\".*readonly.*>");
        Matcher mariedMatcher = mariedPattern.matcher(result);
        assertTrue(mariedMatcher.find());
        assertThat(result).contains("<input name=\"age\" type=\"text\" class=\"form-control\" ");
        // it has start process button
        assertThat(result).contains("<button type=\"button\" class=\"btn btn-primary\" onclick=\"startProcess(this);\">Submit</button>");
    }
}
