# Kie Controller Maven Plugin

Maven plugin to deploy k-jar container on remote managed kie-server cluster.

Default goalPrefix: __kie-ctrl__.
Actual goals are:
1. deploy
2. dispose
3. get-server-templates
5. get-containers
6. get-container
7. create-container
8. delete-container
9. start-container
10. stop-container

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
          <username>${kieserver.username}</username>
          <password>${kieserver.password}</password>
          <templateId>process-server</templateId>
        </configuration>
    </plugin>
        
  </plugins>
</build>
```

## Managed Deploy 

Managed deploy is performed by a process server central controller (business central or kie-wb).
Process instances must have been registered to the controller using the system properties that allow to form a cluster.
A cluster of managed process server is called a __server template__.

Process server controller can manage more server template.
A managed deploy consists to __add and start a new container to a server template__.
Process server instances member of a server template will have all the same containers with the same configurations.
 
## Goal: get-server-templates

This is an idempotent/query goal to verify server template topologies and configurations. 

The only mandatory parameters are __controllerUsername__ and __controllerPassword__, which are the Basic Authentication credential of remote controller.
The controller user principal must have __rest-all__ role.

You can change the remote controller url access setting __hostname__, __port__, __protocol__ and __contextPath__.
Or change default timeout values setting the __connectionTimeout__ and __socketTimeout__ properties. Timeout values are intended in __seconds__.
Another not mandatory property is __templateId__ if it is not set goal displays all templates info, otherwise only the template set in the property will be displayed.

Default values of tot mandatory properties are:

| Property          | Default value    | 
| ----------------- |:----------------:| 
| hostname          | localhost        | 
| port              | 8080             | 
| protocol          | http             | 
| contextPath       | business-central | 
| connectionTimeout | 100              |
| socketTimeout     | 2                |
| templateId        | null             |

### Get server templates example
``` 
mvn kie-ctrl:get-server-templates -Dkie-ctrl.controller-username=fabio -Dkie-ctrl.controller-password=fabio\$739 -Dkie-ctrl.port=8230
```
Alternatively every parameter could be set in configuration plugin.

An example of output might be:
```
[INFO] Protocol: http
[INFO] Host Name: localhost
[INFO] Port: 8230
[INFO] Controller Username: fabio
[INFO] Controller Password: ###SECRET###
[INFO] Connection Timeout: 100
[INFO] Socket Timeout: 2
[INFO] Context Path: business-central
[INFO] --- Server Template --- 
[INFO] Server Template: process-server
[INFO] Capabilities: [RULE, PROCESS, PLANNING]
[INFO]   Server: http://localhost:8380/kie-server/services/rest/server
[INFO]   Server: http://localhost:8080/kie-server/services/rest/server
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Release: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Status: STARTED
[INFO]     Capability: PROCESS
[INFO]     Config: {org.kie.server.controller.api.model.spec.ProcessConfig={runtimeStrategy=PER_PROCESS_INSTANCE, mergeMode=null, kbase=null, ksession=null}}
[INFO]   ----------------- 
[INFO] ----------------------- 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 15.710 s
[INFO] Finished at: 2017-08-07T13:06:15+02:00
[INFO] Final Memory: 60M/493M
[INFO] ------------------------------------------------------------------------
```
In the former example we can see that:
1. Remote controller has one server template named __process-server__
2. Template __process-server__ control two remote servers.
3. On __process-server__ template a container with id __it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT__ is defined
4. Container __it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT__ has a state __STARTED__
5. On Container __it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT__ the runtime strategy has forced to __PER_PROCESS_INSTANCE__

## Goal: get-containers

If we need to get only status of containers of a server template, we can use this idempotent/query goal.
Parameters are the same of get-server-templates goal, but for this goal __templateId__ is a mandatory parameter.

### Get containers example
``` 
mvn kie-ctrl:get-containers -Dkie-ctrl.controller-username=fabio -Dkie-ctrl.controller-password=fabio\$739 -Dkie-ctrl.port=8230 -Dkie-ctrl.template-id=process-server
```
Alternatively every parameter could be set in configuration plugin.

The output is similar to get-server-templates goal, displaying only containers info.

## Goal: get-container

This goal retrieves container info. The property are the same of get-containers goals.
The container to be shown can be set using __container__ property or taken from the local maven project context using unified execution convention.
If we apply the unified execution convention the container id will be always equals to the __GAV of local maven project__.

### Get container example
``` 
mvn kie-ctrl:get-container -Dkie-ctrl.controller-username=fabio -Dkie-ctrl.controller-password=fabio\$739 -Dkie-ctrl.port=8230 -Dkie-ctrl.template-id=process-server -Dkie-ctrl.container=it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
```
Alternatively every parameter could be set in configuration plugin.

The output is similar to get-server-templates goal, displaying only containers info.

## Goal: create-container

The goals seen so far are just queries to know the actual state of server templates. 
From now on, we will only see commands, each of which will have the effect of changing the configuration of a template server.
 
This goal define a new container for a server template. I use the word define, and not deploy or activate, because container are created in __STOPPED__ status.
To activate the container see __start-container__ or __deploy__ goals, which will be explained later in this file.
A stopped container is present only on controller and not on process server instances.

Mandatory parameters are __controllerUsername__, __controllerPassword__ and __templateId__.
Furthermore the goal must be invoked from the k-jar local maven project.
Said in another way the goal requires project.
 
We can override the connection (not mandatory) parameters:

| Property          | Default value    | 
| ----------------- |:----------------:| 
| hostname          | localhost        | 
| port              | 8080             | 
| protocol          | http             | 
| contextPath       | business-central | 
| connectionTimeout | 100              |
| socketTimeout     | 2                |

But we can override also the process and rule container configuration setting the properties:

1. runtimeStrategy (Process runtime strategy)
2. kbase (Default kie-base)
3. ksession (Default kie-session)
4. mergeMode (Process merge mode)
5. pollInterval (Rule poll interval)
6. scannerStatus (Rule scanner status)

### Create container example

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
[INFO] Controller Password: ###SECRET###
[INFO] Connection Timeout: 100
[INFO] Socket Timeout: 2
[INFO] Context Path: business-central
[INFO] Container: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO] Process Config: Runtime Strategy: PER_PROCESS_INSTANCE - Kie Base: Default - Kie Session: Default - Merge Mode: Default
[INFO] Rule Config: Use Default Rule Config
[INFO] Release id: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO] Marshaller extensions init
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT created on server template process-server
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.531 s
[INFO] Finished at: 2017-08-07T14:37:32+02:00
[INFO] Final Memory: 28M/409M
[INFO] ------------------------------------------------------------------------
```

## Goal: delete-container

This command remove a container from a server template.

If the container is in __STARTED__ status, this command does not undeploy container started on process server instances.
If you need also to stop any running container see __dispose__ goal, which will be explained later in this file.

Parameters are the same of __get-container__ goal. 

### Delete container example
``` 
mvn kie-ctrl:delete-container
```
The output is similar to __create-container__ goal, the difference it is that the message will be:

```
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT disposed on server template process-server
```

Showing __disposed__ instead of __created__ word.

## Goal: start-container

To start a __STOPPED__ container use this command.

Parameters are the same of __get-container__ goal.

### Start container example
``` 
mvn kie-ctrl:start-container
```
The output is similar to __create-container__ goal, the difference it is that the message will be:

```
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT started on server template process-server
```

Showing __started__ instead of __created__ word.

## Goal: stop-container

To stop a __STARTED__ container use this command.

Parameters are the same of __get-container__ goal.

### Stop container example
``` 
mvn kie-ctrl:stop-container
```
The output is similar to __create-container__ goal, the difference it is that the message will be:

```
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT stopped on server template process-server
```

Showing __stopped__ instead of __created__ word.

## Goal: deploy

This is probably the most useful goal for CI/CD integration.
 
This goal not only create the new container on server template, but also starts container and verify that the new container will be __STARTED__ and available on all server instance of selected server template.

The control will be released only if the new container will be successfully deployed in all process server instances of server template.

To achieve this result the goal needs to poll active servers, the poll is repeated by default every seconds, but the time is configurable setting __verifyServerWaitTime__ not mandatory property.
You also need to provide direct access credentials to the controlled servers, setting the mandatory property __username__ and __password__.

This credential typically are the same credential with which the controller manages controlled server of the template server.
The user needs to have the role __kie-server__ on remote server instance security contexts.

In the same way as the create-container goal mandatory parameters are also __controllerUsername__, __controllerPassword__ and __templateId__.
Furthermore the goal must be invoked from the k-jar local maven project. Said in another way the goal requires project.
 
We can override the connection (not mandatory) parameters:

| Property          | Default value    | 
| ----------------- |:----------------:| 
| hostname          | localhost        | 
| port              | 8080             | 
| protocol          | http             | 
| contextPath       | business-central | 
| connectionTimeout | 100              |
| socketTimeout     | 2                |

But we can override also the process and rule container configuration setting the properties:

1. runtimeStrategy (Process runtime strategy)
2. kbase (Default kie-base)
3. ksession (Default kie-session)
4. mergeMode (Process merge mode)
5. pollInterval (Rule poll interval)
6. scannerStatus (Rule scanner status)

### Deploy example

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
        <username>${kieserver.username}</username>
        <password>${kieserver.password}</password>
        <port>8230</port>
    	<templateId>process-server</templateId>
    	<runtimeStrategy>PER_PROCESS_INSTANCE</runtimeStrategy>
      </configuration>
    </plugin>
        
  </plugins>
</build>
...
</project>

```
``` 
mvn kie-ctrl:deploy
```
An example of output might be:
```
[INFO] Protocol: http
[INFO] Host Name: localhost
[INFO] Port: 8230
[INFO] Controller Username: fabio
[INFO] Controller Password: ###SECRET###
[INFO] Connection Timeout: 100
[INFO] Socket Timeout: 2
[INFO] Context Path: business-central
[INFO] Process Config: Runtime Strategy: PER_PROCESS_INSTANCE - Kie Base: Default - Kie Session: Default - Merge Mode: Default
[INFO] Rule Config: Use Default Rule Config
[INFO] Release id: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO] Container: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO] Marshaller extensions init
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT created on server template process-server
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT started on server template process-server
[INFO] --- Server Template --- 
[INFO] Server Template: process-server
[INFO] Capabilities: [RULE, PROCESS, PLANNING]
[INFO]   Server: http://localhost:8380/kie-server/services/rest/server
[INFO]   Server: http://localhost:8080/kie-server/services/rest/server
[INFO]   --- Container --- 
[INFO]   Container: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Release: it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT
[INFO]   Status: STARTED
[INFO]     Capability: PROCESS
[INFO]     Config: {org.kie.server.controller.api.model.spec.ProcessConfig={runtimeStrategy=PER_PROCESS_INSTANCE, mergeMode=null, kbase=null, ksession=null}}
[INFO]   ----------------- 
[INFO] ----------------------- 
[INFO] Verify Server - Wait Time: 1000
[INFO] Verifying Server: http://localhost:8380/kie-server/services/rest/server
[INFO] Server http://localhost:8380/kie-server/services/rest/server started with messages [Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT successfully created with module it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT.] on date Mon Aug 07 15:11:21 CEST 2017
[INFO] Verifying Server: http://localhost:8080/kie-server/services/rest/server
[INFO] Server http://localhost:8080/kie-server/services/rest/server started with messages [Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT successfully created with module it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT.] on date Mon Aug 07 15:11:23 CEST 2017
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 8.096 s
[INFO] Finished at: 2017-08-07T15:11:24+02:00
[INFO] Final Memory: 30M/403M
[INFO] ------------------------------------------------------------------------
```
In the former example we can see that:
1. The container is __created__
2. Created container is __started__
3. The template server content is displayed after deploy
4. It is verified that the container is started on every controlled server
5. The original message generated by the server on which the container has been deployed is also shown

## Goal: dispose

This command remove a container from template and before stop the container in case container is in the state __STARTED__.

The goal does not require the project, the container name could be express using __container__ parameter or taken from local maven project using unified execution container name convention.
The property __templateId__ is mandatory.

### Dispose example
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
``` 
mvn kie-ctrl:dispose
```
An example of output might be:
```
[INFO] Protocol: http
[INFO] Host Name: localhost
[INFO] Port: 8230
[INFO] Controller Username: fabio
[INFO] Controller Password: ###SECRET###
[INFO] Connection Timeout: 100
[INFO] Socket Timeout: 2
[INFO] Context Path: business-central
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT stopped on server template process-server
[INFO] Container it.redhat.demo:bpms-signal:1.0.0-SNAPSHOT disposed on server template process-server
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 1.530 s
[INFO] Finished at: 2017-08-07T15:28:43+02:00
[INFO] Final Memory: 28M/408M
[INFO] ------------------------------------------------------------------------
```
In the former example we can see that:
1. The container is __stopped__ first
2. Stopped container is __disposed__


