jBPM Example
=============

This example deploys as a bundle a jBPM process (? display image) which is called when Spring instantiates
the bean "EvaluationProcessExample". The process contains a task ("RegisterTask" )and a user task ("Human Task").

??

To build this project use

    mvn install

To deploy this project into :

[JBoss Fuse](http://access.redhat.com/downloads) or
[Apache Karaf](http://karaf.apache.org/index/community/download.html)

Start JBoss Fuse or Apache Karaf

    <JBoss Fuse Home>/bin/fuse  or <Karaf Home>/bin/karaf

In the console, use the following commands

    features:addurl mvn:org.drools/drools-karaf-features/6.0.1-SNAPSHOT/xml/features
    features:install spring
    features:install spring-dm
    features:install jbpm
    features:install jbpm-example
    # install -s mvn:org.drools/jbpm-osgi-example/6.0.1-SNAPSHOT

To see the results tail the Fuse ESB log

    tail -f <Fuse ESB Home> or <Karaf Home>/data/log/fuseesb.log

    Processing evaluation for employee UserId-12345
    Executing work item WorkItem 1 [name=Human Task, state=0, processInstanceId=1, parameters{NodeName=Two}]
    Executing work item WorkItem 2 [name=RegisterRequest, state=0, processInstanceId=1, parameters{employeeId=UserId-12345}]
