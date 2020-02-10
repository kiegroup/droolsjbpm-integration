# Kie Server Maven Plugin

Maven plugin to deploy k-jar container on remote kie-server.

Default goalPrefix: __kieserver__.
Actual goals are: __deploy__, __dispose__ and __update__.

## Plugin configuration

On pom.xml of local project add plugin: 

```xml
<build>
    <plugins>
        ...
        <plugin>
            <groupId>org.kie.server</groupId>
            <artifactId>kie-server-maven-plugin</artifactId>
            <version>7.33.1-SNAPSHOT</version>
            <configuration>
                <username>${kieserver.username}</username>
                <password>${kieserver.password}</password>
            </configuration>
        </plugin>
        
    </plugins>
</build>
```

## Goal: deploy

Deploy the actual kjar project on remote server.

**Note: Deploy procedure does not transfer the kjar over remote server, but only the reference to the artifact.
Remote kie server needs to access, during deploy phase, to kjar and all dependencies.**

Deploy procedure send to remote kie-server only the reference to the k-jar.
Group id, artifact id and version are taken from the maven local project.

It means that **Goal requires project**.

Deploy on kie server is synchronous. It means that the control is released to the client only when the container is running or the deploying process has failed. 

By default the container name follows the unified execution convention: container name is the maven GAV.
To change container name see **Parameter: container** chapter below.

By default the plugin perform deploy on kie server at:
**http://localhost:8080/kie-server**

To change kie-server references, set the **hostname**, **port**, **protocol** and **context-path** properties described below.

Authentication is performed using HTTP basic auth. Properties **username** and **password** are mandatory. 

### Change the runtime strategy

By default the runtime strategy is defined in the k-jar deployment descriptor.
To override it is possible to set *deploy.runtime-strategy* parameter with a valid runtime strategy string.
Possible values are:

1.    SINGLETON: single kie-session for each container
2.    PER_REQUEST: single kie-session for each invocation / request / thread
3.    PER_PROCESS_INSTANCE: single kie-session for each process instance inside the same request
4.    PER_CASE: case management strategy

####Deploy example 
```
mvn kieserver:deploy -Ddeploy.username=fabio -Ddeploy.password=fabio\$739
```

To override k-jar deployment descriptor runtime strategy:

```
mvn kieserver:deploy -Ddeploy.username=fabio -Ddeploy.password=fabio\$739 -Ddeploy.runtime-strategy=PER_REQUEST
```
Alternatively every parameter could be set in configuration plugin

```xml
<plugin>
	<groupId>org.kie.server</groupId>
	<artifactId>kie-server-maven-plugin</artifactId>
	<version>7.5.0-SNAPSHOT</version>
	<configuration>
		<username>${kieserver.username}</username>
		<password>${kieserver.password}</password>
		<runtimeStrategy>${kieserver.runtimeStrategy}</runtimeStrategy>
	</configuration>
</plugin>
```
```
mvn kieserver:deploy
```

## Goal: dispose

Dispose the actual k-jar project on remote server.

By default the container name follows the unified execution convention: container name is the maven GAV.
To change container name see *Parameter: container* chapter below.

**Goal not requires project**

By default the plugin perform deploy on kie server at:
**http://localhost:8080/kie-server**

To change kie-server references, set the **hostname**, **port**, **protocol** and **context-path** properties described below.

Authentication is performed using HTTP basic auth. Properties **username** and **password** are mandatory. 

####Dispose example
```
mvn kieserver:dispose -Ddeploy.username=fabio -Ddeploy.password=fabio\$739
```

## Goal: update

Update the actual k-jar project on remote server.

Deploy procedure send to remote kie-server only the reference to the k-jar.
Group id, artifact id and version are taken from the maven local project.

It means that **Goal requires project**.

By default the container name follows the unified execution convention: container name is the maven GAV.
To change container name see *Parameter: container* chapter below.

By default the plugin perform deploy on kie server at:
**http://localhost:8080/kie-server**

To change kie-server references, set the **hostname**, **port**, **protocol** and **context-path** properties described below.

Authentication is performed using HTTP basic auth. Properties **username** and **password** are mandatory. 

####Update example
```
mvn mvn kieserver:update -Ddeploy.username=fabio -Ddeploy.password=fabio\$739
```
## Parameters

The runtime-strategy parameter is specific of goal deploy.
All other parameters, described in the next section, are common to all goals.

### Parameter: container

Name: __container__

Required: __false__

Default value: __maven project GAV string__

By default container name is the GAV of the current maven project. 
To change the container name set the property container.

The following command try to create a container with my-container-name as a name:
```
mvn kieserver:deploy -Ddeploy.container=my-container-name
```

The following command try to dispose a container with my-container-name as a name:
```
mvn kieserver:dispose -Ddeploy.container=my-container-name
```

### Parameter: hostname

Name: __hostname__

Required: __false__
 
Default value: __localhost__

Hostname of remote kie server.

### Parameter: port

Name: __port__

Required: __false__
 
Default value: __8080__

Port of remote kie server. **Must be a numeric value**.

### Parameter: protocol

Name: __protocol__

Required: __false__
 
Default value: __http__

Http it is the only supported protocol.
Must be a valid http protocol value. Possible values are **http** or **https**.

### Parameter: context-path

Name: __context-path__

Required: __false__
 
Default value: __kie-server__

Kie server remote application context path.

### Parameter: username

Name: __username__

Required: __true__
 
Default value: __NONE__

Username for basic authentication on remote kie server.

### Parameter: password

Name: __password__

Required: __true__
 
Default value: __NONE__

Password for basic authentication on remote kie server.

### Parameter: timeout

Name: __timeout__

Required: __false__
 
Default value: __30000__

Unit: __milliseconds__

Timeout to perform remote invocation. **Must be a numeric value**.
By default timeout is set to 30000 milliseconds, equally to 5 minutes. 

Choose for timeout an arbitrary high value, remember that the deploy on kie server is synchronous. 





