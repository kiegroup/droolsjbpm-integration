/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.springboot.autoconfiguration.taskassigning;

import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtension;
import org.kie.server.springboot.autoconfiguration.jbpm.JBPMKieServerAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED;

@Configuration
@ConditionalOnClass({KieServerImpl.class})
@AutoConfigureAfter({JBPMKieServerAutoConfiguration.class})
public class TaskAssigningRuntimeKieServerAutoConfiguration {

    private static final String KIE_SERVER_TASK_ASSIGNING_RUNTIME_ENABLED = "kieserver.taskAssigning.runtime.enabled";

    private String runtimeDisabledValue = null;

    @Bean
    @ConditionalOnMissingBean(name = "taskAssigningRuntimeServerExtension")
    @ConditionalOnProperty(name = KIE_SERVER_TASK_ASSIGNING_RUNTIME_ENABLED)
    public KieServerExtension taskAssigningRuntimeServerExtension() {
        runtimeDisabledValue = System.getProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED);
        System.setProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED, Boolean.FALSE.toString());
        return new TaskAssigningRuntimeKieServerExtension();
    }

    @Bean
    @ConditionalOnMissingBean(name = "taskAssigningPersistenceUnitPostProcessor")
    @ConditionalOnProperty(name = KIE_SERVER_TASK_ASSIGNING_RUNTIME_ENABLED)
    public TaskAssigningPersistenceUnitPostProcessor taskAssigningPersistenceUnitPostProcessor() {
        return new TaskAssigningPersistenceUnitPostProcessor();
    }

    @EventListener
    public void handleContextRefreshEvent(ContextClosedEvent evt) {
        if (runtimeDisabledValue != null) {
            System.setProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED, runtimeDisabledValue);
        } else {
            System.clearProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED);
        }
    }
}