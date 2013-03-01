Drools-Camel Example
=====================

To build this project use

    mvn install

This project includes a unit test, org.drools.camel.example.CamelContextXmlTest, that shows calling this component

To run this project use

    mvn camel:run

To deploy this project into [Fuse ESB](http://fusesource.com/downloads) or [Apache Karaf](http://karaf.apache.org/index/community/download.html)

Start Fuse ESB or Apache Karaf

    <Fuse ESB Home>/bin/fuseesb  or <Karaf Home>/bin/karaf

In the console, use the following commands

    features:addurl mvn:org.drools/drools-karaf-features/6.0.0-SNAPSHOT/xml/features
    features:install drools-module
    features:install drools-camel
    features:install drools-camel-example

To see the results tail the Fuse ESB log

    tail -f <Fuse ESB Home> or <Karaf Home>/data/log/fuseesb.log
    
    2013-02-22 17:52:51,192 | INFO  | imer://testRoute | Home  | ... | Person Young Person is staying home
    2013-02-22 17:52:52,133 | INFO  | imer://testRoute | Bar   | ... | Person Old Person can go to the bar
    2013-02-22 17:52:53,130 | INFO  | imer://testRoute | Bar   | ... | Person Old Person can go to the bar
    2013-02-22 17:52:54,134 | INFO  | imer://testRoute | Home  | ... | Person Young Person is staying home
    2013-02-22 17:52:55,130 | INFO  | imer://testRoute | Home  | ... | Person Young Person is staying home
