package org.kie.server.springboot.samples;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

@Configuration("kieServerSecurity")
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String KIE_SERVER_PASSWORD = "kieserver1!";
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
        .authorizeRequests()
            .antMatchers("/rest/server*").authenticated()
            .and()
        .httpBasic();
    }

    @Bean
    @Override
    public UserDetailsManager userDetailsService() {
        InMemoryUserDetailsManager manager = new InMemoryUserDetailsManager();
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        manager.createUser(User.withUsername("kieserver").password(encoder.encode(KIE_SERVER_PASSWORD)).roles(KIE_SERVER_ROLE).build());
        manager.createUser(User.withUsername("yoda").password(encoder.encode(USER_PASSWORD)).roles(KIE_SERVER_ROLE, GUEST_ROLE).build());
        manager.createUser(User.withUsername("administrator").password(encoder.encode(USER_PASSWORD)).roles(KIE_SERVER_ROLE, GUEST_ROLE, ADMIN_ROLE).build());
        manager.createUser(User.withUsername("john").password(encoder.encode(USER_PASSWORD)).roles(KIE_SERVER_ROLE, GUEST_ROLE, ENGINEERING_ROLE, HR_ROLE, IT_ROLE, ACCOUNTING_ROLE).build());
        manager.createUser(User.withUsername("mary").password(encoder.encode(USER_PASSWORD)).roles(KIE_SERVER_ROLE, GUEST_ROLE, ENGINEERING_ROLE, HR_ROLE, IT_ROLE, ACCOUNTING_ROLE).build());
        return manager;
    }
}
