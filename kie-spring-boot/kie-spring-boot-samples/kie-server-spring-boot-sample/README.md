KIE Server with all capabilities
========================================

KIE Server SpringBoot sample application that uses Spring Security for securing access to KIE Server resources.
This is a complete (fully featured KIE Server - includes all capabilities) KIE Server that can be used to leverage 
business process management, rules management and planning solutions in single runtime.

How to configure it
------------------------------

Complete configuration is via application.properties of the projects.
Users can decide which KIE Server extensions should be activated via following properties:

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

How to run it
------------------------------

You can run the application by simply starting

```
mvn clean spring-boot:run

```


