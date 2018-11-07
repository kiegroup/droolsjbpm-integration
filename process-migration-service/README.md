# Process Instance Migration Service

This service is intended to be deployed as a standalone application. It can be integrated with KIE Process Server API to execute migrations remotely.

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