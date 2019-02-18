jBPM sample - basic
========================================

jBPM SpringBoot sample application that is based on jbpm-spring-boot-starter-basic and provides
all pieces to use jBPM in embedded mode. It provides all services from jbpm-services-api 
that are directly available for injection/autowire.

Supported profiles
------------------------------

This sample application support three profiles:

- H2 (default)
- PostreSQL
- MySQL

configuration of each profile is via application-{profile}.properties file where data source can be configured to desired values.

To be able to use the given profile it needs to be first build with enabled maven profile:

For H2 use following command to build the project:

```
mvn clean install
```

For PostgreSQL use following command to build the project:

```
mvn clean install -Ppostgres
```

For MySQL use following command to build the project:

```
mvn clean install -Pmysql
```

Running the application
------------------------------

For PostgreSQL use following command to build the project:

For H2 use following command to build the project:

```
java -jar target/jbpm-spring-boot-sample-basic-7.18.1-SNAPSHOT.jar evaluation:evaluation:1.0.0-SNAPSHOT
```

```
java -Dspring.profiles.active=postgres -jar target/jbpm-spring-boot-sample-basic-7.18.1-SNAPSHOT.jar evaluation:evaluation:1.0.0-SNAPSHOT
```

For MySQL use following command to build the project:

```
java -Dspring.profiles.active=mysql -jar target/jbpm-spring-boot-sample-basic-7.18.1-SNAPSHOT.jar evaluation:evaluation:1.0.0-SNAPSHOT
```

last part is the kjar that you would like to deploy in GAV format: ** groupId:artifactId:version**

