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

It is a [Thorntail 2.x](http://docs.wildfly-swarm.io/2.4.0.Final/) application

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
  - host: http://kieserver1.example.com:8080/kie-server/services/rest/server
    username: joe
    password: secret
  - host: http://kieserver2.example.com:8080/kie-server/services/rest/server
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

_Refer to the [Thorntail Datasource](https://docs.thorntail.io/2.4.0.Final/#creating-a-datasource_thorntail) configuration for further details_

# Usage

## Define the plan (without node mappings)

Request:

```
URL: http://localhost:8180/plans
Method: POST
HTTP Headers:
  Content-Type: application/json
  Authorization: Basic a2VybWl0OnRoZWZyb2c=
Body:
{
    "name": "Test plan",
    "description": "Evaluation Process Test Plan",
    "sourceContainerId": "evaluation_1.0",
    "targetProcessId": "evaluation",
    "targetContainerId": "evaluation_1.1"
}
```

Response:

```
Status: 200 OK
HTTP Headers:
  Content-Type: application/json
Body:
{
    "id": 1,
    "name": "Test plan",
    "description": "Evaluation Process Test Plan",
    "sourceContainerId": "evaluation_1.0",
    "targetProcessId": "evaluation",
    "targetContainerId": "evaluation_1.1"
}
```

## Deploy some processes to test the migration

1. Start a KIE Server
1. Deploy two versions of the evaluation project (evaluation_1.0 and evaluation_1.1)
1. Start one instance of the evaluation process (evaluation_1.0)

## Create a sync migration

```
URL: http://localhost:8180/migrations
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

```
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

## Check the migration output

The following request will fetch the overall result of the migration

Request:

```
URL: http://localhost:8180/migrations/1
Method: GET
HTTP Headers:
  Content-Type: application/json
  Authorization: Basic a2VybWl0OnRoZWZyb2c=
```

Response:

```
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
```
URL: http://localhost:8180/migrations/1/results
Method: GET
HTTP Headers:
  Content-Type: application/json
  Authorization: Basic a2VybWl0OnRoZWZyb2c=
```

Response:
```
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

## Create an Async migration

1. Start two more processes
1. Trigger the migration of all the existing active processes

Request:

```
URL: http://localhost:8180/migrations
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

```
Status: 202 Accepted
HTTP Headers:
  Content-Type: application/json
Body:
{
    "id": 2,
    "definition": {
        "execution": {
            "type": "ASYNC",
            "scheduled_start_time": "2018-12-11T12:35:00.000Z"
        },
        "plan_id": 1,
        "process_instance_ids": [],
        "kieserver_id": "sample-server",
    },
    "status": "SCHEDULED",
    "created_at": "2018-11-07T11:28:43.828Z",
    "started_at": null,
    "finished_at": null
}
```

The migration status can be checked using the migrations api with the id returned as done before

# User Interface

The Process Instance Migration User Interface can be accessed in the following URL

http://localhost:8080/