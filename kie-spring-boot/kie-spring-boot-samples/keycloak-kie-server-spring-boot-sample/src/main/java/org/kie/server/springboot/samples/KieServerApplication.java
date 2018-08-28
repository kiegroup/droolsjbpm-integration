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

package org.kie.server.springboot.samples;

import org.jbpm.springboot.security.SpringSecurityUserGroupCallback;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;
import org.kie.server.services.api.KieServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class KieServerApplication {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(KieServerApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(KieServerApplication.class, args);
    }
      
    @Bean
    CommandLineRunner deployAndValidate() {
        return new CommandLineRunner() {
            
            @Autowired            
            private KieServer kieServer;
            
            @Override
            public void run(String... strings) throws Exception {                
                LOGGER.info("KieServer {} started", kieServer);
            }
        };
    }

    // override user group callback to take advantage of keycloak managed roles
    @Bean
    public UserGroupCallback userGroupCallback(IdentityProvider identityProvider) {        
        return new SpringSecurityUserGroupCallback(identityProvider);
    }
}
