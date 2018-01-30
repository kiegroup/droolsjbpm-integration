KIE Server OptaPlanner Spring Boot Starter
========================================
Spring Boot starter that configures completely KIE Server with BRM and DMN and Planner capabilities. 


How to configure it
------------------------------

Complete configuration is done via application.properties file (or its yaml equivalent).


KIE Server dedicated configuration is prefixed with kieserver and allows to configure

- context path for REST endpoints, location, server name and id and list of controllers (optionally)

```
kieserver.serverId=SpringBoot
kieserver.serverName=KIE Server SpringBoot
kieserver.restContextPath=/rest
kieserver.location=http://localhost:8080/rest/server
#kieserver.controllers=
```

All KIE server extensions are disabled by default so you need to enabled them explicitly to make them available as REST endpoints

```
kieserver.drools.enabled=true
kieserver.dmn.enabled=true
kieserver.optaplanner.enabled=true
```

Additional configuration properties that might be relevant (depending on application needs) can be found at https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#common-application-properties

Starter comes with out of the box Spring Security setup that allows configures two users

- kieserver/kieserver1! - that is standard user that will be used to connect to controller
- john/john@pwd1 - application user to quickly try it out 

That's enough to quickly try it out but for more advanced use cases one would have to provide their own setup. It can be given by providing a Configuration class named kieServerSecurity as shown below.

```
@Configuration("kieServerSecurity")
@EnableWebSecurity
public class DefaultWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
		http
        .csrf().disable()
        .authorizeRequests()
            .anyRequest().authenticated()
            .and()
        .httpBasic();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        // configure it the way you need it        
    }
}
```

this will then override default setup and allow to plug in to your user/group repository for authentication and authorization.


How to use it
------------------------------

Best and easiest way is to use Spring Initializr (https://start.spring.io) and generate project with following starters

- Planning Server
- security


Update application.properties to configure data base and you can directly start the application with:

```
mvn clean spring-boot:run
```


