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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({ KieServerImpl.class })
@EnableConfigurationProperties(KieServerProperties.class)
public class KieServerAutoConfiguration extends ResourceConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(KieServerAutoConfiguration.class);
    
    private KieServerProperties properties;   
    private IdentityProvider identityProvider;
    
    public KieServerAutoConfiguration(KieServerProperties properties, Optional<IdentityProvider> identityProvider) {
        this.properties = properties;
        if (identityProvider.isPresent()) {
            this.identityProvider = identityProvider.get();
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
        
        registerInstances(kieServerResource);
        
        // next add any resources from server extensions
        List<KieServerExtension> activeExtensions = kieServer.getServerExtensions();

        for (KieServerExtension extension : activeExtensions) {
            LinkedHashSet<Object> resources = new LinkedHashSet<>(extension.getAppComponents(SupportedTransports.REST));
            registerInstances(resources);
        }
        logger.info("KieServer (id {}) started successfully", KieServerEnvironment.getServerId());
        return kieServer;
    }
    
    @Bean
    public ServletRegistrationBean jerseyServlet() {
        ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(), properties.getRestContextPath() + "/*");
        registration.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, KieServerAutoConfiguration.class.getName());
        return registration;
    }
}
