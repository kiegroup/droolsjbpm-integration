KIE Server Spring Boot Starter
========================================
Spring Boot starter that configures completely KIE Server with all capabilities. 

Persistence is configured out of the box and relies on data source setup via application.properties. See section how to configure.

IdentityProvider used by jBPM services api to collect information about user who performs the operation is based on Spring Security
so keep in mind that proper configuration of Spring Security will be required for it to work as expected.

Since this includes all capabilities it has full access to underlying services. When it comes to jBPM it does rely on jbpm services API and therefore makes all services available for injection/autowire:

- ProcessService
- DeploymentService
- DefinitionService
- RuntimeDataService
- UserTaskService
- ProcessInstanceAdminService
- UserTaskAdminService
- CaseService
- CaseRuntimeDataService

In case you don't need to have all capabilities consider using other starters that provide tailored setup to cover following areas:

- Rules and Decisions KIE Server (Drools, DMN) - kie-server-spring-boot-starter-drools
- Rules and Decisions, Process and Cases KIE Server (Drools, DMN, jBPM, Case mgmt) - kie-server-spring-boot-starter-jbpm
- Planning KIE Server (Optaplanner) - kie-server-spring-boot-starter-optaplanner

How to configure it
------------------------------

Complete configuration is done via application.properties file (or its yaml equivalent).

Dedicated jBPM properties are prefixed with jbpm and provide access to setting up jBPM executor:

```
jbpm.executor.enabled=false
#jbpm.executor.retries=5
#jbpm.executor.interval=3
#jbpm.executor.threadPoolSize=1
#jbpm.executor.timeUnit=SECONDS
```

Mandatory configuration that must be placed in application.properties is related to data base and transaction manager setup:

```
#data source configuration
spring.datasource.username=sa
spring.datasource.password=sa
spring.datasource.url=jdbc:h2:./target/spring-boot-jbpm;MVCC=true
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.type=org.apache.tomcat.jdbc.pool.XADataSource

#hibernate configuration
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.jpa.properties.hibernate.show_sql=false
spring.jpa.properties.hibernate.hbm2ddl.auto=update
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

#transaction manager configuration
spring.jta.narayana.transaction-manager-id=1

```

jBPM relies on Narayana as transaction manager as it was tested and proved to be most reliable transaction manager.


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
kieserver.jbpm.enabled=true
kieserver.jbpmui.enabled=true
kieserver.casemgmt.enabled=true
kieserver.optaplanner.enabled=true
kieserver.prometheus.enabled=true
kieserver.scenariosimulation.enabled=true
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


How to extend it
------------------------------

For basic use there is nothing that would require extension for jBPM. Although in many cases there will be a need to tune a bit the way jBPM works in different environments. Most common are:

- User group callback (for task assignment)
- User info (for notifications)  
- Identity provider (if you don't want to use spring security)

For that the only thing you need to provide is a bean that will deliver custom implementation, for instance

```
@Bean
public UserGroupCallback userGroupCallback() throws IOException {
    return new MyCustomUserGroupCallback();
}
```

this will then override the one provided out of the box by auto configuration and instead use custom one. In general, this approach can be used for all beans that jBPM auto configuration provides.


How to use it
------------------------------

Best and easiest way is to use Spring Initializr (https://start.spring.io) and generate project with following starters

- KIE Server
- security
- h2

once the project is generated, edit its pom.xml and add following property to make sure proper version of narayana is used

```
<narayana.version>5.6.4.Final</narayana.version>
```

so properties section of your pom.xml should look like this

```
<properties>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
  <java.version>1.8</java.version>
  <narayana.version>5.6.4.Final</narayana.version>
</properties>
```	

Update application.properties to configure data base and you can directly start the application with:

```
mvn clean spring-boot:run
```


