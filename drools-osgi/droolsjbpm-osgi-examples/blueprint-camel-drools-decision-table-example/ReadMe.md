Drools-Blueprint-Camel Example
=====================

To build this project use

    mvn install

This project includes a unit test, org.drools.camel.example.CamelContextXmlTest, that shows calling this component

To run this project use

    mvn camel:run

To deploy this project into :

[JBoss Fuse](http://access.redhat.com/downloads) or
[Apache Karaf](http://karaf.apache.org/index/community/download.html)

Start JBoss Fuse or Apache Karaf

    <JBoss Fuse Home>/bin/fuse  or <Karaf Home>/bin/karaf

In the console, use the following commands

    features:addurl mvn:org.drools/drools-karaf-features/6.0.0-SNAPSHOT/xml/features
    features:install drools-module
    features:install drools-decisiontable
    features:install kie-aries-blueprint
    features:install kie-camel
    features:install drools-blueprint-camel-example

To see the results tail the Fuse ESB log

    tail -f <Fuse ESB Home> or <Karaf Home>/data/log/fuseesb.log
    
    2013-06-07 17:26:12,717 | INFO  | uteDecisionTable | Chilton   | 249 - org.apache.camel.camel-core - 2.10.3 | Cheese Stilton costs 10 EUR.
    2013-06-07 17:26:12,842 | INFO  | imer://testRoute | Home      | 249 - org.apache.camel.camel-core - 2.10.3 | Person Young Person is staying home
    2013-06-07 17:26:22,716 | INFO  | uteDecisionTable | Chilton   | 249 - org.apache.camel.camel-core - 2.10.3 | Cheese Stilton costs 10 EUR.
    2013-06-07 17:26:22,839 | INFO  | imer://testRoute | Bar       | 249 - org.apache.camel.camel-core - 2.10.3 | Person Old Person can go to the bar
