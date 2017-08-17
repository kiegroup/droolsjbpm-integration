# Kie Controller Maven Plugin

Maven plugin to deploy k-jar container on remote managed kie-server cluster.

Default goalPrefix: __kie-ctrl__.
Goals are:

1. get-template
2. create-template
3. delete-template
5. get-containers
6. get-container
7. create-container
8. delete-container
9. start-container
10. stop-container
11. deploy-container
12. dispose-container

## Plugin configuration

On pom.xml of local project add plugin: 

```xml
<build>
  <plugins>
    ...
    <plugin>
        <groupId>org.kie.server</groupId>
        <artifactId>kie-server-controller-plugin</artifactId>
        <version>8.0.0-SNAPSHOT</version>
        <configuration>
          <controllerUsername>${controller.username}</controllerUsername>
          <controllerPassword>${controller.password}</controllerPassword>
          ...
        </configuration>
    </plugin>
        
  </plugins>
</build>
```
### Basic connection properties

All goals share the same basic connection properties:

| Property           | Default value    | Mandatory | Name                         | Type    |
| ------------------ |:----------------:|:---------:|:----------------------------:|:--------|
| controllerUsername | null             | true      | kie-ctrl.controller-username | String  |
| controllerPassword | null             | true      | kie-ctrl.controller-password | String  |
| hostname           | localhost        | false     | kie-ctrl.hostname            | String  |
| port               | 8080             | false     | kie-ctrl.port                | Integer |
| protocol           | http             | false     | kie-ctrl.protocol            | String  |
| contextPath        | business-central | false     | kie-ctrl.context-path        | String  |
| controllerPath     | /rest/controller | false     | kie-ctrl.controller-path     | String  | 
| connectionTimeout  | 100              | false     | kie-ctrl.connection-timeout  | Integer |
| socketTimeout      | 2                | false     | kie-ctrl.socket-timeout      | Integer |

The mandatory parameters are __controllerUsername__ and __controllerPassword__, which are the basic authentication credential of remote controller.
The controller user principal must exist in controller security context.

You can change the remote controller url access setting __hostname__, __port__, __protocol__, __contextPath__ and __controllerPath__.
Or change default timeout values for all remote invocations setting the __connectionTimeout__ and __socketTimeout__ properties. Timeout values are intended in __seconds__.

## Managed Deploy 

Managed deploy is performed by a process server central controller (business central or kie-wb).
Process instances must have been registered to the controller using the system properties that allow to form a cluster.
A cluster of managed process server is called a __server template__.

Process server controller can manage more server template.
A managed deploy consists in __add and start a new container to a server template__.
Process server instances member of a server template will have all the same containers with the same configurations.
 
## 1. Goal: get-template

This is an idempotent/query goal to verify server template topologies and configurations. 

By default the goal display info about all server templates.
You can specify __templateId__ not mandatory property it is not set goal displays all templates info, otherwise only the template set in the property will be displayed.
Goal does not require project.

| Property           | Default value    | Mandatory | Name                         | Type    |
| ------------------ |:----------------:|:---------:|:----------------------------:|:--------|
| templateId         | null             | false     | kie-ctrl.template-id         | String  |

### Get server templates example

Running the goal:
``` 
mvn kie-ctrl:get-template -Dkie-ctrl.controller-username=fabio -Dkie-ctrl.controller-password=fabio\$739 -Dkie-ctrl.port=8230
```

An example of output might be:
```
[INFO] --- kie-server-controller-plugin:8.0.0-SNAPSHOT:get-template (default-cli) @ bpms-signal ---
[INFO] Protocol: http
[INFO] Host Name: localhost
[INFO] Port: 8230
[INFO] Controller Username: fabio
[INFO] Marshaller extensions init
[INFO] --- Server Template --- 
[INFO] Server Template Id: process-server
[INFO] Server Template Name: process-server
[INFO] Capabilities: [RULE, PROCESS]
[INFO]   Server: http://localhost:8080/kie-server/services/rest/server
[INFO]   Server: http://localhost:8380/kie-server/services/rest/server
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-selection-process:5.0.0
[INFO]   Release: it.redhat.demo:bpms-selection-process:5.0.0
[INFO]   Status: STOPPED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-selection-process:3.0.0
[INFO]   Release: it.redhat.demo:bpms-selection-process:3.0.0
[INFO]   Status: STOPPED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Release: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Status: STOPPED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO] ----------------------- 

```
In the former example we can see that:
1. Remote controller has one server template named __process-server__
2. Template __process-server__ control two remote servers.
3. On __process-server__ template three containers are defined
4. Containers __it.redhat.demo:bpms-selection-process:5.0.0__ and __it.redhat.demo:bpms-selection-process:2.0.0__ have __STARTED__ state 
5. Container __it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT__ has __STOPPED__ state
5. On all containers the runtime strategy has forced to __PER_PROCESS_INSTANCE__

## 2. Goal: create-template

Use this goal to define a new server template. Parameter __templateId__ is mandatory.
If not specified template name will be the same value of template id.
By default template capabilities are all possible capabilities: "RULE", "PROCESS", "PLANNING".
You can also define a list of templates you want to create at the same time.
Goal does not require project.

| Property           | Default value                 | Mandatory | Name                         | Type            |
| ------------------ |:-----------------------------:|:---------:|:----------------------------:|:---------------:|
| templateId         | null                          | true      | kie-ctrl.template-id         | String          |
| templateName       | ${templateId}                 | false     | kie-ctrl.template-name       | String          |
| capabilities       | "RULE", "PROCESS", "PLANNING" | false     | kie-ctrl.capabilities        | List<String>    |
| containers         | null                          | false     | kie-ctrl.containers          | List<Container> |

Container nested properties are:

| Property           | Default value                 | Mandatory | Type            |
| ------------------ |:-----------------------------:|:---------:|:---------------:|
| id                 | ${project.GAV}                | false     | String          |
| groupId            | null                          | true      | String          |
| artifactId         | null                          | true      | String          |
| version            | null                          | true      | String          |
| runtimeStrategy    | null                          | false     | String          |
| kbase              | null                          | false     | String          |
| ksession           | null                          | false     | String          |
| mergeMode          | null                          | false     | String          |
| pollInterval       | null                          | false     | Integer         |
| scannerStatus      | null                          | false     | String          |

Property id is not mandatory, by default container id is set to maven GAV string, that enables unified execution convention.

### Create template example

With the given maven configuration:
```xml
<build>
  <plugins>
    ...
    <plugin>
        <groupId>org.kie.server</groupId>
        <artifactId>kie-server-controller-plugin</artifactId>
        <version>8.0.0-SNAPSHOT</version>
        <configuration>
            <templateId>process-server</templateId>
            <controllerUsername>${controller.username}</controllerUsername>
            <controllerPassword>${controller.password}</controllerPassword>
            <port>8230</port>
            <username>fabio</username>
            <password>fabio$739</password>
            <capabilities>
                <capability>RULE</capability>
                <capability>PROCESS</capability>
            </capabilities>
            <containers>
                <container>
                    <groupId>it.redhat.demo</groupId>
                    <artifactId>bpms-selection-process</artifactId>
                    <version>5.0.0</version>
                    <runtimeStrategy>PER_PROCESS_INSTANCE</runtimeStrategy>
                </container>
                <container>
                    <groupId>it.redhat.demo</groupId>
                    <artifactId>bpms-selection-process</artifactId>
                    <version>3.0.0</version>
                    <runtimeStrategy>PER_PROCESS_INSTANCE</runtimeStrategy>
                </container>
            </containers>
        </configuration>
    </plugin>
        
  </plugins>
</build>
```

Running the goal:
``` 
mvn kie-ctrl:create-template
```

An example of output might be:
```
[INFO] Protocol: http
[INFO] Host Name: localhost
[INFO] Port: 8230
[INFO] Controller Username: fabio
[INFO] Marshaller extensions init
[INFO] Server Template Id: process-server
[INFO] Server Template Name: process-server
[INFO] Capabilities: [RULE, PROCESS]
[INFO] Hibernate Validator 4.1.0.Final
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-selection-process:5.0.0
[INFO]   Release: it.redhat.demo:bpms-selection-process:5.0.0
[INFO]   Status: STOPPED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-selection-process:3.0.0
[INFO]   Release: it.redhat.demo:bpms-selection-process:3.0.0
[INFO]   Status: STOPPED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO] Server template process-server CREATED
```

## 3. Goal: delete-template

Use this goal to remove a server template.
__templateId__ is a mandatory parameter.
Goal does not require project.

### Delete template example

Running the goal:
``` 
mvn kie-ctrl:delete-template -Dkie-ctrl.controller-username=fabio -Dkie-ctrl.controller-password=fabio\$739 -Dkie-ctrl.port=8230 -Dkie-ctrl.template-id=process-server
```

An example of output might be:
```
[INFO] Protocol: http
[INFO] Host Name: localhost
[INFO] Port: 8230
[INFO] Controller Username: fabio
[INFO] Marshaller extensions init
[INFO] Server Template Id: process-server
[INFO] Server template process-server DELETED
```

## 4. Goal: get-containers

If we need to get only status of containers of a server template, we can use this idempotent/query goal.
__templateId__ is a mandatory parameter.
Goal does not require project.

### Get containers example

Running the goal:
``` 
mvn kie-ctrl:get-containers -Dkie-ctrl.controller-username=fabio -Dkie-ctrl.controller-password=fabio\$739 -Dkie-ctrl.port=8230 -Dkie-ctrl.template-id=process-server
```

The output is similar to get-server-templates goal, displaying only containers info.

## 5. Goal: get-container

This goal retrieves container info.
__templateId__ is a mandatory parameter.
Goal does not require project.

The container to be shown can be set using __container__ property or taken from the local maven project context using unified execution convention.
If container property is not set the goal requires maven project.

### Get container example
``` 
mvn kie-ctrl:get-container -Dkie-ctrl.controller-username=fabio -Dkie-ctrl.controller-password=fabio\$739 -Dkie-ctrl.port=8230 -Dkie-ctrl.template-id=process-server -Dkie-ctrl.container=it.redhat.demo:bpms-selection-process:5.0.0
```
Alternatively every parameter could be set in configuration plugin.

The output is similar to get-server-templates goal, displaying only container info.

## 6. Goal: create-container
 
This goal define a new container for a server template. We use the word define, and not deploy or activate, because container are created in __STOPPED__ status.
To activate the container see __start-container__ or __deploy__ goals, which will be explained later in this file.
A stopped container is present only on controller and not on process server instances.

__templateId__ is a mandatory parameter and Goal __requires project__.
Template name could be specified if not equals to template id.
Container name if not specified is taken from the project GAV name.
Other parameters could be set to force container behaviour.

Parameters are:

| Property           | Default value                 | Mandatory | Name                         | Type            |
| ------------------ |:-----------------------------:|:---------:|:----------------------------:|:---------------:|
| templateId         | null                          | true      | kie-ctrl.template-id         | String          |
| templateName       | ${templateId}                 | false     | kie-ctrl.template-name       | String          |
| container          | ${project.GAV}                | false     | kie-ctrl.container           | String          |
| runtimeStrategy    | null                          | false     | kie-ctrl.runtime-strategy    | String          |
| kbase              | null                          | false     | kie-ctrl.kbase               | String          |
| ksession           | null                          | false     | kie-ctrl.ksession            | String          |
| mergeMode          | null                          | false     | kie-ctrl.mergeMode           | String          |
| pollInterval       | null                          | false     | kie-ctrl.pollInterval        | Integer         |
| scannerStatus      | null                          | false     | kie-ctrl.scannerStatus       | String          |
 


### Create container example

With the given maven configuration:
```xml
<project>
...
<groupId>it.redhat.demo</groupId>
<artifactId>bpms-signal</artifactId>
<version>1.0.0-SNAPSHOT</version>
...
<build>
  <plugins>
    ...
    <plugin>
      <groupId>org.kie.server</groupId>
      <artifactId>kie-server-controller-plugin</artifactId>
      <version>8.0.0-SNAPSHOT</version>
      <configuration>
        <templateId>process-server</templateId>
        <controllerUsername>${controller.username}</controllerUsername>
        <controllerPassword>${controller.password}</controllerPassword>
        <port>8230</port>
    	<runtimeStrategy>PER_PROCESS_INSTANCE</runtimeStrategy>
      </configuration>
    </plugin>
        
  </plugins>
</build>
...
</project>

```
``` 
mvn kie-ctrl:create-container
```
An example of output might be:
```
[INFO] Protocol: http
[INFO] Host Name: localhost
[INFO] Port: 8230
[INFO] Controller Username: fabio
[INFO] Marshaller extensions init
[INFO] Server Template Id: process-server
[INFO] Server Template Name: process-server
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Release: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Status: STOPPED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT CREATED on server template process-server
```

## 7. Goal: delete-container

This command remove a container from a server template.

If the container is in __STARTED__ status, this command does not undeploy container started on process server instances.
If you need also to stop any running container see __dispose__ goal, which will be explained later in this file.

This goal retrieves container info.
__templateId__ is a mandatory parameter.
Goal does not require project.

The container to be shown can be set using __container__ property or taken from the local maven project context using unified execution convention.
If container property is not set the goal requires maven project.

### Delete container example

Running the goal:
``` 
mvn kie-ctrl:delete-container
```

An example of output might be:

```
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT DELETED from server template process-server
```

## 8. Goal: start-container

To start a __STOPPED__ container use this command.

This goal retrieves container info.
__templateId__ is a mandatory parameter.
Goal does not require project.

The container to be shown can be set using __container__ property or taken from the local maven project context using unified execution convention.
If container property is not set the goal requires maven project.

### Start container example

Running the goal:
``` 
mvn kie-ctrl:start-container
```

An example of output might be:

```
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT STARTED on server template process-server
```

## 9. Goal: stop-container

To stop a __STARTED__ container use this command.

This goal retrieves container info.
__templateId__ is a mandatory parameter.
Goal does not require project.

The container to be shown can be set using __container__ property or taken from the local maven project context using unified execution convention.
If container property is not set the goal requires maven project.

### Stop container example

Running the goal:
``` 
mvn kie-ctrl:stop-container
```

An example of output might be:

```
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT STOPPED on server template process-server
```

## 10. Goal: deploy-container
 
This goal not only create the new container on server template, but also starts container and verify that the new container will be __STARTED__ and available on all server instance of selected server template.

The control will be released only if the new container will be successfully deployed in all process server instances of server template.
To achieve this result the goal needs to poll active servers, the poll is repeated by default every seconds, but the time is configurable setting __verifyServerWaitTime__ not mandatory property.
It is possible also to set the time to wait for each managed server, using _verifyServerMaxTime_ property.
If timeout is exceeded, deploy goal will be perform with success with a WARNING message.

You also need to provide direct access credentials to the controlled servers, setting the mandatory property __username__ and __password__.
Those credentials typically are the same credential with which the controller manages controlled server of the template server.
The user needs to have the role __kie-server__ on remote server instance security contexts.

Parameters are:

| Property             | Default value                 | Mandatory | Name                          | Type            |
| -------------------- |:-----------------------------:|:---------:|:-----------------------------:|:---------------:|
| templateId           | null                          | true      | kie-ctrl.template-id          | String          |
| templateName         | ${templateId}                 | false     | kie-ctrl.template-name        | String          |
| container            | ${project.GAV}                | false     | kie-ctrl.container            | String          |
| runtimeStrategy      | null                          | false     | kie-ctrl.runtime-strategy     | String          |
| kbase                | null                          | false     | kie-ctrl.kbase                | String          |
| ksession             | null                          | false     | kie-ctrl.ksession             | String          |
| mergeMode            | null                          | false     | kie-ctrl.mergeMode            | String          |
| pollInterval         | null                          | false     | kie-ctrl.pollInterval         | Integer         |
| username             | null                          | true      | kie-ctrl.username             | String          |
| password             | null                          | true      | kie-ctrl.password             | String          |
| verifyServerWaitTime | 1000 (in millis)              | false     | kie-ctrl.verifyServerWaitTime | Integer         |
| verifyServerMaxTime  | 300000 (in millis)            | false     | kie-ctrl.verifyServerMaxTime  | Integer         |

### Deploy container example

With the given maven configuration:

```xml

<project>
...
<groupId>it.redhat.demo</groupId>
<artifactId>bpms-signal</artifactId>
<version>1.0.0-SNAPSHOT</version>
...
<build>
  <plugins>
    ...
    <plugin>
      <groupId>org.kie.server</groupId>
      <artifactId>kie-server-controller-plugin</artifactId>
      <version>8.0.0-SNAPSHOT</version>
      <configuration>
        <templateId>process-server</templateId>
        <controllerUsername>${controller.username}</controllerUsername>
        <controllerPassword>${controller.password}</controllerPassword>
        <port>8230</port>
        <username>${kieserver.username}</username>
        <password>${kieserver.password}</password>
    	<runtimeStrategy>PER_PROCESS_INSTANCE</runtimeStrategy>
      </configuration>
    </plugin>
        
  </plugins>
</build>
...
</project>

```

Running the goal:
``` 
mvn kie-ctrl:deploy
```

An example of output might be:
```
[INFO] Protocol: http
[INFO] Host Name: localhost
[INFO] Port: 8230
[INFO] Controller Username: fabio
[INFO] Marshaller extensions init
[INFO] Server Template Id: process-server
[INFO] Server Template Name: null
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Release: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Status: STOPPED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT CREATED on server template process-server
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT STARTED on server template process-server
[INFO] --- Server Template --- 
[INFO] Server Template Id: process-server
[INFO] Server Template Name: process-server
[INFO] Capabilities: [RULE, PROCESS]
[INFO]   Server: http://localhost:8080/kie-server/services/rest/server
[INFO]   Server: http://localhost:8380/kie-server/services/rest/server
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-selection-process:5.0.0
[INFO]   Release: it.redhat.demo:bpms-selection-process:5.0.0
[INFO]   Status: STOPPED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-selection-process:3.0.0
[INFO]   Release: it.redhat.demo:bpms-selection-process:3.0.0
[INFO]   Status: STOPPED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Release: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Status: STARTED
[INFO]     Capability: PROCESS
[INFO]     Config: [ runtimeStrategy = PER_PROCESS_INSTANCE. kSession = Default. kBase = Default. mergeMode = Default ]
[INFO]   ----------------- 
[INFO] ----------------------- 
[INFO] Verify Server - Wait Time: 1000
[INFO] Verify Server - Max Time: 300000
[INFO] Verifying Server: http://localhost:8080/kie-server/services/rest/server
[INFO] Server http://localhost:8080/kie-server/services/rest/server started with messages [Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT successfully created with module it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT.] on date Wed Aug 09 19:34:10 CEST 2017
[INFO] Verifying Server: http://localhost:8380/kie-server/services/rest/server
[INFO] Server http://localhost:8380/kie-server/services/rest/server started with messages [Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT successfully created with module it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT.] on date Wed Aug 09 19:34:14 CEST 2017
```
In the former example we can see that:
1. We've deploy it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT container with it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT release
1. Container has been __CREATED__
2. Container has been __STARTED__
3. Full template server is displayed after deploy: 2 servers, 3 container
4. We've verify that containers have been successfully started on managed server instances
5. The original message generated by the server on which the container has been deployed is also shown

## 11. Goal: dispose-container

This command remove a container from template and before stop the container in case container is in the state __STARTED__.

The goal does not require the project, the container name could be express using __container__ parameter or taken from local maven project using unified execution container name convention.
The property __templateId__ is mandatory.

### Dispose example

With the given maven configuration:
```xml

<project>
...
<groupId>it.redhat.demo</groupId>
<artifactId>bpms-signal</artifactId>
<version>1.0.0-SNAPSHOT</version>
...
<build>
  <plugins>
    ...
    <plugin>
      <groupId>org.kie.server</groupId>
      <artifactId>kie-server-controller-plugin</artifactId>
      <version>8.0.0-SNAPSHOT</version>
      <configuration>
        <controllerUsername>${controller.username}</controllerUsername>
        <controllerPassword>${controller.password}</controllerPassword>
        <port>8230</port>
    	<templateId>process-server</templateId>
      </configuration>
    </plugin>
        
  </plugins>
</build>
...
</project>

```

Running the goal:
``` 
mvn kie-ctrl:dispose-container
```
An example of output might be:
```
[INFO] Protocol: http
[INFO] Host Name: localhost
[INFO] Port: 8230
[INFO] Controller Username: fabio
[INFO] Marshaller extensions init
[INFO] Server Template Id: process-server
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT STOPPED on server template process-server
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT DISPOSED on server template process-server
```
In the former example we can see that:
1. Working on container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
2. Container has been __STOPPED__
3. Container has been __DISPOSED__
