Drools-Camel Example
=====================

To build this project use

    mvn install

This project includes a unit test, org.drools.camel.example.CamelContextXmlTest, that shows calling this component

To run this project use

    mvn camel:run

To deploy this project into [Fuse ESB](http://fusesource.com/downloads)

Start Fuse ESB

    <Fuse ESB Home>/bin/fuseesb

In the Fuse ESB console, use the following commands

    FuseESB:karaf@root>
    features:addurl mvn:org.drools.karaf/drools/5.5.1-SNAPSHOT/xml/features
    features:install drools-module
    features:install drools-spring
    features:install drools-camel
    features:install drools-camel-example

To see the results tail the Fuse ESB log

    tail -f <Fuse ESB Home>/data/log/fuseesb.log
    
    2013-02-22 17:52:51,192 | INFO  | imer://testRoute | Home  | ... | Person Young Person is staying home
    2013-02-22 17:52:52,133 | INFO  | imer://testRoute | Bar   | ... | Person Old Person can go to the bar
    2013-02-22 17:52:53,130 | INFO  | imer://testRoute | Bar   | ... | Person Old Person can go to the bar
    2013-02-22 17:52:54,134 | INFO  | imer://testRoute | Home  | ... | Person Young Person is staying home
    2013-02-22 17:52:55,130 | INFO  | imer://testRoute | Home  | ... | Person Young Person is staying home
