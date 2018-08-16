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

package org.kie.server.springboot.autoconfiguration;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.Feature;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.jaxrs.spring.AbstractJaxrsClassesScanServer;
import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.remote.rest.common.resource.KieServerRestImpl;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerContainerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.springboot.SpringBootKieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ KieServerImpl.class })
@EnableConfigurationProperties(KieServerProperties.class)
public class KieServerAutoConfiguration extends AbstractJaxrsClassesScanServer {
    
    private static final Logger logger = LoggerFactory.getLogger(KieServerAutoConfiguration.class);
    
    private KieServerProperties properties;   
    private IdentityProvider identityProvider;
    private List<Object> endpoints;
    
    @Value("${cxf.jaxrs.classes-scan:false}")
    private boolean jaxrsComponentScanEnabled;

    public KieServerAutoConfiguration(KieServerProperties properties, Optional<IdentityProvider> identityProvider) {
        this.properties = properties;
        if (identityProvider.isPresent()) {
            this.identityProvider = identityProvider.get();
        }
        if (!jaxrsComponentScanEnabled) {
            System.setProperty("cxf.jaxrs.classes-scan-packages", "");
        }
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "kieServerExtension")
    public KieServerExtension kieServerExtension() {
        
        return new KieServerContainerExtension();
    }   
    
    @Bean(destroyMethod="destroy")
    @ConditionalOnMissingBean(name = "kieServer")
    public KieServer kieServer(List<KieServerExtension> extensions) {
        System.setProperty(KieServerConstants.KIE_SERVER_CONTROLLER, properties.getControllers());
        System.setProperty(KieServerConstants.KIE_SERVER_LOCATION, properties.getLocation());
        if (KieServerEnvironment.getServerId() == null) {
            String serverName = properties.getServerName();
            String serverId = properties.getServerId();

            KieServerEnvironment.setServerId(serverId.toString());
            KieServerEnvironment.setServerName(serverName);
        }
        logger.info("KieServer (id {} (name {})) started initialization process", KieServerEnvironment.getServerId(), KieServerEnvironment.getServerName());
        SpringBootKieServerImpl kieServer = new SpringBootKieServerImpl(extensions, identityProvider);        
        kieServer.init();
        
        KieServerRestImpl kieServerResource = new KieServerRestImpl(kieServer);
        this.endpoints = new ArrayList<>();
        endpoints.add(kieServerResource);
        
        // next add any resources from server extensions
        List<KieServerExtension> activeExtensions = kieServer.getServerExtensions();

        for (KieServerExtension extension : activeExtensions) {
            LinkedHashSet<Object> resources = new LinkedHashSet<>(extension.getAppComponents(SupportedTransports.REST));
            endpoints.addAll(resources);
        }        
        logger.info("KieServer (id {}) started successfully", KieServerEnvironment.getServerId());
        return kieServer;
    }

    @Override
    protected void setJaxrsResources(JAXRSServerFactoryBean factory) {
        factory.setServiceBeans(endpoints);
        if (jaxrsComponentScanEnabled) {
            super.setJaxrsResources(factory);        
        }
    }
    
    @Bean
    public Server jaxRsServer() {
        return super.createJaxRsServer();
    }

    @Override
    public List<Feature> getFeatures() {
        List<Feature> features = new ArrayList<>(super.getFeatures());
        if (properties.isSwagger()) {
            try {
                features.add((Feature) Class.forName("org.apache.cxf.jaxrs.swagger.Swagger2Feature").newInstance());
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                logger.error("Swagger feature was enabled but cannot be created", e);
            }
        }
        return features;
    }    
    
}
