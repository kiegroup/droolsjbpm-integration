KIE Server with KeyCloak
========================================

KIE Server SpringBoot sample application that uses KeyCloak and Spring Security for securing access to KIE Server resources.

This sample requires KeyCloak to be installed and configured with following defaults:
- keycloak.auth-server-url=http://localhost:8100/auth
- keycloak.realm=master
- keycloak.resource=springboot-app

all keycloak configuration is present in src/main/resources/application.properties file.

How to configure it
------------------------------

- Download and install KeyCloak. 
- Use default master realm or create new one
- Create client named springboot-app and set its AccessType to public
- Set Valid redirect URI and Web Origin according to your local setup - with default setup they should be set to
	- Valid Redirect URIs: http://localhost:8090/*
	- Web Origins: http://localhost:8090
- Create realm roles that are used in the example (HR and PM)
- Create user named john and password john1 and add HR and/or PM role to that user

How to run it
------------------------------

You can run the application by simply starting

```
mvn clean spring-boot:run

```
There is also a test case that is ignored by default but can be enabled to validate setup


