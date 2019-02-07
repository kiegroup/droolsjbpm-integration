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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration("kieServerSecurity")
@EnableWebSecurity
public class IntegrationTestsWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .csrf().disable()
        .authorizeRequests().antMatchers("/**/server/readycheck").permitAll() // Allow health check without authentication
        .anyRequest().authenticated()
        .and()
        .httpBasic();
    }

    /**
     * Provide altered implementation of StrictHttpFirewall to be able to run som tests from ProcessDefinitionIntegrationTest
     * class which test that there can be a task name with a question mark (?).
     * The other possibility to set this implementation is by overriding the {@link #configure(WebSecurity)} method.
     */
    @Bean
    public HttpFirewall customStrictHttpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();
        firewall.setAllowUrlEncodedPercent(true);

        return firewall;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        // Configuration is the same as in the kie-server-tests module
        auth.inMemoryAuthentication().withUser("yoda").password(encoder.encode("usetheforce123@")).roles("kie-server", "guest")
        .and()        
        .withUser("Administrator").password(encoder.encode("usetheforce123@")).roles("kie-server", "guest", "Administrators")
        .and()        
        .withUser("john").password(encoder.encode("usetheforce123@")).roles("kie-server", "guest", "engineering", "HR", "IT", "Accounting")
        .and()        
        .withUser("mary").password(encoder.encode("usetheforce123@")).roles("kie-server", "guest", "engineering", "HR", "IT", "Accounting");
    }
    
}