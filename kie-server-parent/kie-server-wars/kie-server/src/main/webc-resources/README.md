Installing KIE Server on Tomcat 8

This instruction describes all steps to install KIE Server on Tomcat 8 standalone distribution - this means it's Tomcat downloaded as zip/tar.

 1. Extract Tomcat archive into desired location - TOMCAT_HOME
 2. Copy following libraries into TOMCAT_HOME/lib
   - javax.security.jacc:javax.security.jacc-api
   - org.kie:kie-tomcat-integration
   - org.slf4j:artifactId=slf4j-api
   - org.slf4j:artifactId=slf4j-jdk14

 versions of these libraries will depend on the release, so best to check what versions are shipped with KIE

 3. Copy JDBC driver lib into TOMCAT_HOME/lib depending on the data base of your choice, as example H2 is used

 4. Configure users and roles in tomcat-users.xml (or different user repository if applicable)
 <tomcat-users>
   <role rolename="admin"/>
   <role rolename="PM"/>
   <role rolename="HR"/>
   <role rolename="analyst"/>
   <role rolename="user"/>
   <role rolename="kie-server"/>

   <user username="testuser" password="testpwd" roles="admin,analyst,PM,HR,kie-server"/>
   <user username="kieserver" password="kieserver1!" roles="kie-server"/>
 </tomcat-users>

 5. Configure data source for data base access by jBPM extension of KIE Server
    Edit TOMCAT_HOME/conf/context.xml and add following within Context tags of the file

       <Resource name="sharedDataSource"
       		  auth="Container"
       		  type="org.h2.jdbcx.JdbcDataSource"
       		  user="sa"
              password="sa"
              url="jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MVCC=TRUE"
              description="H2 Data Source"
              loginTimeout="0"
              testOnBorrow="false"
              factory="org.h2.jdbcx.JdbcDataSourceFactory"/>

 6. Configure JACC Valve for security integration
    Edit TOMCAT_HOME/conf/server.xml and add following in Host section after last Valve declaration

    <Valve className="org.kie.integration.tomcat.JACCValve" />

 7. Create setenv.sh|bat in TOMCAT_HOME/bin with following content

    CATALINA_OPTS="-Xmx512M -Djbpm.tsr.jndi.lookup=java:comp/env/TransactionSynchronizationRegistry -Dorg.kie.server.persistence.ds=java:comp/env/jdbc/jbpm -Djbpm.tm.jndi.lookup=java:comp/env/TransactionManager -Dorg.kie.server.persistence.tm=JBossTS -Dhibernate.connection.release_mode=after_transaction -Dorg.kie.server.id=tomcat-kieserver -Dorg.kie.server.location=http://localhost:8080/kie-server/services/rest/server -Dorg.kie.server.controller=http://localhost:8080/kie-wb/rest/controller"

    Last three parameters might require reconfiguration as they depend on actual environment they run on:
    Actual kie server id to identify given kie server
    -Dorg.kie.server.id=tomcat-kieserver

    Actual location of the kie server over HTTP
    -Dorg.kie.server.location=http://localhost:8080/kie-server/services/rest/server

    Location of the controller in case kie server should run in managed mode
    -Dorg.kie.server.controller=http://localhost:8080/kie-wb/rest/controller

 8. Configure XA Recovery

    Create xa recovery file next to the context.xml with data base configuration with following content:

    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
    <properties>
        <entry key="DB_1_DatabaseUser">sa</entry>
        <entry key="DB_1_DatabasePassword">sa</entry>
        <entry key="DB_1_DatabaseDynamicClass"></entry>
        <entry key="DB_1_DatabaseURL">java:comp/env/h2DataSource</entry>
    </properties>

    Append to CATALINA_OPTS in setenv.sh|bat file following:
    -Dcom.arjuna.ats.jta.recovery.XAResourceRecovery1=com.arjuna.ats.internal.jdbc.recovery.BasicXARecovery\;abs://$CATALINA_HOME/conf/xa-recovery-properties.xml\ \;1

    BasicXARecovery supports following parameters:
     - path to the properties file
     - the number of connections defined in the properties file