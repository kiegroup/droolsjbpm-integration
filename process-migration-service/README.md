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

## Requirements

* JRE 1.8 or greater
* Running KIE Process server

## Build

```bash
mvn clean package
```

## Run application

It is a [Thorntail 2.x](http://docs.wildfly-swarm.io/2.4.0.Final/) application.

```bash
java -jar target/process-migration-thorntail.jar
```

You can provide your custom configuration file. Check [project-defaults.yml](./src/main/resources/project-defaults.yml) to see an example. The provided configuration will be added or override the one existing in project-defaults.yml

```bash
java -jar target/process-migration-thorntail.jar -s./myconfig.yml
```

Start the server on a different ports set:

```bash
java -jar target/process-migration-thorntail.jar -Dswarm.network.socket-binding-groups.standard-sockets.port-offset=10
```

## Configuration

Default configuration is as follows:

```yaml
thorntail:
  deployment:
    process-migration.war:
      jaxrs:                   (1)
        application-path: /rest
  datasources:                 (2)
    data-sources:
      pimDS:
        driver-name: h2
        connection-url: jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
        user-name: sa
        password: sa
  ejb3:                        (3)
    timer-service:
      default-data-store: timers-store
      database-data-stores:
        timers-store:
          datasource-jndi-name: java:jboss/datasources/pimDS
          partition: timer
```

1. Deploy the application on `/rest`
1. H2 inmemory datasource. Override it by one of your choice
1. EJB Timers persistence configuration

### Configuration overrides

It is possible to override or extend the provided configuration. You can provide one or more additional configuration files that will allow you to customize the application. Several examples are provided in the [examples](./examples/) folder.

As an example, if you want to replace the H2 default persistence configuration by [MariaDB](./examples/persistence/mariadb.yml) and the authentication mechanism to use [LDAP](./examples/authentication/ldap/ldapExtended.yml) you could use the following command to start the application:

```bash
java -Dthorntail.classpath=./mariadb-java-client-2.4.2.jar -jar target/process-migration-thorntail.jar -s./examples/authentication/ldap/ldapExtended.yml -s./examples/persistence/mariadb.yml
```

**Note:** As the MariaDB jdbc driver is not included in the classpath it must be added.

**Note:** These files will override or extend the already defined properties in the project-defaults.yml file

#### Defining KIE Servers

The right way to configure the connection to one or more KIE Servers in order to perform the migrations, a list of kieservers should exist in the configuration file.

```yaml
kieservers:
  - host: http://kieserver1.example.com:8080/kie-server/services/rest/server
    username: joe
    password: secret
  - host: http://kieserver2.example.com:8080/kie-server/services/rest/server
    username: jim
    password: secret
```

#### MySQL Datasource

See [Using non-provided JDBC drivers](#using-non-provided-jdbc-drivers) for details on how to include additional JDBC drivers to the runtime.

```yaml
thorntail:
  datasources:
    data-sources:
      pimDS:
        driver-name: mysql
        connection-url: jdbc:mysql://mysql.example.com:3306/pimdb?useUnicode=true&useSSL=false&serverTimezone=UTC
        user-name: pim
        password: pim
```

_Refer to the [Thorntail Datasource](https://docs.thorntail.io/2.4.0.Final/#creating-a-datasource_thorntail) configuration for further details_

#### Basic authentication

Authentication example available [here](./examples/authentication/properties). Shows how to define basic authentication using properties files.

```{yaml}
thorntail:
  deployment:
    process-migration.war:
      web:
        login-config:
          auth-method: BASIC (1)
          security-domain: pim
        security-constraints:
          - url-pattern: /* (2)
            roles: [ admin ]
          - url-pattern: /health/* (3)
          - url-pattern: /rest/health/* (3)
  security:
    security-domains:
      pim:
        classic-authentication:
          login-modules:
            UsersRoles:
              code: UsersRoles
              flag: required
              module-options: (4)
                usersProperties: /opt/process-migration/config/application-users.properties
                rolesProperties: /opt/process-migration/config/application-roles.properties
```

1. Authentication type `BASIC`
1. Every resource under root path requires the `admin` role
1. Health checks are not secured
1. Properties files use absolute path as they are not part of the classpath. See the existing files in the examples folder to see how to use them.

## Using non-provided JDBC drivers

The H2 JDBC driver is included by default. However, users will want to use different JDBC drivers to connect to external databases. For that purpose you will
have to provide a `-Dthorntail.classpath` parameter with the path to the JDBC driver.

```bash
$ java -Dthorntail.classpath=./mariadb-java-client-2.4.2.jar -jar target/process-migration-thorntail.jar -s./mariadb-config.yml
...
19-07-19 11:00:00,566 INFO  [org.wildfly.swarm.datasources] (main) THORN1003: Auto-detected JDBC driver for h2
2019-07-19 11:00:00,572 INFO  [org.wildfly.swarm.datasources] (main) THORN1003: Auto-detected JDBC driver for mariadb
...
```

## Usage

### Define the plan (without node mappings)

Request:

```bash
URL: http://localhost:8180/rest/plans
Method: POST
HTTP Headers:
  Content-Type: application/json
  Authorization: Basic a2VybWl0OnRoZWZyb2c=
Body:
{
    "name": "Test plan",
    "description": "Evaluation Process Test Plan",
    "source": {
      "containerId": "evaluation_1.0",
      "processId": "evaluation"
    },
    "target": {
      "containerId": "evaluation_1.1",
      "processId": "evaluation"
    }
}
```

Response:

```http
Status: 200 OK
HTTP Headers:
  Content-Type: application/json
Body:
{
    "id": 1,
    "name": "Test plan",
    "description": "Evaluation Process Test Plan",
    "source": {
      "containerId": "evaluation_1.0",
      "processId": "evaluation"
    },
    "target": {
      "containerId": "evaluation_1.1",
      "processId": "evaluation"
    }
}
```

### Deploy some processes to test the migration

1. Start a KIE Server
1. Deploy two versions of the evaluation project (evaluation_1.0 and evaluation_1.1)
1. Start one instance of the evaluation process (evaluation_1.0)

### Create a sync migration

```http
URL: http://localhost:8180/rest/migrations
Method: POST
HTTP Headers:
  Content-Type: application/json
  Authorization: Basic a2VybWl0OnRoZWZyb2c=
Body:
{
    "planId": 1,
    "processInstanceIds": [1],
    "kieserverId": "sample-server",
    "execution": {
      "type": "SYNC"
    }
}
```

Response:

```http
Status: 200 OK
HTTP Headers:
  Content-Type: application/json
Body:
{
    "id": 1,
    "definition": {
        "planId": 1,
        "processInstanceIds": [1],
        "kieserverId": "sample-server",
        "requester": "kermit",
        "execution": {
            "type": "SYNC"
        }
    },
    "createdAt": "2018-11-29T13:47:07.839Z",
    "startedAt": "2018-11-29T13:47:07.839Z",
    "finishedAt": "2018-11-29T13:47:07.874Z",
    "status": "COMPLETED"
}
```

As it is a Synchronous migration, the result of the migration will be returned once it has finished.

### Check the migration output

The following request will fetch the overall result of the migration

Request:

```http
URL: http://localhost:8180/rest/migrations/1
Method: GET
HTTP Headers:
  Content-Type: application/json
  Authorization: Basic a2VybWl0OnRoZWZyb2c=
```

Response:

```http
Status: 200 OK
HTTP Headers:
  Content-Type: application/json
Body:
{
    "id": 1,
    "definition": {
        "planId": 1,
        "processInstanceIds": [],
        "kieserverId": "sample-server",
        "requester": "kermit",
        "execution": {
            "type": "SYNC"
        }
    },
    "createdAt": "2018-11-27T14:28:58.918Z",
    "startedAt": "2018-11-27T14:28:59.861Z",
    "finishedAt": "2018-11-27T14:29:00.167Z",
    "status": "COMPLETED"
}
```

To retrieve the individual results of the migration of each process instance

Request:

```http
URL: http://localhost:8180/rest/migrations/1/results
Method: GET
HTTP Headers:
  Content-Type: application/json
  Authorization: Basic a2VybWl0OnRoZWZyb2c=
```

Response:

```http
Status: 200 OK
HTTP Headers:
  Content-Type: application/json
Body:
[
    {
        "id": 1,
        "migrationId": 3,
        "processInstanceId": 5,
        "startDate": "2018-12-18T11:16:26.779Z",
        "endDate": "2018-12-18T11:16:26.906Z",
        "successful": true,
        "logs": [
            "INFO Tue Dec 18 12:16:26 CET 2018 Variable instances updated = 1 for process instance id 5",
            "INFO Tue Dec 18 12:16:26 CET 2018 Node instances updated = 3 for process instance id 5",
            "INFO Tue Dec 18 12:16:26 CET 2018 Process instances updated = 1 for process instance id 5",
            "INFO Tue Dec 18 12:16:26 CET 2018 Task variables updated = 1 for process instance id 5",
            "INFO Tue Dec 18 12:16:26 CET 2018 Task audit updated = 1 for process instance id 5",
            "INFO Tue Dec 18 12:16:26 CET 2018 Tasks updated = 1 for process instance id 5",
            "INFO Tue Dec 18 12:16:26 CET 2018 Context info updated = 0 for process instance id 5",
            "INFO Tue Dec 18 12:16:26 CET 2018 Executor Jobs updated = 0 for process instance id 5",
            "WARN Tue Dec 18 12:16:26 CET 2018 Source and target process id is exactly the same (test.myprocess) it's recommended to use unique process ids",
            "INFO Tue Dec 18 12:16:26 CET 2018 Mapping: Node instance logs to be updated  = [1]",
            "INFO Tue Dec 18 12:16:26 CET 2018 Mapping: Node instance logs updated = 1 for node instance id 1",
            "INFO Tue Dec 18 12:16:26 CET 2018 Mapping: Task audit updated = 1 for task id 5",
            "INFO Tue Dec 18 12:16:26 CET 2018 Mapping: Task updated = 1 for task id 5",
            "INFO Tue Dec 18 12:16:26 CET 2018 Migration of process instance (5) completed successfully to process test.myprocess"
        ]
    },
    {
        "id": 2,
        "migrationId": 3,
        "processInstanceId": 6,
        "startDate": "2018-12-18T11:16:26.992Z",
        "endDate": "2018-12-18T11:16:27.039Z",
        "successful": true,
        "logs": [
            "INFO Tue Dec 18 12:16:27 CET 2018 Variable instances updated = 1 for process instance id 6",
            "INFO Tue Dec 18 12:16:27 CET 2018 Node instances updated = 3 for process instance id 6",
            "INFO Tue Dec 18 12:16:27 CET 2018 Process instances updated = 1 for process instance id 6",
            "INFO Tue Dec 18 12:16:27 CET 2018 Task variables updated = 1 for process instance id 6",
            "INFO Tue Dec 18 12:16:27 CET 2018 Task audit updated = 1 for process instance id 6",
            "INFO Tue Dec 18 12:16:27 CET 2018 Tasks updated = 1 for process instance id 6",
            "INFO Tue Dec 18 12:16:27 CET 2018 Context info updated = 0 for process instance id 6",
            "INFO Tue Dec 18 12:16:27 CET 2018 Executor Jobs updated = 0 for process instance id 6",
            "WARN Tue Dec 18 12:16:27 CET 2018 Source and target process id is exactly the same (test.myprocess) it's recommended to use unique process ids",
            "INFO Tue Dec 18 12:16:27 CET 2018 Mapping: Node instance logs to be updated  = [1]",
            "INFO Tue Dec 18 12:16:27 CET 2018 Mapping: Node instance logs updated = 1 for node instance id 1",
            "INFO Tue Dec 18 12:16:27 CET 2018 Mapping: Task audit updated = 1 for task id 6",
            "INFO Tue Dec 18 12:16:27 CET 2018 Mapping: Task updated = 1 for task id 6",
            "INFO Tue Dec 18 12:16:27 CET 2018 Migration of process instance (6) completed successfully to process test.myprocess"
        ]
    }
]
```

### Create an Async migration

1. Start two more processes
1. Trigger the migration of all the existing active processes

Request:

```http
URL: http://localhost:8180/rest/migrations
Method: POST
HTTP Headers:
  Content-Type: application/json
  Authorization: Basic a2VybWl0OnRoZWZyb2c=
Body:
{
    "planId": 1,
    "processInstanceIds": [],
    "kieserverId": "sample-server",
    "execution": {
      "type": "ASYNC",
      "scheduledStartTime": "2018-12-11T12:35:00.000Z"
    }
}
```

Response:

```http
Status: 202 Accepted
HTTP Headers:
  Content-Type: application/json
Body:
{
    "id": 2,
    "definition": {
        "execution": {
            "type": "ASYNC",
            "scheduledStartTime": "2018-12-11T12:35:00.000Z"
        },
        "planId": 1,
        "processInstanceIds": [],
        "kieServerId": "sample-server",
    },
    "status": "SCHEDULED",
    "createdAt": "2018-11-07T11:28:43.828Z",
    "startedAt": null,
    "finishedAt": null
}
```

The migration status can be checked using the migrations api with the id returned as done before

## User Interface

The Process Instance Migration User Interface can be accessed in the following URL

[http://localhost:8080/](http://localhost:8080/)
