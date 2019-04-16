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

import org.jbpm.casemgmt.api.event.CaseEventListener;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.prometheus.PrometheusCaseEventListener;
import org.kie.server.services.prometheus.PrometheusProcessEventListener;
import org.kie.server.services.prometheus.PrometheusTaskEventListener;
import org.kie.server.springboot.autoconfiguration.KieServerProperties;
import org.kie.server.springboot.autoconfiguration.drools.extensions.SpringBootPrometheusKieServerExtension;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({KieServerImpl.class})
@AutoConfigureAfter({DroolsKieServerAutoConfiguration.class})
@EnableConfigurationProperties(KieServerProperties.class)
public class PrometheusKieServerAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "prometheusServerExtension")
    @ConditionalOnProperty(name = "kieserver.prometheus.enabled")
    public KieServerExtension prometheusServerExtension() {
        return new SpringBootPrometheusKieServerExtension();
    }

    @Bean
    @ConditionalOnProperty(name = "kieserver.prometheus.enabled")
    public ProcessEventListener processEventListener() {
        return new PrometheusProcessEventListener();
    }

    @Bean
    @ConditionalOnProperty(name = "kieserver.prometheus.enabled")
    public CaseEventListener caseEventListener() {
        return new PrometheusCaseEventListener();
    }

    @Bean
    @ConditionalOnProperty(name = "kieserver.prometheus.enabled")
    public TaskLifeCycleEventListener taskEventListener() {
        return new PrometheusTaskEventListener();
    }
}
