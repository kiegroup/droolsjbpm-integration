package org.kie.springboot.kieserver.autoconfigure;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.servlet.ServletProperties;
import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.remote.rest.common.resource.KieServerRestImpl;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerContainerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.security.JACCIdentityProvider;
import org.kie.springboot.kieserver.SpringBootKieServerImpl;
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
        } else {
            this.identityProvider = new JACCIdentityProvider();
        }
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "kieServerExtension")
    public KieServerExtension kieServerExtension() {
        
        return new KieServerContainerExtension();
    }   
    
    @Bean
    @ConditionalOnMissingBean(name = "kieServer")
    public KieServer kieServer(List<KieServerExtension> extensions) {
        
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
        ServletRegistrationBean registration = new ServletRegistrationBean(new ServletContainer(), properties.getRestContextPath());
        registration.addInitParameter(ServletProperties.JAXRS_APPLICATION_CLASS, KieServerAutoConfiguration.class.getName());
        return registration;
    }
}
