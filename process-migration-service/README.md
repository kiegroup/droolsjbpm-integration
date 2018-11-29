# Process Instance Migration Service

This service is intended to be deployed as a standalone application. It must be integrated with one or more KIE Process Server instances to be able to execute migrations remotely.

The two entities that will be used are _Migration Plans_ and _Migrations_. 

## Migration Plan

Is a definition of how the migration will be performed. Includes the following information:

* Plan ID (Generated)
* Plan name
* Plan description
* Source and target container IDs
* Node mappings (if any)

## Migration

Is the execution of a defined plan, applied to a set of process instances. These are the attributes that define a Migration:

* Migration ID (Generated)
* Plan ID
* Instances to migrate.
* Execution type
    * Sync or Async
    * Scheduled start time
    * Callback URL
    
# Requirements

* JRE 1.8 or greater
* Running KIE Process server

# Build

```
$ mvn clean package
```

# Run application

It is a [Thorntail 2.x](http://docs.wildfly-swarm.io/2.2.1.Final/) application

```
$ java -jar target/process-migration-thorntail.jar
```

You can provide your custom configuration file. Check [project-defaults.yml](./src/main/resources/project-defaults.yml) to see an example

```
$ java -jar target/process-migration-thorntail.jar -s./myconfig.yml
```

Start the server on a different ports:

```
$ java -jar target/process-migration-thorntail.jar -Dswarm.network.socket-binding-groups.standard-sockets.port-offset=10
```

# Configuration

Default configuration is as follows:

```
thorntail:
  deployment:
    process-migration.war:
      jaxrs:                   (1)
        application-path: /
      web:                     (2)
        login-config:
          auth-method: BASIC
          security-domain: pim
        security-constraints:
          - url-pattern: /*
            roles: [ admin ]
          - url-pattern: /health/*
  datasources:                 (3)
    data-sources:
      pimDS:
        driver-name: h2
        connection-url: jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
        user-name: sa
        password: sa
  ejb3:                        (4)
    timer-service:
      default-data-store: timers-store
      database-data-stores:
        timers-store:
          datasource-jndi-name: java:jboss/datasources/pimDS
          partition: timer
  security:
    security-domains:
      pim:                     (5)
        classic-authentication:
          login-modules:
            UsersRoles:
              code: UsersRoles
              flag: required
              module-options:
                usersProperties: application-users.properties
                rolesProperties: application-roles.properties
```

1. Deploy the application in the root path
1. Configure Basic authentication for the application. Only the admin role is allowed to access the API. The `health` endpoint is available to anyone
1. H2 inmemory datasource. Override it by one of your choice
1. EJB Timers persistence configuration
1. Properties based authentication. Default admin user/password is `kermit`/`thefrog`

## Configuration overrides

It is possible to override or extend the provided configuration.

### Defining KIE Servers

The right way to configure the connection to one or more KIE Servers in order to perform the migrations, a list of kieservers should exist in the configuration file. 

```
kieservers:
  - host: kieserver1.example.com
    port: 8080
    protocol: http
    contextRoot: /kie-server
    path: /services/rest/server
    username: joe
    password: secret
  - host: kieserver2.example.com
    port: 8080
    protocol: http
    contextRoot: /kie-server
    path: /services/rest/server
    username: jim
    password: secret
```

### MySQL Datasource

```
thorntail:
  datasources:
    data-sources:
      pimDS:
        driver-name: mysql
        connection-url: jdbc:mysql://mysql.example.com:3306/pimdb?useUnicode=true&useSSL=false&serverTimezone=UTC
        user-name: pim
        password: pim
```

_Refer to the [Thorntail Datasource](https://docs.thorntail.io/2.2.1.Final/#creating-a-datasource_thorntail) configuration for further details_