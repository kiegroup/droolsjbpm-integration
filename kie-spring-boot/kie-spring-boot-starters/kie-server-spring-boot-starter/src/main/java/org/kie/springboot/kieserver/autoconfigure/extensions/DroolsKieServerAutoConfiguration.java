package org.kie.springboot.kieserver.autoconfigure.extensions;

import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.drools.DroolsKieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.springboot.kieserver.autoconfigure.KieServerProperties;
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
    public KieServerExtension droolsServerExtension() {

        return new DroolsKieServerExtension();
    }
}
