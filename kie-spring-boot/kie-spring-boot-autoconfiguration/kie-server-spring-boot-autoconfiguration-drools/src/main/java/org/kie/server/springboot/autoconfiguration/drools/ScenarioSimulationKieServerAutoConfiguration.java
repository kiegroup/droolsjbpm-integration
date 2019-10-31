/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.scenariosimulation.ScenarioSimulationKieServerExtension;
import org.kie.server.springboot.autoconfiguration.KieServerProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({KieServerImpl.class})
@AutoConfigureAfter({DMNKieServerAutoConfiguration.class})
@EnableConfigurationProperties(KieServerProperties.class)
public class ScenarioSimulationKieServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "scenarioSimulationServerExtension")
    @ConditionalOnProperty(name = "kieserver.scenariosimulation.enabled")
    public KieServerExtension scenarioSimulationServerExtension() {
        return new ScenarioSimulationKieServerExtension();
    }
}
