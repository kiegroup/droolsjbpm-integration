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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

@Configuration("kieServerSecurity")
@EnableWebSecurity
public class IntegrationTestsWebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String USER_PASSWORD = "usetheforce123@";
    private static final String KIE_SERVER_ROLE = "kie-server";
    private static final String GUEST_ROLE = "guest";
    private static final String ADMIN_ROLE = "Administrators";
    private static final String ENGINEERING_ROLE = "engineering";
    private static final String HR_ROLE = "HR";
    private static final String IT_ROLE = "IT";
    private static final String ACCOUNTING_ROLE = "HR";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .csrf().disable()
        .authorizeRequests().antMatchers("/**/server/readycheck").permitAll() // Allow health check without authentication
        .regexMatchers(".*swagger.json", ".*swagger-ui.js", ".*/css/.*css", ".*/lib/.*js", ".*/images/.*png").permitAll() //Allow also Swagger elements
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

    @Bean
    @Override
    public UserDetailsManager userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        manager.createUser(User.withUsername("yoda").password(encoder.encode(USER_PASSWORD)).roles(KIE_SERVER_ROLE, GUEST_ROLE).build());
        manager.createUser(User.withUsername("administrator").password(encoder.encode(USER_PASSWORD)).roles(KIE_SERVER_ROLE, GUEST_ROLE, ADMIN_ROLE).build());
        manager.createUser(User.withUsername("john").password(encoder.encode(USER_PASSWORD)).roles(KIE_SERVER_ROLE, GUEST_ROLE, ENGINEERING_ROLE, HR_ROLE, IT_ROLE, ACCOUNTING_ROLE).build());
        manager.createUser(User.withUsername("mary").password(encoder.encode(USER_PASSWORD)).roles(KIE_SERVER_ROLE, GUEST_ROLE, ENGINEERING_ROLE, HR_ROLE, IT_ROLE, ACCOUNTING_ROLE).build());
        return manager;
    }
}