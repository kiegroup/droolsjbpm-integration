package org.jboss.springboot;

import java.util.Arrays;
import java.util.List;

import org.kie.internal.identity.IdentityProvider;
import org.springframework.context.annotation.Bean;

public class TestAutoConfiguration {

    @Bean
    public IdentityProvider identityProvider() {
        
        return new IdentityProvider() {
            
            private List<String> roles = Arrays.asList("PM", "HR");
            
            @Override
            public boolean hasRole(String arg0) {
                return roles.contains(arg0);
            }
            
            @Override
            public List<String> getRoles() {
                
                return roles;
            }
            
            @Override
            public String getName() {
                return "john";
            }
        };
    }
}
