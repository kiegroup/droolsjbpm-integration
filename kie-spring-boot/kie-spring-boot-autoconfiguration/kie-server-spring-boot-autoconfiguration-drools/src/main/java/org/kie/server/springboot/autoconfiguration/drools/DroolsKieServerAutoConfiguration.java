/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.springboot.autoconfiguration.drools;

import java.util.List;
import java.util.Optional;

import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.drools.DroolsKieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.springboot.autoconfiguration.KieServerProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({KieServerImpl.class})
@EnableConfigurationProperties(KieServerProperties.class)
public class DroolsKieServerAutoConfiguration {

    private KieServerProperties properties;

    public DroolsKieServerAutoConfiguration(KieServerProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(name = "droolsServerExtension")
    @ConditionalOnProperty(name = "kieserver.drools.enabled")
    public KieServerExtension droolsServerExtension(Optional<List<AgendaEventListener>> agendaEventListeners,
            Optional<List<RuleRuntimeEventListener>> ruleRuntimeEventListeners) {

        return new DroolsKieServerExtension() {

            @Override
            public void init(KieServerImpl kieServer, KieServerRegistry registry) {
                super.init(kieServer, registry);
                
                rulesExecutionService.setAgendaEventListeners(agendaEventListeners.orElse(null));
                rulesExecutionService.setRuleRuntimeEventListeners(ruleRuntimeEventListeners.orElse(null));
            }
            
        };
    }
}
