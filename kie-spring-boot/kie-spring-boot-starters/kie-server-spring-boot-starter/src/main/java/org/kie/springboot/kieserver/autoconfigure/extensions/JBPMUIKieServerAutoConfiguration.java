package org.kie.springboot.kieserver.autoconfigure.extensions;

import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.ui.JBPMUIKieServerExtension;
import org.kie.springboot.kieserver.autoconfigure.KieServerProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({KieServerImpl.class})
@AutoConfigureAfter({JBPMKieServerAutoConfiguration.class})
@EnableConfigurationProperties(KieServerProperties.class)
public class JBPMUIKieServerAutoConfiguration {

    private KieServerProperties properties;

  
    public JBPMUIKieServerAutoConfiguration(KieServerProperties properties) {
        this.properties = properties;   
    }

    @Bean
    @ConditionalOnMissingBean(name = "jBPMUIServerExtension")
    @ConditionalOnProperty(name = "kieserver.jbpmui.enabled")
    public KieServerExtension jbpmUIServerExtension() {

        return new JBPMUIKieServerExtension();

    }
}
