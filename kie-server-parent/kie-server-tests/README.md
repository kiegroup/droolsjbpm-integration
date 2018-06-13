Kie server integration tests
=====================

**This test suite tests the Kie server deployed on various containers like WildFly 10, EAP 7, Tomcat 8,
Oracle WebLogic 12 and IBM WebSphere 9, using Maven Cargo plugin.** Tests cover mainly functionality of Kie server clients, Kie server controller and Kie server router, also contain several REST tests.

Every extension has own dedicated test module. kie-server-integ-tests-all contains tests covering usage of several extensions in one test.

Tests are run very easily using the command

```mvn clean install -P<container-profile> <container-specific-params>```

where `<container-profile>` is simply a particular container. Another container-specific parameters may also be configured (see the table below).
WildFly10, EAP 7 and Tomcat 8 do not have to be pre-installed, they will be downloaded automatically (in case of EAP 7, download URL has to be provided).
Oracle WebLogic 12 and IBM WebSphere 9 have to be pre-installed and the installation path has to be provided using a Maven property `weblogic.home` or `websphere.home` respectively.
Tests are executed using Failsafe plugin. To run specific test class use `-Dit.test=<Test class name>`
Most of the tests can be executed locally as JUnit tests, in this case embedded server is used and only REST endpoints are tested.

The following table lists all currently supported combinations of parameters:

| Container to run    | \<container-profile\> | \<container-specific params\>             |
| -----------------   | --------------------- | ----------------------------------------- |
|     WildFly10       | wildfly10             | *none*                                    |
|     EAP 7           | eap7                  | eap7.download.url                         |
|     Tomcat 9        | tomcat9               | *none*                                    |
| Oracle WebLogic 12  | oracle-wls-12         | weblogic.home                             |
| IBM WebSphere 9*    | websphere9            | websphere.home                            |

\* User used to run tests against WebSphere needs to have write access into WebSphere installation folder, subfolders and files.


## Database configuration
By default, the tests are ran with the H2 database.

