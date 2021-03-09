KIE Server
========================================

KIE Server SpringBoot sample application that uses Spring Security for securing access to KIE Server resources.
This example is a KIE Server - including drools, jbpm and case management capabilities- to test Kafka emitter in springboot applications.

How to configure it
------------------------------

Configuration is via application.properties of the projects.
Users can decide which KIE Server extensions should be activated via following properties:

```
kieserver.drools.enabled=true
kieserver.dmn.enabled=false
kieserver.jbpm.enabled=true
kieserver.jbpmui.enabled=true
kieserver.casemgmt.enabled=true
kieserver.optaplanner.enabled=false
kieserver.prometheus.enabled=false
kieserver.scenariosimulation.enabled=false
```

How to run it
------------------------------

You can run the application by simply starting

```
mvn clean spring-boot:run

```


